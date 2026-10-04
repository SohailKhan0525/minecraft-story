package com.minecraftstory.story;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class StoryInteraction {
    private StoryInteraction() {}

    public static void open(ServerPlayer player, String npcId, String sceneId) {
        StorySessionManager.state(player);
        ServerPlayNetworking.send(player, new StoryNetwork.OpenDialogue(npcId, sceneId));
    }

    public static void handleChoice(ServerPlayer player, String npcId, String choiceId) {
        StoryState state = StorySessionManager.state(player);

        switch (npcId + ":" + choiceId) {
            case "mara:light_yes" -> Chapter1Story.chooseIntroduction(state, "saw_the_light");
            case "mara:light_no" -> Chapter1Story.chooseIntroduction(state, "memory_missing");
            case "mara:light_unsure" -> Chapter1Story.chooseIntroduction(state, "deny_light");
            case "sera:mercy" -> Chapter1Story.chooseObservatoryPath(state, true);
            case "sera:knowledge" -> Chapter1Story.chooseObservatoryPath(state, false);
            case "crystal:seal" -> Chapter1Story.chooseCrystalEnding(state, "seal");
            case "crystal:touch" -> Chapter1Story.chooseCrystalEnding(state, "touch");
            case "crystal:destroy" -> Chapter1Story.chooseCrystalEnding(state, "destroy");
            default -> { return; }
        }

        StorySessionManager.save(player);
    }
}
