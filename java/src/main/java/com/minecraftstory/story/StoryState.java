package com.minecraftstory.story;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class StoryState {
    private final Set<StoryFlag> flags = EnumSet.noneOf(StoryFlag.class);
    private final Set<String> completedQuests = new HashSet<>();
    private String activeQuest = "A Bell Before Breakfast";
    private int questProgress = 0;

    public boolean has(StoryFlag flag) {
        return flags.contains(flag);
    }

    public void set(StoryFlag flag) {
        flags.add(flag);
    }

    public void clear(StoryFlag flag) {
        flags.remove(flag);
    }

    public String activeQuest() {
        return activeQuest;
    }

    public void setActiveQuest(String quest) {
        if (quest == null || quest.isBlank()) {
            throw new IllegalArgumentException("Quest name cannot be blank");
        }
        if (!quest.equals(activeQuest)) questProgress = 0;
        activeQuest = quest;
    }

    public int questProgress() {
        return questProgress;
    }

    public void setQuestProgress(int progress) {
        if (progress < 0) throw new IllegalArgumentException("Quest progress cannot be negative");
        questProgress = progress;
    }

    public void completeQuest(String questId) {
        if (questId != null && !questId.isBlank()) completedQuests.add(questId);
    }

    public boolean completedQuest(String questId) {
        return completedQuests.contains(questId);
    }

    public Set<String> completedQuests() {
        return Set.copyOf(completedQuests);
    }

    public Set<StoryFlag> snapshot() {
        return flags.isEmpty() ? EnumSet.noneOf(StoryFlag.class) : EnumSet.copyOf(flags);
    }
}
