package com.minecraftstory.story;

import java.util.List;

public final class StoryQuest {
    public record Objective(String id, String description, int target) {}

    private StoryQuest() {}

    public static List<Objective> objectives(String questId) {
        return switch (questId) {
            case "bell" -> List.of(
                new Objective("reach_mara", "Reach Havenfall and locate Mara Vale.", 1),
                new Objective("speak_mara", "Speak with Mara about the falling light.", 1)
            );
            case "blue_fire" -> List.of(
                new Objective("find_chapel", "Reach the chapel.", 1),
                new Objective("inspect_flame_a", "Inspect the first cold blue flame.", 1),
                new Objective("inspect_flame_b", "Inspect the second cold blue flame.", 1),
                new Objective("inspect_flame_c", "Inspect the third cold blue flame.", 1),
                new Objective("report_cael", "Return to Brother Cael with what you found.", 1)
            );
            case "elias" -> List.of(
                new Objective("find_workshop", "Search Elias's mapmaking workshop.", 1),
                new Objective("follow_rope", "Follow the torn survey rope toward the east road.", 1),
                new Objective("find_map", "Recover Elias's dropped map fragment.", 1),
                new Objective("find_stair", "Find the impossible staircase at the forest edge.", 1),
                new Objective("reach_elias", "Reach Elias below the roots.", 1)
            );
            case "roots" -> List.of(
                new Objective("enter_forest", "Push through the Silent Forest.", 1),
                new Objective("stone_markers", "Read three ancient stone markers.", 3),
                new Objective("observatory", "Reach the buried Observatory.", 1),
                new Objective("map_core", "Recover the Observatory map from its central table.", 1),
                new Objective("reach_sera", "Meet Sera at the edge of the Observatory.", 1)
            );
            case "door" -> List.of(
                new Objective("ash_lens", "Recover the Ash Lens.", 1),
                new Objective("star_iron", "Recover the Star-Iron Shard.", 1),
                new Objective("warden_seal", "Recover the Warden Seal.", 1),
                new Objective("align_ring", "Align the Observatory ring mechanism.", 1),
                new Objective("open_door", "Open the door beneath the world.", 1)
            );
            case "knight" -> List.of(
                new Objective("guardian", "Face the Hollow Knight.", 1),
                new Objective("break_guard", "Survive the guardian's first assault.", 1),
                new Objective("fracture", "Survive the Hollow Knight's fracture.", 1),
                new Objective("defeat_guard", "Defeat the Hollow Knight.", 1)
            );
            case "heart" -> List.of(
                new Objective("reach_heart", "Reach the Heart Chamber.", 1),
                new Objective("survive_wave_1", "Survive the first defense.", 1),
                new Objective("survive_wave_2", "Survive the second defense.", 1),
                new Objective("reach_crystal", "Reach the black crystal.", 1),
                new Objective("choose_fate", "Decide the fate of the crystal.", 1)
            );
            case "night" -> List.of(
                new Objective("leave_ruins", "Escape the collapsing Observatory.", 1),
                new Objective("cross_forest", "Cross the Silent Forest.", 1),
                new Objective("reach_river", "Reach the river.", 1),
                new Objective("witness_lights", "Witness the four lights above Havenfall.", 1)
            );
            default -> List.of(new Objective("unknown", "Complete the current story objective.", 1));
        };
    }

    public static int targetMinutes(String questId) {
        return switch (questId) {
            case "bell" -> 8;
            case "blue_fire" -> 11;
            case "elias" -> 12;
            case "roots" -> 13;
            case "door" -> 14;
            case "knight" -> 9;
            case "heart" -> 16;
            case "night" -> 10;
            default -> 5;
        };
    }
}

