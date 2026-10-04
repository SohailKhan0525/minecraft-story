package com.minecraftstory.story;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StorySessionManager {
    private static final String ROOT = "MinecraftStory";
    private static final Map<UUID, StoryState> STATES = new ConcurrentHashMap<>();

    private StorySessionManager() {}

    public static StoryState state(UUID playerId) {
        return STATES.computeIfAbsent(playerId, ignored -> new StoryState());
    }

    public static StoryState state(ServerPlayer player) {
        StoryState state = STATES.computeIfAbsent(player.getUUID(), ignored -> new StoryState());
        loadIfNeeded(player, state);
        return state;
    }

    public static void reset(UUID playerId) {
        STATES.put(playerId, new StoryState());
    }

    public static void save(ServerPlayer player) {
        StoryState state = STATES.get(player.getUUID());
        if (state == null) return;
        CompoundTag root = new CompoundTag();
        root.putString("activeQuest", state.activeQuest());
        for (StoryFlag flag : StoryFlag.values()) {
            root.putBoolean(flag.name(), state.has(flag));
        }
        player.getPersistentData().put(ROOT, root);
    }

    private static void loadIfNeeded(ServerPlayer player, StoryState state) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        if (root.isEmpty()) return;
        String quest = root.getString("activeQuest").orElse("");
        if (!quest.isBlank()) state.setActiveQuest(quest);
        for (StoryFlag flag : StoryFlag.values()) {
            if (root.getBooleanOr(flag.name(), false)) state.set(flag);
        }
    }
}
