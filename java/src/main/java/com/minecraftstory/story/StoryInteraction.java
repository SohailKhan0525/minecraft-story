package com.minecraftstory.story;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class StoryInteraction {
    private StoryInteraction() {}

    public static void open(ServerPlayer player, String npcId, String sceneId) {
        StorySessionManager.state(player);
        ServerPlayNetworking.send(player, new StoryNetwork.OpenDialogue(npcId, sceneId));
    }

    public static void openIntroduction(ServerPlayer player) {
        StoryState state = StorySessionManager.state(player);
        if (state.has(StoryFlag.INTRO_STARTED) || state.has(StoryFlag.INTRO_LIGHT_SEEN) || state.has(StoryFlag.INTRO_MEMORY_MISSING) || state.has(StoryFlag.INTRO_LIGHT_DENIED)) return;
        state.set(StoryFlag.INTRO_STARTED);
        StorySessionManager.save(player);
        open(player, "mara", "havenfall");
    }

    public static void handleChoice(ServerPlayer player, String npcId, String choiceId) {
        StoryState state = StorySessionManager.state(player);

        switch (npcId + ":" + choiceId) {
            case "mara:light_yes" -> { Chapter1Story.chooseIntroduction(state, "saw_the_light"); state.setActiveQuest("Blue Fire"); }
            case "mara:light_no" -> { Chapter1Story.chooseIntroduction(state, "memory_missing"); state.setActiveQuest("Blue Fire"); }
            case "mara:light_unsure" -> { Chapter1Story.chooseIntroduction(state, "deny_light"); state.setActiveQuest("Blue Fire"); }
            case "sera:mercy" -> { Chapter1Story.chooseObservatoryPath(state, true); state.setActiveQuest("The Door Beneath the World"); }
            case "sera:knowledge" -> { Chapter1Story.chooseObservatoryPath(state, false); state.setActiveQuest("The Door Beneath the World"); }
            case "crystal:seal" -> { Chapter1Story.chooseCrystalEnding(state, "seal"); state.setActiveQuest("The Night Is Not Over"); }
            case "crystal:touch" -> { Chapter1Story.chooseCrystalEnding(state, "touch"); state.setActiveQuest("The Night Is Not Over"); }
            case "crystal:destroy" -> { Chapter1Story.chooseCrystalEnding(state, "destroy"); state.setActiveQuest("The Night Is Not Over"); }
            default -> { return; }
        }

        StorySessionManager.save(player);
    }
}
