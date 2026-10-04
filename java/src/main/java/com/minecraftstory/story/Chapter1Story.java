package com.minecraftstory.story;

import java.util.List;

public final class Chapter1Story {
    public static final String TITLE = "The Night the Sky Broke";

    private Chapter1Story() {
    }

    public static List<String> questOrder() {
        return List.of(
            "A Bell Before Breakfast",
            "Blue Fire",
            "Find Elias",
            "Beneath the Roots",
            "The Door Beneath the World",
            "The Hollow Knight",
            "The Heart of the Observatory",
            "The Night Is Not Over"
        );
    }

    public static void chooseIntroduction(StoryState state, String answer) {
        switch (answer) {
            case "saw_the_light" -> state.set(StoryFlag.INTRO_LIGHT_SEEN);
            case "memory_missing" -> state.set(StoryFlag.INTRO_MEMORY_MISSING);
            case "deny_light" -> state.set(StoryFlag.INTRO_LIGHT_DENIED);
            default -> throw new IllegalArgumentException("Unknown introduction choice: " + answer);
        }
    }

    public static void chooseObservatoryPath(StoryState state, boolean mercy) {
        if (mercy) {
            state.set(StoryFlag.MERCY_PATH);
            state.clear(StoryFlag.KNOWLEDGE_PATH);
        } else {
            state.set(StoryFlag.KNOWLEDGE_PATH);
            state.clear(StoryFlag.MERCY_PATH);
        }
    }

    public static void chooseCrystalEnding(StoryState state, String choice) {
        switch (choice) {
            case "seal" -> { state.set(StoryFlag.CRYSTAL_SEALED); state.clear(StoryFlag.CRYSTAL_TOUCHED); state.clear(StoryFlag.CRYSTAL_DESTROY_ATTEMPTED); }
            case "touch" -> { state.set(StoryFlag.CRYSTAL_TOUCHED); state.clear(StoryFlag.CRYSTAL_SEALED); state.clear(StoryFlag.CRYSTAL_DESTROY_ATTEMPTED); }
            case "destroy" -> { state.set(StoryFlag.CRYSTAL_DESTROY_ATTEMPTED); state.clear(StoryFlag.CRYSTAL_SEALED); state.clear(StoryFlag.CRYSTAL_TOUCHED); }
            default -> throw new IllegalArgumentException("Unknown crystal choice: " + choice);
        }
        state.set(StoryFlag.CHAPTER_1_COMPLETE);
        state.set(StoryFlag.CHAPTER_2_UNLOCKED);
    }
}
