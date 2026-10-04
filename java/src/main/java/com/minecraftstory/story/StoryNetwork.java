package com.minecraftstory.story;

import com.minecraftstory.MinecraftStory;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class StoryNetwork {
    private StoryNetwork() {}

    public record OpenDialogue(String npcId, String sceneId) implements CustomPacketPayload {
        public static final Type<OpenDialogue> TYPE = new Type<>(MinecraftStory.id("open_dialogue"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenDialogue> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, OpenDialogue::npcId,
                ByteBufCodecs.STRING_UTF8, OpenDialogue::sceneId,
                OpenDialogue::new
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record QuestHudState(String quest, int progress, int target) implements CustomPacketPayload {
        public static final Type<QuestHudState> TYPE = new Type<>(MinecraftStory.id("quest_hud_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, QuestHudState> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, QuestHudState::quest,
                ByteBufCodecs.VAR_INT, QuestHudState::progress,
                ByteBufCodecs.VAR_INT, QuestHudState::target,
                QuestHudState::new
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record DialogueChoice(String npcId, String choiceId) implements CustomPacketPayload {
        public static final Type<DialogueChoice> TYPE = new Type<>(MinecraftStory.id("dialogue_choice"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DialogueChoice> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, DialogueChoice::npcId,
                ByteBufCodecs.STRING_UTF8, DialogueChoice::choiceId,
                DialogueChoice::new
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(OpenDialogue.TYPE, OpenDialogue.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(QuestHudState.TYPE, QuestHudState.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DialogueChoice.TYPE, DialogueChoice.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DialogueChoice.TYPE, (payload, context) -> {
            context.server().execute(() -> StoryInteraction.handleChoice(context.player(), payload.npcId(), payload.choiceId()));
        });
    }
}

    public static void syncQuest(net.minecraft.server.level.ServerPlayer player, StoryState state) {
        int target = StoryQuest.objectives(questId(state.activeQuest())).size();
        ServerPlayNetworking.send(player, new QuestHudState(state.activeQuest(), state.questProgress(), target));
    }

    private static String questId(String title) {
        return switch (title) {
            case "A Bell Before Breakfast" -> "bell";
            case "Blue Fire" -> "blue_fire";
            case "Find Elias" -> "elias";
            case "Beneath the Roots" -> "roots";
            case "The Door Beneath the World" -> "door";
            case "The Hollow Knight" -> "knight";
            case "The Heart of the Observatory" -> "heart";
            case "The Night Is Not Over" -> "night";
            default -> "bell";
        };
    }
