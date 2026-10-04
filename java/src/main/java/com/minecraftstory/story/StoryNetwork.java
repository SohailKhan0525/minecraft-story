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
        PayloadTypeRegistry.serverboundPlay().register(DialogueChoice.TYPE, DialogueChoice.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DialogueChoice.TYPE, (payload, context) -> {
            context.server().execute(() -> StoryInteraction.handleChoice(context.player(), payload.npcId(), payload.choiceId()));
        });
    }
}
