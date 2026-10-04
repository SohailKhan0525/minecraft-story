package com.minecraftstory.story;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StoryInteraction {
    private static final Map<UUID, Long> INTRO_COOLDOWN = new ConcurrentHashMap<>();

    private StoryInteraction() {}

    public static void open(ServerPlayer player, String npcId, String sceneId) {
        StorySessionManager.state(player);
        StoryQuestSystem.onNpcInteraction(player, npcId);
        ServerPlayNetworking.send(player, new StoryNetwork.OpenDialogue(npcId, sceneId));
    }

    public static void openIntroduction(ServerPlayer player) {
        long now = player.level().getGameTime();
        long last = INTRO_COOLDOWN.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (now - last < 200) return;
        INTRO_COOLDOWN.put(player.getUUID(), now);

        StoryState state = StorySessionManager.state(player);
        if (state.has(StoryFlag.INTRO_STARTED) || state.has(StoryFlag.INTRO_LIGHT_SEEN) || state.has(StoryFlag.INTRO_MEMORY_MISSING) || state.has(StoryFlag.INTRO_LIGHT_DENIED)) return;
        state.set(StoryFlag.INTRO_STARTED);
        StorySessionManager.save(player);
        open(player, "mara", "havenfall");
    }

    public static void handleChoice(ServerPlayer player, String npcId, String choiceId) {
        StoryState state = StorySessionManager.state(player);
        if (npcId.equals("mara") && !state.activeQuest().equals("A Bell Before Breakfast")) return;
        if (npcId.equals("sera") && (!state.activeQuest().equals("Beneath the Roots") || state.questProgress() < 5)) return;
        if (npcId.equals("crystal") && (!state.activeQuest().equals("The Heart of the Observatory") || state.questProgress() < 4)) return;
        if (npcId.equals("mira") && (!state.has(StoryFlag.CHAPTER_1_COMPLETE) || state.has(StoryFlag.POST_CREDITS_SCENE_SEEN))) return;
        if (npcId.equals("mira") && (!state.has(StoryFlag.CHAPTER_1_COMPLETE) || state.has(StoryFlag.POST_CREDITS_SCENE_SEEN))) return;

        if ("mira".equals(npcId)) {
            state.set(StoryFlag.POST_CREDITS_SCENE_SEEN);
            StorySessionManager.save(player);
            StoryNetwork.cinematic(player, "TO BE CONTINUED", "Chapter 2 — Coming Soon", 240);
            return;
        }

        if ("mira".equals(npcId)) {
            state.set(StoryFlag.POST_CREDITS_SCENE_SEEN);
            StorySessionManager.save(player);
            StoryNetwork.cinematic(player, "TO BE CONTINUED", "CHAPTER 2 — COMING SOON", 240);
            return;
        }

        switch (npcId + ":" + choiceId) {
            case "mara:light_yes" -> { Chapter1Story.chooseIntroduction(state, "saw_the_light"); StoryQuestSystem.resolveChoice(player, "mara"); }
            case "mara:light_no" -> { Chapter1Story.chooseIntroduction(state, "memory_missing"); StoryQuestSystem.resolveChoice(player, "mara"); }
            case "mara:light_unsure" -> { Chapter1Story.chooseIntroduction(state, "deny_light"); StoryQuestSystem.resolveChoice(player, "mara"); }
            case "sera:mercy" -> { Chapter1Story.chooseObservatoryPath(state, true); StoryQuestSystem.resolveChoice(player, "sera"); }
            case "sera:knowledge" -> { Chapter1Story.chooseObservatoryPath(state, false); StoryQuestSystem.resolveChoice(player, "sera"); }
            case "crystal:seal" -> { Chapter1Story.chooseCrystalEnding(state, "seal"); StoryQuestSystem.resolveChoice(player, "crystal"); }
            case "crystal:touch" -> { Chapter1Story.chooseCrystalEnding(state, "touch"); StoryQuestSystem.resolveChoice(player, "crystal"); }
            case "crystal:destroy" -> { Chapter1Story.chooseCrystalEnding(state, "destroy"); StoryQuestSystem.resolveChoice(player, "crystal"); }
            default -> { return; }
        }

        StorySessionManager.save(player);
    }
}
