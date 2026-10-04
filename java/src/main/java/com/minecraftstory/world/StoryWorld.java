package com.minecraftstory.world;

import com.minecraftstory.story.StoryInteraction;
import com.minecraftstory.story.StorySessionManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
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
    private static final Set<ServerLevel> BUILT_LEVELS = ConcurrentHashMap.newKeySet();

    private static final Map<String, NpcSpec> NPCS = Map.of(
            "mara", new NpcSpec("Mara Vale", 20, 64, 0),
            "elias", new NpcSpec("Elias Venn", 24, 64, 4),
            "cael", new NpcSpec("Brother Cael", 10, 64, 8),
            "sera", new NpcSpec("Sera Voss", 38, 64, -8),
            "bram", new NpcSpec("Bram the Baker", 28, 64, 8),
            "nessa", new NpcSpec("Nessa the Blacksmith", 0, 64, -8)
    );

    private StoryWorld() {}

    public static void register() {
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(entity instanceof Villager villager)) {
                return InteractionResult.PASS;
            }
            String npcId = villager.getTags().stream().filter(tag -> tag.startsWith("minecraftstory_npc:")).findFirst().map(tag -> tag.substring("minecraftstory_npc:".length())).orElse("");
            if (npcId.isBlank()) return InteractionResult.PASS;

            String scene = switch (npcId) {
                case "mara" -> "havenfall";
                case "elias" -> "observatory";
                case "cael" -> "chapel";
                case "sera" -> "silent_forest";
                default -> "havenfall";
            };
            StoryInteraction.open(serverPlayer, npcId, scene);
            return InteractionResult.SUCCESS;
        });

        ServerPlayerEvents.JOIN.register(player -> {
            StorySessionManager.state(player);
            ensureWorld((ServerLevel) player.level());
            spawnNpcs((ServerLevel) player.level());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                if (!level.players().isEmpty()) {
                    ensureWorld(level);
                    spawnNpcs(level);
                }
            }
        });
    }

    private static void ensureWorld(ServerLevel level) {
        if (!BUILT_LEVELS.add(level)) return;

        int ox = STORY_ORIGIN.getX();
        int oy = STORY_ORIGIN.getY();
        int oz = STORY_ORIGIN.getZ();

        for (int x = -42; x <= 42; x++) {
            for (int z = -42; z <= 42; z++) {
                level.setBlockAndUpdate(new BlockPos(ox + x, oy - 1, oz + z), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }

        // River and crossing.
        fill(level, new BlockPos(-42, oy, 20), new BlockPos(42, oy, 25), Blocks.WATER.defaultBlockState());
        fill(level, new BlockPos(-42, oy, 19), new BlockPos(42, oy, 19), Blocks.SAND.defaultBlockState());
        fill(level, new BlockPos(-42, oy, 26), new BlockPos(42, oy, 26), Blocks.SAND.defaultBlockState());

        // Village roads.
        fill(level, new BlockPos(-30, oy, -2), new BlockPos(30, oy, 2), Blocks.PATH_BLOCK.defaultBlockState());
        fill(level, new BlockPos(-2, oy, -30), new BlockPos(2, oy, 20), Blocks.PATH_BLOCK.defaultBlockState());

        buildHouse(level, 8, oy, 8, 9, 7, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        buildHouse(level, 24, oy, 6, 9, 7, Blocks.BRICKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState());
        buildHouse(level, 0, oy, -8, 9, 7, Blocks.COBBLESTONE.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState());

        // Forest edge / observatory direction.
        for (int x = 28; x <= 42; x += 4) {
            for (int z = -28; z <= -4; z += 5) {
                makeTree(level, x, oy, z);
            }
        }


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

    private static void fill(ServerLevel level, BlockPos a, BlockPos b, BlockState state) {
        for (int x = Math.min(a.getX(), b.getX()); x <= Math.max(a.getX(), b.getX()); x++) {
            for (int z = Math.min(a.getZ(), b.getZ()); z <= Math.max(a.getZ(), b.getZ()); z++) {
                level.setBlockAndUpdate(new BlockPos(x, a.getY(), z), state);
            }
        }
    }

    private static void spawnNpcs(ServerLevel level) {
        for (Map.Entry<String, NpcSpec> entry : NPCS.entrySet()) {
            String id = entry.getKey();
            NpcSpec spec = entry.getValue();
            boolean exists = level.getEntitiesOfClass(Villager.class,
                    new AABB(spec.x - 2, spec.y - 1, spec.z - 2, spec.x + 2, spec.y + 3, spec.z + 2))
                    .stream()
                    .anyMatch(v -> id.equals(v.getTags().stream().filter(tag -> tag.startsWith("minecraftstory_npc:")).findFirst().map(tag -> tag.substring("minecraftstory_npc:".length())).orElse("")));
            if (exists) continue;

            Villager villager = EntityType.VILLAGER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (villager == null) continue;
            villager.setPos(spec.x + 0.5, spec.y, spec.z + 0.5);
            villager.setCustomName(net.minecraft.network.chat.Component.literal(spec.name));
            villager.setCustomNameVisible(true);
            villager.addTag("minecraftstory_npc:" + id);
            level.addFreshEntity(villager);
        }
    }

    private record NpcSpec(String name, int x, int y, int z) {}
}
