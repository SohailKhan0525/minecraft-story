package com.minecraftstory.client;

import com.minecraftstory.MinecraftStory;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class StoryHud {
    private static String quest = "";
    private static int progress;
    private static int target;

    private StoryHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                MinecraftStory.id("story_quest"),
                StoryHud::extract
        );
    }

    public static void setQuest(String activeQuest, int questProgress, int questTarget) {
        quest = activeQuest == null ? "" : activeQuest;
        progress = Math.max(0, questProgress);
        target = Math.max(0, questTarget);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || quest.isBlank()) return;

        int width = Math.min(360, minecraft.getWindow().getGuiScaledWidth() - 24);
        int x = 12;
        int y = 12;
        int height = 44;

        graphics.fill(x, y, x + width, y + height, 0xCC111318);
        graphics.outline(x, y, width, height, 0xFF6E7785);
        graphics.text(minecraft.font, Component.literal("CHAPTER 1"), x + 10, y + 7, 0xFFD7B56D, true);
        graphics.text(minecraft.font,
                Component.literal(quest + "  [" + progress + "/" + target + "]"),
                x + 10, y + 20, 0xFFFFFFFF, true);
        graphics.text(minecraft.font, Component.literal("Follow the objective • Explore • Interact"),
                x + 10, y + 32, 0xFFB8C0CC, false);
    }
}
