package com.minecraftstory.client;

import com.minecraftstory.MinecraftStory;
import com.minecraftstory.story.Chapter1Content;
import com.minecraftstory.story.StoryNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class MinecraftStoryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StoryHud.register(); StoryCinematic.register();

        ClientPlayNetworking.registerGlobalReceiver(StoryNetwork.QuestHudState.TYPE, (payload, context) ->
                context.client().execute(() ->
                        StoryClientState.updateQuest(payload.quest(), payload.progress(), payload.target())
                ));

        ClientPlayNetworking.registerGlobalReceiver(StoryNetwork.CinematicCue.TYPE, (p,c) -> c.client().execute(() -> StoryCinematic.play(p.title(), p.subtitle(), p.duration())));
        ClientPlayNetworking.registerGlobalReceiver(StoryNetwork.OpenDialogue.TYPE, (payload, context) -> {
            context.client().execute(() ->
                    Minecraft.getInstance().gui.setScreen(
                            new DialogueScreen(payload.npcId(), payload.sceneId())
                    ));
        });
    }

    public static String speakerLabel(String id) {
        return switch (id) {
            case "mara" -> "Mara Vale";
            case "elias" -> "Elias Venn";
            case "cael" -> "Brother Cael";
            case "sera" -> "Sera Voss";
            case "bram" -> "Bram";
            case "nessa" -> "Nessa";
            default -> "Unknown";
        };
    }
}
