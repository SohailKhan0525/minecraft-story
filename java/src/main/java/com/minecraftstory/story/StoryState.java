package com.minecraftstory.story;

import java.util.EnumSet;
import java.util.Set;

public final class StoryState {
    private final Set<StoryFlag> flags = EnumSet.noneOf(StoryFlag.class);
    private String activeQuest = "A Bell Before Breakfast";

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
        activeQuest = quest;
    }

    public Set<StoryFlag> snapshot() {
        return flags.isEmpty() ? EnumSet.noneOf(StoryFlag.class) : EnumSet.copyOf(flags);
    }
}
