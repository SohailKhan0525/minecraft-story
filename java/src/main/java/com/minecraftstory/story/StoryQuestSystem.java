package com.minecraftstory.story;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        // Keep world checks cheap while retaining responsive objective progression.
        if (ticks % 5 != 0) return;

        ServerLevel level = player.serverLevel();
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
                    complete(player, state, "Blue Fire");
                }
            }
            case "cael" -> {
                if ("Blue Fire".equals(state.activeQuest()) && state.questProgress() >= 4) complete(player, state, "Find Elias");
            }
            case "sera" -> {
                if ("Beneath the Roots".equals(state.activeQuest()) && state.questProgress() >= 5) {
                    player.displayClientMessage(Component.literal("Sera is waiting. Choose whether to rescue her or follow the archive."), true);
                }
            }
            case "crystal" -> {
                if ("The Heart of the Observatory".equals(state.activeQuest()) && state.questProgress() >= 4) {
                    player.displayClientMessage(Component.literal("The crystal is waiting for your decision."), true);
                }
            }
            default -> {}
        }
    }

    public static void resolveChoice(ServerPlayer player, String npcId) {
        StoryState state = StorySessionManager.state(player);
        if ("sera".equals(npcId) && "Beneath the Roots".equals(state.activeQuest()) && state.questProgress() >= 5) {
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
            case HARD -> new DifficultyProfile(3, 3, false);
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
        if (progress < 4) {
            if (in(p, 7, 7, 9, 9)) progress = maxProgress(p, s, 2, "The blue flame feels colder up close.");
            if (in(p, 11, 7, 13, 9)) progress = maxProgress(p, s, 3, "The second flame whispers.");
            if (in(p, 7, 11, 9, 13)) progress = maxProgress(p, s, 4, "The third flame answers from underground.");
        }
        if (progress >= 4 && in(p, 5, 5, 15, 15)) complete(p, s, "Find Elias");
    }

    private static void progressElias(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 21, 2, 29, 11)) advance(p, s, 1, "Elias's workshop is empty.");
        if (progress < 2 && in(p, 29, -1, 37, 7)) advance(p, s, 2, "The torn survey rope points east.");
        if (progress < 3 && in(p, 34, -6, 40, 0)) advance(p, s, 3, "You recover a map fragment.");
        if (progress < 4 && in(p, 34, -18, 42, -10)) advance(p, s, 4, "The impossible staircase is real.");
        if (progress < 5 && in(p, 35, -24, 45, -16)) {
            s.set(StoryFlag.ELIAS_FOUND);
            complete(p, s, "Beneath the Roots");
        }
    }

    private static void progressRoots(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 27, -32, 43, -5)) advance(p, s, 1, "The Silent Forest swallows the road.");
        if (in(p, 30, -28, 34, -23)) maxProgress(p, s, 2, "Ancient stone: THE SKY WAS BROKEN HERE.");
        if (in(p, 35, -19, 39, -14)) maxProgress(p, s, 3, "Ancient stone: ELEVEN KNEELED.");
        if (in(p, 39, -12, 43, -7)) maxProgress(p, s, 4, "Ancient stone: ONE REMAINED.");
        if (progress < 5 && in(p, 33, -25, 43, -15)) advance(p, s, 5, "The buried Observatory is ahead.");
        if (progress >= 5 && in(p, 35, -16, 45, -8)) complete(p, s, "The Door Beneath the World");
    }

    private static void progressDoor(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 34, -8, 38, -4)) advance(p, s, 1, "Ash Lens recovered.");
        if (progress < 2 && in(p, 39, -7, 43, -3)) advance(p, s, 2, "Star-Iron Shard recovered.");
        if (progress < 3 && in(p, 42, -4, 46, 0)) advance(p, s, 3, "Warden Seal recovered.");
        if (progress < 4 && in(p, 36, 0, 44, 6)) advance(p, s, 4, "The ring mechanism aligns.");
        if (progress >= 4 && in(p, 38, 5, 46, 11)) complete(p, s, "The Hollow Knight");
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
        if (progress >= 2 && in(p, 36, 3, 52, 20) && noTaggedMobs(level, p, "minecraftstory_hollow_knight")) {
            advance(p, s, 3, "The guardian falls silent.");
            complete(p, s, "The Heart of the Observatory");
        }
    }

    private static void progressHeart(ServerPlayer p, StoryState s, ServerLevel level, int progress) {
        DifficultyProfile d = difficulty(level);
        if (progress < 1 && in(p, 38, 12, 50, 24)) advance(p, s, 1, "Heart Chamber reached.");
        if (progress == 1 && !HEART_WAVES_SPAWNED.getOrDefault(p.getUUID(), false)) {
            HEART_WAVES_SPAWNED.put(p.getUUID(), true);
            spawnWave(level, p, d.combatCount(), "minecraftstory_heart_wave");
            advance(p, s, 2, "First defense survived.");
        } else if (progress == 2 && noTaggedMobs(level, p, "minecraftstory_heart_wave")) {
            spawnWave(level, p, d.combatCount() + 1, "minecraftstory_heart_wave");
            advance(p, s, 3, "Second defense begins.");
        } else if (progress == 3 && noTaggedMobs(level, p, "minecraftstory_heart_wave")) {
            advance(p, s, 4, "The black crystal is exposed.");
        }

    }

    private static void progressNight(ServerPlayer p, StoryState s, int progress) {
        if (progress < 1 && in(p, 35, -12, 49, 0)) advance(p, s, 1, "You escape the collapsing ruins.");
        if (progress < 2 && in(p, 27, -30, 43, -5)) advance(p, s, 2, "The forest is behind you.");
        if (progress < 3 && in(p, -20, 17, 20, 30)) advance(p, s, 3, "The river is safe.");
        if (progress >= 3 && in(p, -30, -5, 30, 5)) complete(p, s, "The Night Is Not Over");
    }

    private static void spawnGuardians(ServerLevel level, ServerPlayer player, int count) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            LivingEntity entity = EntityType.RAVAGER.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (entity == null) continue;
            entity.setPos(player.getX() + 3 + i * 2, player.getY(), player.getZ() + 5);
            entity.setCustomName(Component.literal(i == 0 ? "Hollow Knight" : "Knight's Echo"));
            entity.setCustomNameVisible(true);
            entity.addTag("minecraftstory_hollow_knight");
            if (entity instanceof Mob mob) mob.setTarget(player);
            level.addFreshEntity(entity);
        }
    }

    private static void spawnWave(ServerLevel level, ServerPlayer player, int count, String tag) {
        for (int i = 0; i < Math.max(0, count); i++) {
            LivingEntity entity = EntityType.ZOMBIE.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (entity == null) continue;
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
                .stream().noneMatch(m -> m.getTags().contains(tag) && !m.isDeadOrDying());
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
        p.displayClientMessage(Component.literal("Quest: " + s.activeQuest() + " [" + value + "/" + target + "] — " + text), true);
        StorySessionManager.save(p);
    }

    private static void complete(ServerPlayer p, StoryState s, String nextQuest) {
        String currentId = questId(s.activeQuest());
        if (currentId != null) s.completeQuest(currentId);
        p.displayClientMessage(Component.literal("Quest complete: " + s.activeQuest()), true);
        if (nextQuest == null || nextQuest.equals(s.activeQuest())) {
            s.set(StoryFlag.CHAPTER_1_COMPLETE);
            s.set(StoryFlag.CHAPTER_2_UNLOCKED);
            p.displayClientMessage(Component.literal("Chapter 1 complete — Chapter 2 unlocked."), true);
        } else {
            s.setActiveQuest(nextQuest);
            s.setQuestProgress(0);
        }
        StorySessionManager.save(p);
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
