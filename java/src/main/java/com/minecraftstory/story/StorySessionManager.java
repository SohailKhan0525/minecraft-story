package com.minecraftstory.story;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StorySessionManager {
    private static final Map<UUID, StoryState> STATES = new ConcurrentHashMap<>();

    private StorySessionManager() {}

    public static StoryState state(UUID playerId) {
        return STATES.computeIfAbsent(playerId, ignored -> new StoryState());
    }

    public static void reset(UUID playerId) {
        STATES.put(playerId, new StoryState());
    }
}
