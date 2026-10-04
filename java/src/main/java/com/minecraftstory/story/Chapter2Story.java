package com.minecraftstory.story;

import java.util.List;

public final class Chapter2Story {
    public static final String TITLE = "The Four Stars";

    private Chapter2Story() {}

    public static List<String> questOrder() {
        return List.of(
                "The First Star",
                "Ashes in Havenfall",
                "The Cartographer's Lie",
                "Beneath the Four",
                "The Returning"
        );
    }

    public static void begin(StoryState state) {
        if (!state.has(StoryFlag.CHAPTER_2_UNLOCKED)) return;
        state.set(StoryFlag.CHAPTER_2_STARTED);
        state.setActiveQuest(questOrder().getFirst());
    }
}
