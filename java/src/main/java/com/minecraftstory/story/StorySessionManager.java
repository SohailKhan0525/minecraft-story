package com.minecraftstory.story;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StorySessionManager {
    private static final Map<UUID, StoryState> STATES = new ConcurrentHashMap<>();

    private StorySessionManager() {}

    public static StoryState state(UUID playerId) {
        return STATES.computeIfAbsent(playerId, ignored -> new StoryState());
    }

    public static StoryState state(ServerPlayer player) {
        StoryState cached = STATES.get(player.getUUID());
        if (cached != null) return cached;

        MinecraftServer server = player.level().getServer();
        StoryState loaded = server == null
                ? new StoryState()
                : StorySavedData.get(server).load(player.getUUID());

        STATES.put(player.getUUID(), loaded);
        return loaded;
    }

    public static void reset(UUID playerId) {
        STATES.put(playerId, new StoryState());
    }

    public static void save(ServerPlayer player) {
        StoryState state = STATES.get(player.getUUID());
        MinecraftServer server = player.level().getServer();
        if (state == null || server == null) return;
        StorySavedData.get(server).save(player.getUUID(), state);
    }
}
