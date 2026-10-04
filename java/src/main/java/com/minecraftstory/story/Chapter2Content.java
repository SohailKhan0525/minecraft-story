package com.minecraftstory.story;

import java.util.List;

public final class Chapter2Content {
    public record Line(String speaker, String text) {}

    private Chapter2Content() {}

    public static List<Line> opening(StoryState state) {
        String ending = state.has(StoryFlag.CRYSTAL_SEALED) ? "sealed the Heart"
                : state.has(StoryFlag.CRYSTAL_TOUCHED) ? "touched the Heart"
                : "tried to destroy the Heart";
        return List.of(
                new Line("Mara", "Four stars appeared after you returned."),
                new Line("Elias", "And one of them is moving."),
                new Line("Cael", "The old records say the stars are doors."),
                new Line("Sera", "You " + ending + ". The mountain noticed."),
                new Line("Wanderer", "Then we find out what opened."),
                new Line("Mara", "Before it finds us.")
        );
    }
}
