package com.minecraftstory.story;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StoryQuestSystem {
    private static final Map<UUID, Integer> TICKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> KNIGHT_SPAWNED = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> HEART_WAVES_SPAWNED = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> KNIGHT_FRACTURE_SPAWNED = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> KNIGHT_MEMORY_SPAWNED = new ConcurrentHashMap<>();

    private StoryQuestSystem() {}

    public static void start(StoryState state, String questTitle) {
        if (!state.activeQuest().equals(questTitle)) state.setActiveQuest(questTitle);
        state.setQuestProgress(0);
    }

    public static void tick(ServerPlayer player) {
        StoryState state = StorySessionManager.state(player);
        String quest = questId(state.activeQuest());
        if (quest == null || state.has(StoryFlag.CHAPTER_1_COMPLETE)) return;

        int ticks = TICKS.merge(player.getUUID(), 1, Integer::sum);
        // Keep world checks cheap while retaining responsive objective progression; physical quest points are handled by block interaction callbacks.
        if (ticks % 5 != 0) return;

        ServerLevel level = player.level();
        int progress = state.questProgress();

        switch (quest) {
            case "bell" -> progressBell(player, state, progress);
            case "blue_fire" -> progressBlueFire(player, state, progress);
            case "elias" -> progressElias(player, state, progress);
            case "roots" -> progressRoots(player, state, progress);
            case "door" -> progressDoor(player, state, progress);
            case "knight" -> progressKnight(player, state, level, progress);
            case "heart" -> progressHeart(player, state, level, progress);
            case "night" -> progressNight(player, state, progress);
            case "first_star" -> progressFirstStar(player, state, progress);
            case "ashes" -> progressAshes(player, state, progress);
            case "cartographer" -> progressCartographer(player, state, progress);
            case "four" -> progressFour(player, state, progress);
            case "returning" -> progressReturning(player, state, progress);
            default -> {}
        }
    }

    public static void onQuestBlockInteraction(ServerPlayer player, net.minecraft.core.BlockPos pos) {
        StoryState state = StorySessionManager.state(player);
        switch (state.activeQuest()) {
            case "Find Elias" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(25, 64, 11)) && state.questProgress() == 0) {
                    setProgress(player, state, 1, "Elias's workshop is empty. A fresh map line points east.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(33, 64, 1)) && state.questProgress() == 1) {
                    setProgress(player, state, 2, "A torn survey rope is still warm.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(36, 64, -6)) && state.questProgress() == 2) {
                    setProgress(player, state, 3, "A map fragment shows a staircase that should not exist.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(35, 64, -14)) && state.questProgress() == 3) {
                    setProgress(player, state, 4, "The trail bends into the Silent Forest.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(39, 64, -22)) && state.questProgress() == 4) {
                    setProgress(player, state, 5, "You find Elias beside the impossible map.");
                }
            }
            case "Blue Fire" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(8, 65, 8)) && state.questProgress() == 1) {
                    setProgress(player, state, 2, "The first cold-blue flame answers.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(12, 65, 8)) && state.questProgress() == 2) {
                    setProgress(player, state, 3, "The second flame whispers a name.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(8, 65, 12)) && state.questProgress() == 3) {
                    setProgress(player, state, 4, "The third flame points underground.");
                }
            }
            case "The First Star" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(54, 64, -24)) && state.questProgress() == 0) {
                    state.set(StoryFlag.CHAPTER_2_STAR_FOUND);
                    setProgress(player, state, 1, "The moving star has left a fragment behind.");
                }
            }
            case "Ashes in Havenfall" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(12, 64, -2)) && state.questProgress() == 0) {
                    state.set(StoryFlag.CHAPTER_2_ASHES_PATH);
                    setProgress(player, state, 1, "The chapel ash remembers the night.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(22, 64, 8)) && state.questProgress() == 1) {
                    setProgress(player, state, 2, "The village bell carries a second voice.");
                }
            }
            case "The Cartographer's Lie" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(54, 64, -8)) && state.questProgress() == 0) {
                    setProgress(player, state, 1, "Elias's copied map contradicts the mountain.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(48, 64, -18)) && state.questProgress() == 1) {
                    state.set(StoryFlag.CHAPTER_2_MAP_TRUTH);
                    setProgress(player, state, 2, "The map was drawn after the sky broke.");
                }
            }
            case "Beneath the Four" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(52, 54, -30)) && state.questProgress() == 0) {
                    setProgress(player, state, 1, "The first star-door opens.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(58, 54, -30)) && state.questProgress() == 1) {
                    setProgress(player, state, 2, "Something on the other side knows your name.");
                }
            }
            case "Beneath the Roots" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(32, 64, -25)) && state.questProgress() == 1) {
                    setProgress(player, state, 2, "The first marker reveals a broken sky.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(37, 64, -16)) && state.questProgress() == 2) {
                    setProgress(player, state, 3, "The second marker names eleven kneeling figures.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(41, 64, -9)) && state.questProgress() == 3) {
                    setProgress(player, state, 4, "The third marker leaves one figure standing.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(38, 54, -10)) && state.questProgress() == 4) {
                    setProgress(player, state, 5, "Elias's impossible map is pinned beneath the roots.");
                }
            }
            case "The Door Beneath the World" -> {
                if (pos.equals(new net.minecraft.core.BlockPos(35, 55, -6)) && state.questProgress() == 0) {
                    StoryItems.give(player, StoryItems.ASH_LENS);
                    setProgress(player, state, 1, "Ash Lens recovered.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(41, 55, -4)) && state.questProgress() == 1) {
                    StoryItems.give(player, StoryItems.STAR_IRON_SHARD);
                    setProgress(player, state, 2, "Star-Iron Shard recovered.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(45, 55, 0)) && state.questProgress() == 2) {
                    StoryItems.give(player, StoryItems.WARDEN_SEAL);
                    setProgress(player, state, 3, "Warden Seal recovered.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(42, 56, -4)) && state.questProgress() == 3
                        && StoryItems.has(player, StoryItems.ASH_LENS)
                        && StoryItems.has(player, StoryItems.STAR_IRON_SHARD)
                        && StoryItems.has(player, StoryItems.WARDEN_SEAL)) {
                    setProgress(player, state, 4, "The Observatory ring recognizes the three relics.");
                } else if (pos.equals(new net.minecraft.core.BlockPos(42, 55, 6)) && state.questProgress() == 4
                        && StoryItems.consume(player, StoryItems.ASH_LENS)
                        && StoryItems.consume(player, StoryItems.STAR_IRON_SHARD)
                        && StoryItems.consume(player, StoryItems.WARDEN_SEAL)) {
                    setProgress(player, state, 5, "The door beneath the world opens."); 
                }
            }
            default -> {}
        }
    }

    public static void onNpcInteraction(ServerPlayer player, String npcId) {
        StoryState state = StorySessionManager.state(player);
        switch (npcId) {
            case "mara" -> {
                if ("A Bell Before Breakfast".equals(state.activeQuest()) &&
                        state.questProgress() >= 1 &&
                        (state.has(StoryFlag.INTRO_LIGHT_SEEN) || state.has(StoryFlag.INTRO_MEMORY_MISSING) || state.has(StoryFlag.INTRO_LIGHT_DENIED))) {
                    setProgress(player, state, 2, "Mara sends you toward the chapel.");
                    complete(player, state, "Blue Fire");
                }
            }
            case "cael" -> {
                if ("Blue Fire".equals(state.activeQuest()) && state.questProgress() >= 4) { setProgress(player, state, 5, "Cael listens in silence."); complete(player, state, "Find Elias"); }
            }
            case "elias" -> {
                if ("Find Elias".equals(state.activeQuest()) && state.questProgress() >= 5) {
                    player.sendSystemMessage(Component.literal("Elias: You actually followed the trail."), true);
                    complete(player, state, "Beneath the Roots");
                }
            }
            case "sera" -> {
                if ("Beneath the Roots".equals(state.activeQuest()) && state.questProgress() >= 5) {
                    player.sendSystemMessage(Component.literal("Sera is waiting. Choose whether to rescue her or follow the archive."), true);
                }
            }
            case "crystal" -> {
                if ("The Heart of the Observatory".equals(state.activeQuest()) && state.questProgress() >= 4) {
                    player.sendSystemMessage(Component.literal("The crystal is waiting for your decision."), true);
                }
            }
            default -> {}
        }
    }

    public static void resolveChoice(ServerPlayer player, String npcId) {
        StoryState state = StorySessionManager.state(player);
        if ("mara".equals(npcId) && "A Bell Before Breakfast".equals(state.activeQuest()) && state.questProgress() >= 1
                && (state.has(StoryFlag.INTRO_LIGHT_SEEN) || state.has(StoryFlag.INTRO_MEMORY_MISSING) || state.has(StoryFlag.INTRO_LIGHT_DENIED))) {
            complete(player, state, "Blue Fire");
        } else if ("sera".equals(npcId) && "Beneath the Roots".equals(state.activeQuest()) && state.questProgress() >= 5) {
            complete(player, state, "The Door Beneath the World");
        } else if ("crystal".equals(npcId) && "The Heart of the Observatory".equals(state.activeQuest()) && state.questProgress() >= 4) {
            complete(player, state, "The Night Is Not Over");
        }
    }

    public static DifficultyProfile difficulty(ServerLevel level) {
        return switch (level.getDifficulty()) {
            case PEACEFUL -> new DifficultyProfile(0, 0, true);
            case EASY -> new DifficultyProfile(1, 1, false);
            case NORMAL -> new DifficultyProfile(2, 2, false);
            case HARD -> new DifficultyProfile(4, 4, false);
        };
    }

    private static void progressBell(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 16, -4, 24, 5)) advance(p, s, 1, "Havenfall found.");
        if (progress >= 1 && (s.has(StoryFlag.INTRO_LIGHT_SEEN) || s.has(StoryFlag.INTRO_MEMORY_MISSING) || s.has(StoryFlag.INTRO_LIGHT_DENIED))) {
            complete(p, s, "Blue Fire");
        }
    }

    private static void progressBlueFire(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 5, 5, 15, 15)) advance(p, s, 1, "Chapel reached.");
    }

    private static void progressElias(ServerPlayer p, StoryState s, int progress) {
        // Find Elias is deliberately interaction-driven. Walking through the forest never silently completes it.
        // Each clue must be physically inspected, and the final clue leads to Elias himself.
    }

    private static void progressRoots(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 27, -32, 43, -5)) advance(p, s, 1, "The Silent Forest swallows the road.");
        if (progress >= 4 && progress < 5 && in(p, 33, -25, 43, -15)) advance(p, s, 5, "The buried Observatory is ahead.");
    }

    private static void progressDoor(ServerPlayer p, StoryState s, int progress) {
        if (progress >= 5 && in(p, 38, 5, 46, 11)) complete(p, s, "The Hollow Knight");
    }

    private static void progressKnight(ServerPlayer p, StoryState s, ServerLevel level, int progress) {
        DifficultyProfile d = difficulty(level);
        if (progress < 1 && in(p, 38, 5, 48, 15)) {
            s.set(StoryFlag.HOLLOW_KNIGHT_SEEN);
            advance(p, s, 1, "The Hollow Knight has found you.");
        }
        if (progress >= 1 && progress < 2 && !KNIGHT_SPAWNED.getOrDefault(p.getUUID(), false)) {
            KNIGHT_SPAWNED.put(p.getUUID(), true);
            spawnGuardians(level, p, d.combatCount());
            advance(p, s, 2, "Survive the guardian assault.");
        }
        if (progress >= 2 && progress < 3 && in(p, 36, 3, 52, 20) && noTaggedMobs(level, p, "minecraftstory_hollow_knight")) {
            KNIGHT_FRACTURE_SPAWNED.put(p.getUUID(), true);
            spawnGuardians(level, p, Math.max(2, d.combatCount() + 1));
            advance(p, s, 3, "The Hollow Knight fractures into echoes.");
        }
        if (progress >= 3 && progress < 4 && in(p, 36, 3, 52, 20)
                && KNIGHT_FRACTURE_SPAWNED.getOrDefault(p.getUUID(), false)
                && noTaggedMobs(level, p, "minecraftstory_hollow_knight")
                && !KNIGHT_MEMORY_SPAWNED.getOrDefault(p.getUUID(), false)) {
            KNIGHT_FRACTURE_SPAWNED.remove(p.getUUID());
            KNIGHT_MEMORY_SPAWNED.put(p.getUUID(), true);
            spawnMemoryBoss(level, p, d);
            playerHint(p, "The Hollow Knight stops moving. Do not attack the memory. Touch the crystal when it opens.");
        }
        if (progress >= 3 && progress < 4 && KNIGHT_MEMORY_SPAWNED.getOrDefault(p.getUUID(), false)
                && noTaggedMobs(level, p, "minecraftstory_hollow_knight")) {
            KNIGHT_MEMORY_SPAWNED.remove(p.getUUID());
            KNIGHT_SPAWNED.remove(p.getUUID());
            advance(p, s, 4, "The memory breaks. The Heart Chamber opens.");
            complete(p, s, "The Heart of the Observatory");
        }
    }

    private static void progressHeart(ServerPlayer p, StoryState s, ServerLevel level, int progress) {
        DifficultyProfile d = difficulty(level);
        if (progress < 1 && in(p, 38, 12, 50, 24)) advance(p, s, 1, "Heart Chamber reached.");
        if (progress == 1 && !HEART_WAVES_SPAWNED.getOrDefault(p.getUUID(), false)) {
            HEART_WAVES_SPAWNED.put(p.getUUID(), true);
            spawnWave(level, p, d.combatCount() + 2, "minecraftstory_heart_wave");
            spawnWardenBoss(level, p, d);
        } else if (progress == 1 && HEART_WAVES_SPAWNED.getOrDefault(p.getUUID(), false)
                && noTaggedMobs(level, p, "minecraftstory_heart_wave")) {
            HEART_WAVES_SPAWNED.remove(p.getUUID());
            advance(p, s, 2, "First defense defeated.");
        } else if (progress == 2 && !HEART_WAVES_SPAWNED.getOrDefault(p.getUUID(), false)) {
            HEART_WAVES_SPAWNED.put(p.getUUID(), true);
            spawnWave(level, p, d.combatCount() + 1, "minecraftstory_heart_wave");
            spawnWardenBoss(level, p, d);
        } else if (progress == 2 && HEART_WAVES_SPAWNED.getOrDefault(p.getUUID(), false)
                && noTaggedMobs(level, p, "minecraftstory_heart_wave")) {
            HEART_WAVES_SPAWNED.remove(p.getUUID());
            advance(p, s, 3, "Second defense defeated.");
        } else if (progress == 3 && in(p, 42, 17, 48, 23)) {
            advance(p, s, 4, "The black crystal is exposed.");
        }

    }

    private static void progressFirstStar(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 48, -30, 60, -15)) {
            advance(p, s, 1, "The northern star has fallen into the forest.");
        }
        if (progress >= 1) complete(p, s, "Ashes in Havenfall");
    }

    private static void progressAshes(ServerPlayer p, StoryState s, int progress) {
        if (progress >= 2) complete(p, s, "The Cartographer's Lie");
    }

    private static void progressCartographer(ServerPlayer p, StoryState s, int progress) {
        if (progress >= 2) complete(p, s, "Beneath the Four");
    }

    private static void progressFour(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 49, -34, 62, -26)) advance(p, s, 1, "The first star-door is awake.");
        if (progress >= 2) complete(p, s, "The Returning");
    }

    private static void progressReturning(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 46, -20, 62, -8)) advance(p, s, 1, "The returning voice is waiting.");
        if (progress >= 1) {
            s.set(StoryFlag.CHAPTER_2_COMPLETE);
            s.set(StoryFlag.CHAPTER_2_UNLOCKED);
            StorySessionManager.save(p);
            StoryNetwork.cinematic(p, "THE FOUR STARS", "Chapter 2 complete\nThe door is open.\nThe story continues.", 240);
            StoryNetwork.syncQuest(p, s);
            p.sendSystemMessage(Component.literal("Chapter 2 complete — the next chapter is unlocked."), true);
            TICKS.remove(p.getUUID());
        }
    }

    private static void progressNight(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 35, -12, 49, 0)) advance(p, s, 1, "You escape the collapsing ruins.");
        if (progress < 2 && in(p, 27, -30, 43, -5)) advance(p, s, 2, "The forest is behind you.");
        if (progress < 3 && in(p, -20, 17, 20, 30)) advance(p, s, 3, "The river is safe.");
        if (progress < 4 && in(p, -30, -5, 30, 5)) advance(p, s, 4, "Havenfall is ahead.");
        if (progress >= 4 && in(p, -30, -5, 30, 5)) complete(p, s, "The Night Is Not Over");
    }

    private static void spawnGuardians(ServerLevel level, ServerPlayer player, int count) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            var spawned = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "ravager")).create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (!(spawned instanceof LivingEntity entity)) continue;
            entity.setPos(player.getX() + 3 + i * 2, player.getY(), player.getZ() + 5);
            entity.setCustomName(Component.literal(i == 0 ? "Hollow Knight" : "Knights Echo"));
            entity.setCustomNameVisible(true);
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                mob.setTarget(player);
                mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(12.0D);
                mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(45.0D);
                mob.setHealth(45.0F);
            }
            entity.addTag("minecraftstory_hollow_knight");
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired();
                mob.setTarget(player);
            }
            level.addFreshEntity(entity);
        }
    }

    private static void spawnMemoryBoss(ServerLevel level, ServerPlayer player, DifficultyProfile difficulty) {
        var spawned = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "ravager"))
                .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (!(spawned instanceof LivingEntity entity)) return;
        entity.setPos(player.getX(), player.getY(), player.getZ() + 7);
        entity.setCustomName(Component.literal("Memory of the Hollow Knight"));
        entity.setCustomNameVisible(true);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.setTarget(player);
            var attack = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            var health = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (attack != null) attack.setBaseValue(22.0D);
            if (health != null) health.setBaseValue(160.0D + difficulty.combatCount() * 45.0D);
            mob.setHealth((float) (160.0D + difficulty.combatCount() * 45.0D));
        }
        entity.addTag("minecraftstory_hollow_knight");
        level.addFreshEntity(entity);
    }

    private static void spawnWardenBoss(ServerLevel level, ServerPlayer player, DifficultyProfile difficulty) {
        var spawned = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "warden"))
                .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (!(spawned instanceof LivingEntity entity)) return;
        entity.setPos(player.getX(), player.getY(), player.getZ() + 8);
        entity.setCustomName(Component.literal("Deep Warden Spawn"));
        entity.setCustomNameVisible(true);
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.setTarget(player);
            var health = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            if (health != null) health.setBaseValue(300.0D + difficulty.combatCount() * 75.0D);
            mob.setHealth((float) (300.0D + difficulty.combatCount() * 75.0D));
        }
        entity.addTag("minecraftstory_heart_wave");
        level.addFreshEntity(entity);
    }

    private static void playerHint(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text), true);
    }

    private static void spawnWave(ServerLevel level, ServerPlayer player, int count, String tag) {
        for (int i = 0; i < Math.max(0, count); i++) {
            var spawned = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", "zombie")).create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (!(spawned instanceof LivingEntity entity)) continue;
            entity.setPos(player.getX() + 3 + i * 1.5, player.getY(), player.getZ() + 6);
            entity.setCustomName(Component.literal("Deep Warden Spawn"));
            entity.setCustomNameVisible(false);
            entity.addTag(tag);
            if (entity instanceof Mob mob) mob.setTarget(player);
            level.addFreshEntity(entity);
        }
    }

    private static boolean noTaggedMobs(ServerLevel level, ServerPlayer player, String tag) {
        return level.getEntitiesOfClass(Mob.class,
                new net.minecraft.world.phys.AABB(player.getX() - 24, player.getY() - 10, player.getZ() - 24,
                        player.getX() + 24, player.getY() + 10, player.getZ() + 24))
                .stream().noneMatch(m -> !m.isDeadOrDying() && (
                        (tag.equals("minecraftstory_hollow_knight") && m.getCustomName() != null
                                && (m.getCustomName().getString().equals("Hollow Knight") || m.getCustomName().getString().equals("Knights Echo")))
                        || (tag.equals("minecraftstory_heart_wave") && m.getCustomName() != null
                                && m.getCustomName().getString().equals("Deep Warden Spawn"))
                ));
    }

    private static int maxProgress(ServerPlayer p, StoryState s, int value, String text) {
        if (s.questProgress() < value) {
            setProgress(p, s, value, text);
        }
        return s.questProgress();
    }

    private static void advance(ServerPlayer p, StoryState s, int value, String text) {
        if (s.questProgress() == value - 1) setProgress(p, s, value, text);
    }

    private static void setProgress(ServerPlayer p, StoryState s, int value, String text) {
        s.setQuestProgress(value);
        int target = StoryQuest.objectives(currentQuestId(s)).size();
        p.sendSystemMessage(Component.literal("Quest: " + s.activeQuest() + " [" + value + "/" + target + "] — " + text), true);
        StorySessionManager.save(p);
        StoryNetwork.syncQuest(p, s);
    }

    private static void complete(ServerPlayer p, StoryState s, String nextQuest) {
        String currentId = questId(s.activeQuest());
        if (currentId != null) s.completeQuest(currentId);
        String finished = s.activeQuest();
        p.sendSystemMessage(Component.literal("Quest complete: " + finished), true);
        if (nextQuest == null || nextQuest.equals(s.activeQuest())) {
            s.set(StoryFlag.CHAPTER_1_COMPLETE);
            StoryNetwork.cinematic(p, "CREDITS", "Story • World • Quests • Characters • Cinematics • Code\\nOriginal game concept • Dialogue • NPCs • Boss design\\nBuilt for Minecraft Java Edition + Fabric\\nSpecial thanks: playtesting • Minecraft • Fabric", 480);
            s.set(StoryFlag.CHAPTER_2_UNLOCKED);
            p.sendSystemMessage(Component.literal("Chapter 1 complete — Chapter 2 unlocked."), true);
        } else {
            s.setActiveQuest(nextQuest);
            s.setQuestProgress(0);
        }
        StorySessionManager.save(p);
        StoryNetwork.syncQuest(p, s);
        TICKS.remove(p.getUUID());
    }

    private static boolean in(ServerPlayer p, int minX, int minZ, int maxX, int maxZ) {
        return p.getX() >= minX && p.getX() <= maxX && p.getZ() >= minZ && p.getZ() <= maxZ;
    }

    private static String currentQuestId(StoryState state) { return questId(state.activeQuest()); }

    private static String questId(String title) {
        return switch (title) {
            case "A Bell Before Breakfast" -> "bell";
            case "Blue Fire" -> "blue_fire";
            case "Find Elias" -> "elias";
            case "Beneath the Roots" -> "roots";
            case "The Door Beneath the World" -> "door";
            case "The Hollow Knight" -> "knight";
            case "The Heart of the Observatory" -> "heart";
            case "The Night Is Not Over" -> "night";
            default -> null;
        };
    }

    public record DifficultyProfile(int combatCount, int extraWaveCount, boolean puzzleOnly) {}
}
