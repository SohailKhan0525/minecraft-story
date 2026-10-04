package com.minecraftstory.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * Story voice runtime.
 *
 * Primary path: generated, original scene performances bundled in the voice resource pack.
 * Fallback path: the player's configured Minecraft narrator/OS speech when the scene audio
 * is not installed. The fallback is deliberately not presented as a recorded actor.
 */
public final class StoryVoice {
    private static SoundInstance activeScene;

    private StoryVoice() {}

    public static void playScene(String sceneId) {
        Minecraft minecraft = Minecraft.getInstance();
        SoundInstance next = Optional.ofNullable(StorySounds.forScene(sceneId))
                .map(sound -> SimpleSoundInstance.forUI(sound, 1.0F))
                .orElse(null);

        stop();
        if (next != null && hasAsset(minecraft, sceneId)) {
            activeScene = next;
            minecraft.getSoundManager().play(next);
        }
    }

    public static void speakFallback(String speaker, String text) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getNarrator().isActive()) {
            minecraft.getNarrator().narrate(Component.literal(speaker + ": " + text));
        }
    }

    public static void stop() {
        Minecraft minecraft = Minecraft.getInstance();
        if (activeScene != null) {
            minecraft.getSoundManager().stop(activeScene);
            activeScene = null;
        }
    }

    private static boolean hasAsset(Minecraft minecraft, String sceneId) {
        return minecraft.getResourceManager().getResource(StorySounds.asset(sceneId)).isPresent();
    }
}
