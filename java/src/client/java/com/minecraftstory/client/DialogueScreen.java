package com.minecraftstory.client;

import com.minecraftstory.story.Chapter1Content;
import com.minecraftstory.story.StoryNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class DialogueScreen extends Screen {
    private final String npcId;
    private final String sceneId;
    private final Chapter1Content.Scene scene;
    private int scrollOffset;

    public DialogueScreen(String npcId, String sceneId) {
        super(Component.literal("Chapter 1"));
        this.npcId = npcId;
        this.sceneId = sceneId;
        this.scene = Chapter1Content.scene(sceneId);
    }

    @Override
    protected void init() {
        StoryVoice.playScene(sceneId);

        if (!hasVoiceAsset()) {
            if (!scene.lines().isEmpty()) {
                Chapter1Content.Line first = scene.lines().get(0);
                StoryVoice.speakFallback(first.speaker(), first.text());
            }
        }

        int buttonWidth = Math.min(520, this.width - 40);
        int x = (this.width - buttonWidth) / 2;

        if (hasChoice()) {
            String[] choices = choices();
            for (int i = 0; i < choices.length; i++) {
                String choice = choices[i];
                String label = switch (choice) {
                    case "light_yes" -> "I saw the light.";
                    case "light_no" -> "I didn't see it.";
                    case "light_unsure" -> "I'm not sure.";
                    case "mercy" -> "Go back for Sera.";
                    case "knowledge" -> "Follow the archive route.";
                    case "seal" -> "Seal the crystal.";
                    case "touch" -> "Touch the crystal.";
                    case "destroy" -> "Try to destroy it.";
                    case "continue" -> "Stay a little longer.";
                    default -> choice;
                };
                this.addRenderableWidget(Button.builder(Component.literal(label), b -> choose(choice))
                        .bounds(x, this.height - 95 + i * 25, buttonWidth, 20).build());
            }
        } else {
            this.addRenderableWidget(Button.builder(Component.literal("Continue"), b -> onClose())
                    .bounds(x, this.height - 42, buttonWidth, 20).build());
        }
    }

    private boolean hasVoiceAsset() {
        return this.minecraft != null && this.minecraft.getResourceManager().getResource(StorySounds.asset(sceneId)).isPresent();
    }

    private boolean hasChoice() {
        return npcId.equals("mara") || npcId.equals("sera") || npcId.equals("crystal") || npcId.equals("mira");
    }

    private String[] choices() {
        return switch (npcId) {
            case "mara" -> new String[]{"light_yes", "light_no", "light_unsure"};
            case "sera" -> new String[]{"mercy", "knowledge"};
            case "crystal" -> new String[]{"seal", "touch", "destroy"};
            case "mira" -> new String[]{"continue"};
            default -> new String[0];
        };
    }

    private void choose(String choice) {
        ClientPlayNetworking.send(new StoryNetwork.DialogueChoice(npcId, choice));
        onClose();
    }

    @Override
    public void onClose() {
        StoryVoice.stop();
        super.onClose();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int left = 24;
        int top = 28;
        graphics.fill(14, 14, this.width - 14, this.height - 14, 0xCC090B12);
        graphics.text(this.font, Component.literal(scene.title()),
                left, top, 0xFFFFFFFF, true);

        int y = top + 24;
        List<Chapter1Content.Line> lines = scene.lines();
        int shown = Math.min(lines.size() - scrollOffset, hasChoice() ? 9 : 13);
        for (int i = 0; i < shown; i++) {
            Chapter1Content.Line line = lines.get(i + scrollOffset);
            int color = line.speaker().equalsIgnoreCase(npcId) ? 0xFFE8D6A8 : 0xFFD0D4DE;
            graphics.text(this.font, Component.literal(line.speaker() + ": " + line.text()),
                    left, y, color, false);
            y += 18;
        }

        if (hasChoice()) {
            graphics.text(this.font, Component.literal("Your choice will affect what happens later."),
                    left, this.height - 118, 0xFFB6BAC5, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxOffset = Math.max(0, scene.lines().size() - (hasChoice() ? 9 : 13));
        scrollOffset = (int) Math.max(0, Math.min(maxOffset, scrollOffset - Math.signum(verticalAmount)));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
