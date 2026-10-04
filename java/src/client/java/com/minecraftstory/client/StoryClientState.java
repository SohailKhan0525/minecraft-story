package com.minecraftstory.client;

public final class StoryClientState {
    private StoryClientState() {}

    public static void updateQuest(String quest, int progress, int target) {
        StoryHud.setQuest(quest, progress, target);
    }
}
