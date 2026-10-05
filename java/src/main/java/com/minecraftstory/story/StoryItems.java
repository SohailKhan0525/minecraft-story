package com.minecraftstory.story;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StoryItems {
    public static final Item ASH_LENS = register("ash_lens");
    public static final Item STAR_IRON_SHARD = register("star_iron_shard");
    public static final Item WARDEN_SEAL = register("warden_seal");

    private StoryItems() {}

    private static Item register(String id) {
        Identifier identifier = Identifier.fromNamespaceAndPath("minecraftstory", id);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, identifier);
        return Registry.register(
                BuiltInRegistries.ITEM,
                identifier,
                new Item(new Item.Properties().setId(key).stacksTo(1))
        );
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

            Item item = serverPlayer.getItemInHand(hand).getItem();
            if (item == ASH_LENS) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "Ash Lens: a relic recovered from the Observatory. Carry it to the ring mechanism."
                ), true);
                return InteractionResult.SUCCESS;
            }
            if (item == STAR_IRON_SHARD) {
                serverPlayer.displayClientMessage(Component.literal(
                        "Star-Iron Shard: slot it into the Observatory ring after the Ash Lens."
                ), true);
                return InteractionResult.SUCCESS;
            }
            if (item == WARDEN_SEAL) {
                serverPlayer.displayClientMessage(Component.literal(
                        "Warden Seal: the final relic. Use the ring, then open the marked gate."
                ), true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    public static boolean has(ServerPlayer player, Item item) {
        return player.getInventory().contains(new ItemStack(item));
    }

    public static boolean consume(ServerPlayer player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    public static void give(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, Prediction.SERVER_ONLY);
        }
    }
}
