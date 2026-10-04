package com.minecraftstory.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Voice fallback that uses the player's configured Minecraft narrator/OS speech.
 * This is intentionally not voice cloning and does not pretend to be a recorded actor.
 * When licensed character .ogg performances are added, this class is the single integration point.
 */
public final class StoryVoice {
    private StoryVoice() {}

    public static void speak(String speaker, String text) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.accessibility().narrator().get().isEnabled()) {
            minecraft.getNarrator().say(Component.literal(speaker + ": " + text));
        }
    }
}
