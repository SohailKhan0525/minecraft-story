package com.minecraftstory.world;

import com.minecraftstory.story.StoryInteraction;
import com.minecraftstory.story.StoryQuestSystem;
import com.minecraftstory.story.StorySessionManager;
import com.minecraftstory.story.StoryFlag;
import com.minecraftstory.story.StoryState;
import com.minecraftstory.story.StoryNetwork;
import com.minecraftstory.story.StoryAmbientSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class StoryWorld {
    private static final BlockPos STORY_ORIGIN = new BlockPos(0, 64, 0);
    private static long NPC_SCHEDULE_TICK;

    private static final Map<String, NpcSpec> NPCS = Map.of(
            "mara", new NpcSpec("Mara Vale", 18, 64, -2),
            "elias", new NpcSpec("Elias Venn", 23, 64, 4),
            "cael", new NpcSpec("Brother Cael", 16, 64, 8),
            "sera", new NpcSpec("Sera Voss", 38, 64, -8),
            "bram", new NpcSpec("Bram the Baker", 28, 64, 8),
            "nessa", new NpcSpec("Nessa the Blacksmith", 0, 64, -8),
            "mira", new NpcSpec("Mira", 18, 64, -4)
    );

    private StoryWorld() {}

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            StoryQuestSystem.onQuestBlockInteraction(serverPlayer, hitResult.getBlockPos());
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(entity instanceof Villager villager)) {
                return InteractionResult.PASS;
            }
            String npcId = npcIdFrom(villager);
            if (npcId.isBlank()) return InteractionResult.PASS;

            String scene = switch (npcId) {
                case "mara" -> "havenfall";
                case "elias" -> "observatory";
                case "cael" -> "chapel";
                case "sera" -> "first_choice";
                case "crystal" -> "heart";
                case "mira" -> "post_credits";
                default -> "havenfall";
            };
            StoryInteraction.open(serverPlayer, npcId, scene);
            return InteractionResult.SUCCESS;
        });

        ServerPlayerEvents.JOIN.register(player -> {
            StorySessionManager.state(player);
            ServerLevel level = player.level();
            ensureWorld(level);
            spawnNpcs(level);

            var state = StorySessionManager.state(player);
            StoryNetwork.syncQuest(player, state);
            if (!state.has(StoryFlag.PLAYER_PLACED)) {
                player.setPos(13.5D, 65.0D, 5.5D);
                player.setYRot(180.0F);
                player.setXRot(0.0F);
                state.set(StoryFlag.PLAYER_PLACED);
                StorySessionManager.save(player);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Chapter 1 — The Night the Sky Broke. Mara Vale is waiting in Havenfall."
                ), true);
                StoryNetwork.cinematic(player, "THE NIGHT THE SKY BROKE", "Chapter 1 is beginning. Havenfall is ahead.", 100);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (alive) return;
            StoryState state = StorySessionManager.state(newPlayer);
            respawnAtCheckpoint(newPlayer, state);
            StoryNetwork.syncQuest(newPlayer, state);
            StorySessionManager.save(newPlayer);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                if (!level.players().isEmpty()) {
                    ensureWorld(level);
                    if (level.getGameTime() % 100L == 0L) spawnNpcs(level);
                    triggerProximityScenes(level);
                    for (ServerPlayer player : level.players()) StoryQuestSystem.tick(player);
                    StoryAmbientSystem.tick(level);
                    runNpcSchedules(level);
                }
            }
        });
    }

    private static void triggerProximityScenes(ServerLevel level) {
        Villager mara = level.getEntitiesOfClass(Villager.class,
                new AABB(15, 63, -5, 21, 68, 1)).stream()
                .filter(v -> "Mara Vale".equals(v.getCustomName() == null ? "" : v.getCustomName().getString()))
                .findFirst().orElse(null);
        if (mara == null) return;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(mara) <= 25.0D) StoryInteraction.openIntroduction(player);
        }
    }

    private static void runNpcSchedules(ServerLevel level) {
        if (++NPC_SCHEDULE_TICK % 200 != 0) return;
        long day = level.getGameTime() % 24000L;
        for (Villager villager : level.getEntitiesOfClass(Villager.class, new AABB(-60, 40, -50, 60, 90, 38))) {
            String id = npcIdFrom(villager);
            if (id.isBlank() || villager.isDeadOrDying()) continue;
            double x = villager.getX(), y = villager.getY(), z = villager.getZ();
            switch (id) {
                case "mara" -> { if (day < 6000) villager.getNavigation().moveTo(18, y, 0, 0.7); else villager.getNavigation().moveTo(24, y, 2, 0.65); }
                case "elias" -> villager.getNavigation().moveTo(24, y, 4, 0.55);
                case "cael" -> villager.getNavigation().moveTo(10, y, 8, 0.45);
                case "sera" -> villager.getNavigation().moveTo(38, y, -8, 0.55);
                case "bram" -> villager.getNavigation().moveTo(28, y, 8, 0.45);
                case "nessa" -> villager.getNavigation().moveTo(0, y, -8, 0.45);
                default -> {}
            }
        }
    }

    private static void ensureWorld(ServerLevel level) {
        StoryWorldSavedData saved = StoryWorldSavedData.get(level);
        if (saved.built()) {
            applyWorldRevision(level);
            return;
        }

        int ox = STORY_ORIGIN.getX();
        int oy = STORY_ORIGIN.getY();
        int oz = STORY_ORIGIN.getZ();

        // Large enough for genuine traversal between the village, river, forest, ruin and escape route.
        for (int x = -60; x <= 60; x++) {
            for (int z = -50; z <= 38; z++) {
                level.setBlockAndUpdate(new BlockPos(ox + x, oy - 1, oz + z), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }

        // River, banks and a narrow crossing.
        fill(level, new BlockPos(-60, oy, 28), new BlockPos(60, oy, 33), Blocks.WATER.defaultBlockState());
        fill(level, new BlockPos(-60, oy, 27), new BlockPos(60, oy, 27), Blocks.SAND.defaultBlockState());
        fill(level, new BlockPos(-60, oy, 34), new BlockPos(60, oy, 34), Blocks.SAND.defaultBlockState());
        fill(level, new BlockPos(-4, oy, 28), new BlockPos(4, oy, 33), Blocks.OAK_PLANKS.defaultBlockState());

        // Village roads and buildings.
        fill(level, new BlockPos(-30, oy, -2), new BlockPos(30, oy, 2), Blocks.DIRT_PATH.defaultBlockState());
        fill(level, new BlockPos(-2, oy, -27), new BlockPos(2, oy, 27), Blocks.DIRT_PATH.defaultBlockState());
        buildHouse(level, 8, oy, 8, 9, 7, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        buildHouse(level, 24, oy, 6, 9, 7, Blocks.BRICKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState());
        buildHouse(level, 0, oy, -8, 9, 7, Blocks.COBBLESTONE.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState());

        // Chapel with three physical cold-blue flame investigation points.
        makeChapel(level, 5, oy + 1, 5);

        // Elias trail: five distinct physical clues, spread from the empty workshop into the forest.
        makeMarker(level, 25, oy, 11, Blocks.OAK_PLANKS.defaultBlockState());
        makeMarker(level, 33, oy, 1, Blocks.TRIPWIRE_HOOK.defaultBlockState());
        makeMarker(level, 36, oy, -6, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        makeMarker(level, 35, oy, -14, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        makeMarker(level, 39, oy, -22, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        fill(level, new BlockPos(29, oy, -1), new BlockPos(37, oy, 1), Blocks.GRAVEL.defaultBlockState());
        makeMarker(level, 35, oy - 10, -6, Blocks.GOLD_BLOCK.defaultBlockState());
        makeMarker(level, 41, oy - 10, -4, Blocks.IRON_BLOCK.defaultBlockState());
        makeMarker(level, 45, oy - 10, 0, Blocks.CRYING_OBSIDIAN.defaultBlockState());

        // Silent Forest.
        for (int x = 28; x <= 55; x += 4) {
            for (int z = -42; z <= -4; z += 5) {
                makeTree(level, x, oy, z);
            }
        }

        // Three ancient stone markers, deliberately separated so the player has to explore.
        makeMarker(level, 32, oy, -25, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        makeMarker(level, 37, oy, -16, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        makeMarker(level, 41, oy, -9, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());

        // Revealed staircase into the buried Observatory.
        buildStaircase(level, 39, oy, -18);

        // Underground Observatory floor and chamber.
        buildObservatory(level, 38, oy - 10, -10);
        buildObservatoryRings(level, 42, oy - 10, -4);
        makeMarker(level, 38, oy - 10, -10, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        makeMarker(level, 42, oy - 9, 6, Blocks.IRON_BARS.defaultBlockState());

        // Deep chamber with a visible black-crystal focus.
        buildHeartChamber(level, 45, oy - 10, 20);
        saved.markBuilt();
        applyWorldRevision(level);
    }

    private static void applyWorldRevision(ServerLevel level) {
        StoryWorldRevisionSavedData revision = StoryWorldRevisionSavedData.get(level);
        int current = revision.revision();
        if (current >= 5) return;

        if (current < 2) {
            buildChapterTitle(level, STORY_ORIGIN.getX() - 13, STORY_ORIGIN.getY() + 1, STORY_ORIGIN.getZ() + 18);
        }
        if (current < 4) {
            buildStructuredChapterWorldV4(level);
        }
        buildStructuredChapterWorldV5(level);
        revision.setRevision(5);
    }

    private static void buildStructuredChapterWorldV4(ServerLevel level) {
        // Town hub: a readable route from spawn -> Story Guide -> Mara -> chapel -> workshop.
        route(level, 13, 64, 6, 20, 64, 6);
        route(level, 20, 64, 6, 18, 64, -2);
        route(level, 18, 64, -2, 16, 64, 8);
        route(level, 18, 64, -2, 23, 64, 4);

        // The guide station is an actual interactive bell, not a decorative block.
        level.setBlockAndUpdate(new BlockPos(20, 64, 6), Blocks.CUT_SANDSTONE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(20, 65, 6), Blocks.BELL.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(19, 64, 6), Blocks.GOLD_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(21, 64, 6), Blocks.GOLD_BLOCK.defaultBlockState());

        // Simple homes have real walkable entrances.
        level.setBlockAndUpdate(new BlockPos(12, 64, 8), Blocks.SPRUCE_FENCE_GATE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(28, 64, 8), Blocks.OAK_FENCE_GATE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(4, 64, -8), Blocks.SPRUCE_FENCE_GATE.defaultBlockState());

        // Light the main route so the player can orient themselves at night.
        for (int[] lamp : new int[][]{
                {16, 64, 6}, {18, 64, 2}, {15, 64, 4}, {21, 64, 9},
                {28, 64, 5}, {32, 64, 0}, {35, 64, -6}, {35, 64, -14}, {39, 64, -22}
        }) {
            buildLamp(level, lamp[0], lamp[1], lamp[2]);
        }

        // Forest threshold: obvious boundary and safe walking corridor.
        for (int y = 64; y <= 68; y++) {
            level.setBlockAndUpdate(new BlockPos(27, y, -4), Blocks.STONE_BRICKS.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(29, y, -4), Blocks.STONE_BRICKS.defaultBlockState());
        }
        for (int x = 27; x <= 29; x++) level.setBlockAndUpdate(new BlockPos(x, 68, -4), Blocks.STONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(28, 67, -4), Blocks.GLOWSTONE.defaultBlockState());
        route(level, 25, 64, 11, 28, 64, -4);
        route(level, 28, 64, -4, 39, 64, -22);

        // Correctly connect the forest staircase to the buried Observatory.
        buildStaircase(level, 39, 64, -18);
        buildObservatory(level, 38, 54, -10);
        route(level, 39, 54, 1, 39, 54, -3);
        level.setBlockAndUpdate(new BlockPos(39, 54, -3), Blocks.SPRUCE_FENCE_GATE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(38, 54, -3), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(40, 54, -3), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        for (int y = 55; y <= 58; y++) {
            level.setBlockAndUpdate(new BlockPos(38, y, -3), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(40, y, -3), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        }

        // Observatory route -> sealed gate -> Heart Chamber.
        route(level, 39, 54, -3, 42, 54, 6);
        level.setBlockAndUpdate(new BlockPos(42, 55, 6), Blocks.SPRUCE_FENCE_GATE.defaultBlockState());
        for (int y = 55; y <= 58; y++) {
            level.setBlockAndUpdate(new BlockPos(41, y, 6), Blocks.OBSIDIAN.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(43, y, 6), Blocks.OBSIDIAN.defaultBlockState());
        }
        level.setBlockAndUpdate(new BlockPos(42, 58, 6), Blocks.OBSIDIAN.defaultBlockState());
        route(level, 42, 54, 6, 45, 54, 14);

        // Replace the old one-block "heart" with a real room: floor, four walls, ceiling, entrance.
        buildHeartChamber(level, 45, 54, 20);
        level.setBlockAndUpdate(new BlockPos(45, 55, 14), Blocks.SPRUCE_FENCE_GATE.defaultBlockState());

        // Every checkpoint gets a visible marker.
        for (BlockPos pos : new BlockPos[]{
                new BlockPos(-12, 64, 35), new BlockPos(8, 64, 8),
                new BlockPos(25, 64, 11), new BlockPos(39, 64, -22),
                new BlockPos(42, 55, 2), new BlockPos(42, 55, 6),
                new BlockPos(45, 55, 15), new BlockPos(18, 64, 0)
        }) {
            checkpointMarker(level, pos);
        }
    }

    private static void route(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2) {
        int x = x1;
        int z = z1;
        while (x != x2) {
            level.setBlockAndUpdate(new BlockPos(x, y1, z), Blocks.DIRT_PATH.defaultBlockState());
            x += Integer.signum(x2 - x);
        }
        while (z != z2) {
            level.setBlockAndUpdate(new BlockPos(x, y1, z), Blocks.DIRT_PATH.defaultBlockState());
            z += Integer.signum(z2 - z);
        }
        level.setBlockAndUpdate(new BlockPos(x2, y2, z2), Blocks.DIRT_PATH.defaultBlockState());
    }

    private static void buildLamp(ServerLevel level, int x, int y, int z) {
        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.OAK_FENCE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x, y + 1, z), Blocks.OAK_FENCE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x, y + 2, z), Blocks.LANTERN.defaultBlockState());
    }

    private static void checkpointMarker(ServerLevel level, BlockPos pos) {
        level.setBlockAndUpdate(pos, Blocks.GOLD_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.AMETHYST_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos.above(2), Blocks.GLOWSTONE.defaultBlockState());
    }

    private static void buildChapterTitle(ServerLevel level, int x, int y, int z) {
        String[][] rows = {
                {"1111","1000","1000","1000","1000","1000","1111"},
                {"1001","1001","1001","1111","1001","1001","1001"},
                {"0110","1001","1001","1111","1001","1001","1001"},
                {"1110","1001","1001","1110","1000","1000","1000"},
                {"1111","0110","0110","0110","0110","0110","0110"},
                {"1111","1000","1000","1110","1000","1000","1111"},
                {"1110","1001","1001","1110","1010","1001","1001"},
                {"0000","0000","0000","0000","0000","0000","0000"},
                {"0110","1110","0110","0110","0110","0110","1111"}
        };
        for (int letter = 0; letter < rows.length; letter++) {
            for (int row = 0; row < 7; row++) {
                String glyph = rows[letter][row];
                for (int col = 0; col < glyph.length(); col++) {
                    if (glyph.charAt(col) == '1') {
                        level.setBlockAndUpdate(new BlockPos(x + letter * 5 + col, y + (6 - row), z),
                                Blocks.GOLD_BLOCK.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void respawnAtCheckpoint(ServerPlayer player, StoryState state) {
        BlockPos checkpoint = switch (state.activeQuest()) {
            case "A Bell Before Breakfast" -> new BlockPos(-12, 65, 35);
            case "Blue Fire" -> new BlockPos(8, 66, 8);
            case "Find Elias" -> new BlockPos(25, 65, 11);
            case "Beneath the Roots" -> new BlockPos(39, 65, -22);
            case "The Door Beneath the World" -> new BlockPos(42, 55, 2);
            case "The Hollow Knight" -> new BlockPos(42, 55, 6);
            case "The Heart of the Observatory" -> new BlockPos(45, 55, 15);
            case "The Night Is Not Over" -> new BlockPos(18, 65, 0);
            default -> new BlockPos(18, 65, -2);
        };
        player.teleportTo(checkpoint.getX() + 0.5D, checkpoint.getY(), checkpoint.getZ() + 0.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Chapter 1 checkpoint restored: " + state.activeQuest()
        ), true);
    }


    private static void ensureChapter2World(ServerLevel level) {
        Chapter2WorldSavedData saved = Chapter2WorldSavedData.get(level);
        if (saved.built()) return;
        int y = 64;
        // Northern star trail and the first four-star gate.
        for (int x = 46; x <= 62; x++) {
            for (int z = -34; z <= -8; z++) {
                if ((x + z) % 7 == 0) level.setBlockAndUpdate(new BlockPos(x, y - 1, z), Blocks.MOSS_BLOCK.defaultBlockState());
            }
        }
        makeMarker(level, 54, y, -24, Blocks.AMETHYST_BLOCK.defaultBlockState());
        makeMarker(level, 12, y, -2, Blocks.SOUL_SOIL.defaultBlockState());
        makeMarker(level, 22, y, 8, Blocks.BELL.defaultBlockState());
        makeMarker(level, 54, y, -8, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        makeMarker(level, 48, y, -18, Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        buildStarGate(level, 52, y - 10, -30);
        buildStarGate(level, 58, y - 10, -30);
        saved.markBuilt();
    }

    private static void buildStarGate(ServerLevel level, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 5; dy++) {
                boolean edge = Math.abs(dx) == 2 || dy == 0 || dy == 5;
                level.setBlockAndUpdate(new BlockPos(x + dx, y + dy, z),
                        edge ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
        }
        level.setBlockAndUpdate(new BlockPos(x, y + 2, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
    }

    private static void makeChapel(ServerLevel level, int x, int y, int z) {
        buildHouse(level, x, y, z, 11, 11, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x + 3, y, z + 3), Blocks.SOUL_FIRE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x + 7, y, z + 3), Blocks.SOUL_FIRE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x + 3, y, z + 7), Blocks.SOUL_FIRE.defaultBlockState());
    }

    private static void buildStaircase(ServerLevel level, int x, int y, int z) {
        for (int i = 0; i < 20; i++) {
            int yy = y - 1 - i / 2;
            int zz = z + i;
            level.setBlockAndUpdate(new BlockPos(x, yy, zz), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x - 1, yy, zz), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x + 1, yy, zz), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        }
    }

    private static void buildObservatory(ServerLevel level, int x, int y, int z) {
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                boolean edge = Math.abs(dx) == 7 || Math.abs(dz) == 7;
                level.setBlockAndUpdate(new BlockPos(x + dx, y, z + dz),
                        edge ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        }
        for (int wallY = y + 1; wallY <= y + 4; wallY++) {
            for (int dx = -7; dx <= 7; dx++) {
                level.setBlockAndUpdate(new BlockPos(x + dx, wallY, z - 7), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(x + dx, wallY, z + 7), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
            for (int dz = -7; dz <= 7; dz++) {
                level.setBlockAndUpdate(new BlockPos(x - 7, wallY, z + dz), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(x + 7, wallY, z + dz), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
            }
        }
        for (int dx = -7; dx <= 7; dx++) for (int dz = -7; dz <= 7; dz++) {
            level.setBlockAndUpdate(new BlockPos(x + dx, y + 5, z + dz), Blocks.DEEPSLATE_TILES.defaultBlockState());
        }
        // South entrance aligns with the staircase/route.
        for (int wallY = y + 1; wallY <= y + 2; wallY++) {
            level.setBlockAndUpdate(new BlockPos(x + 1, wallY, z + 7), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos(x, wallY, z + 7), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(new BlockPos(x, y + 1, z), Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x, y + 2, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x, y + 3, z), Blocks.GLOWSTONE.defaultBlockState());
        makeMarker(level, x, y, z - 6, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
    }

    private static void buildObservatoryRings(ServerLevel level, int x, int y, int z) {
        for (int r = 4; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(Math.abs(dx) - r) + Math.abs(Math.abs(dz) - r) <= 1) {
                        level.setBlockAndUpdate(new BlockPos(x + dx, y + 1, z + dz), Blocks.IRON_BLOCK.defaultBlockState());
                    }
                }
            }
        }
        makeMarker(level, x, y + 1, z, Blocks.AMETHYST_BLOCK.defaultBlockState());
    }

    private static void buildHeartChamber(ServerLevel level, int x, int y, int z) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                boolean edge = Math.abs(dx) == 6 || Math.abs(dz) == 6;
                level.setBlockAndUpdate(new BlockPos(x + dx, y, z + dz),
                        edge ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        }
        for (int wallY = y + 1; wallY <= y + 4; wallY++) {
            for (int dx = -6; dx <= 6; dx++) {
                level.setBlockAndUpdate(new BlockPos(x + dx, wallY, z - 6), Blocks.OBSIDIAN.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(x + dx, wallY, z + 6), Blocks.OBSIDIAN.defaultBlockState());
            }
            for (int dz = -6; dz <= 6; dz++) {
                level.setBlockAndUpdate(new BlockPos(x - 6, wallY, z + dz), Blocks.OBSIDIAN.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(x + 6, wallY, z + dz), Blocks.OBSIDIAN.defaultBlockState());
            }
        }
        for (int dx = -6; dx <= 6; dx++) for (int dz = -6; dz <= 6; dz++) {
            level.setBlockAndUpdate(new BlockPos(x + dx, y + 5, z + dz), Blocks.OBSIDIAN.defaultBlockState());
        }
        // South entrance / player-facing approach.
        for (int yy = y + 1; yy <= y + 2; yy++) level.setBlockAndUpdate(new BlockPos(x, yy, z - 6), Blocks.AIR.defaultBlockState());

        level.setBlockAndUpdate(new BlockPos(x, y + 1, z), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x, y + 2, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(new BlockPos(x, y + 3, z), Blocks.GLOWSTONE.defaultBlockState());
    }

    private static void buildHouse(ServerLevel level, int x, int y, int z, int width, int depth, BlockState wall, BlockState roof) {
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                boolean edge = dx == 0 || dz == 0 || dx == width - 1 || dz == depth - 1;
                level.setBlockAndUpdate(new BlockPos(x + dx, y, z + dz), edge ? wall : Blocks.AIR.defaultBlockState());
                for (int h = 1; h <= 4; h++) {
                    if (edge && !(dz == 0 && dx == width / 2 && h <= 2)) {
                        level.setBlockAndUpdate(new BlockPos(x + dx, y + h, z + dz), wall);
                    }
                }
            }
        }
        for (int dx = -1; dx <= width; dx++) {
            for (int dz = -1; dz <= depth; dz++) {
                level.setBlockAndUpdate(new BlockPos(x + dx, y + 5, z + dz), roof);
            }
        }
    }

    private static void makeTree(ServerLevel level, int x, int y, int z) {
        for (int h = 0; h < 5; h++) level.setBlockAndUpdate(new BlockPos(x, y + h, z), Blocks.SPRUCE_LOG.defaultBlockState());
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) + Math.abs(dz) < 4) {
                level.setBlockAndUpdate(new BlockPos(x + dx, y + 4, z + dz), Blocks.SPRUCE_LEAVES.defaultBlockState());
            }
        }
    }

    private static void makeMarker(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlockAndUpdate(new BlockPos(x, y, z), state);
        level.setBlockAndUpdate(new BlockPos(x, y + 1, z), state);
    }

    private static void fill(ServerLevel level, BlockPos a, BlockPos b, BlockState state) {
        for (int x = Math.min(a.getX(), b.getX()); x <= Math.max(a.getX(), b.getX()); x++) {
            for (int z = Math.min(a.getZ(), b.getZ()); z <= Math.max(a.getZ(), b.getZ()); z++) {
                level.setBlockAndUpdate(new BlockPos(x, a.getY(), z), state);
            }
        }
    }

    private static void spawnNpcs(ServerLevel level) {
        boolean findEliasActive = level.players().stream()
                .anyMatch(player -> "Find Elias".equals(StorySessionManager.state(player).activeQuest()));

        for (Villager oldCrystal : level.getEntitiesOfClass(Villager.class, new AABB(34, 45, 14, 52, 62, 26))) {
            if ("Black Crystal".equals(oldCrystal.getCustomName() == null ? "" : oldCrystal.getCustomName().getString())) {
                oldCrystal.discard();
            }
        }

        for (Map.Entry<String, NpcSpec> entry : NPCS.entrySet()) {
            String id = entry.getKey();
            NpcSpec spec = entry.getValue();

            if ("mira".equals(id)) {
                boolean epilogueActive = level.players().stream().anyMatch(player -> {
                    StoryState state = StorySessionManager.state(player);
                    return state.has(StoryFlag.CHAPTER_1_COMPLETE) && !state.has(StoryFlag.POST_CREDITS_SCENE_SEEN);
                });
                if (!epilogueActive) continue;
            }

            double targetX = spec.x + 0.5D;
            double targetY = spec.y;
            double targetZ = spec.z + 0.5D;
            if ("elias".equals(id) && findEliasActive) {
                targetX = 39.5D;
                targetZ = -22.5D;
            }

            String tag = "minecraftstory_npc:" + id;
            var matches = level.getEntitiesOfClass(Villager.class,
                    new AABB(-90, 40, -70, 90, 90, 50)).stream()
.filter(v -> tagNameEquals(v, tag))
                    .toList();
            for (int i = 1; i < matches.size(); i++) matches.get(i).discard();

            Villager villager = matches.isEmpty() ? null : matches.getFirst();
            if (villager == null) {
                villager = (Villager) BuiltInRegistries.ENTITY_TYPE.getValue(
                        Identifier.fromNamespaceAndPath("minecraft", "villager"))
                        .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (villager == null) continue;
                villager.addTag(tag);
                level.addFreshEntity(villager);
                villager.setGlowingTag(true);
            }

            villager.setPos(targetX, targetY, targetZ);
            villager.setCustomName(net.minecraft.network.chat.Component.literal(spec.name));
            villager.setCustomNameVisible(true);
            villager.setPersistenceRequired();
            villager.setGlowingTag(true);
        }
    }
    private static boolean tagNameEquals(Villager villager, String tag) {
        String name = villager.getCustomName() == null ? "" : villager.getCustomName().getString();
        return NPCS.entrySet().stream()
                .anyMatch(entry -> tag.equals("minecraftstory_npc:" + entry.getKey())
                        && entry.getValue().name.equals(name));
    }

    private static String npcIdFrom(Villager villager) {
        String name = villager.getCustomName() == null ? "" : villager.getCustomName().getString();
        return NPCS.entrySet().stream()
                .filter(entry -> entry.getValue().name.equals(name))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }

    private record NpcSpec(String name, int x, int y, int z) {}
}
