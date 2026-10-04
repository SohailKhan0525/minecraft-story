package com.minecraftstory.story;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public final class StoryItems {
    public static final Item ASH_LENS = register("ash_lens");
    public static final Item STAR_IRON_SHARD = register("star_iron_shard");
    public static final Item WARDEN_SEAL = register("warden_seal");

    private StoryItems() {}

    private static Item register(String id) {
        return BuiltInRegistries.ITEM.register(
                Identifier.fromNamespaceAndPath("minecraftstory", id),
                new Item(new Item.Properties().stacksTo(1))
        );
    }

    public static void register() {
        // Static initialization performs registration; this method provides a clear lifecycle hook.
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
        if (!player.getInventory().add(new ItemStack(item))) {
            player.drop(new ItemStack(item), false);
        }
    }
}
