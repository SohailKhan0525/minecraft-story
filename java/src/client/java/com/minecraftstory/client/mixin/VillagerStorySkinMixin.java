package com.minecraftstory.client.mixin;

import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(VillagerRenderer.class)
public abstract class VillagerStorySkinMixin {
    @Unique
    private static final Map<VillagerRenderState, Identifier> MINECRAFTSTORY_SKINS = new WeakHashMap<>();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void minecraftstory$extractSkin(Villager villager, VillagerRenderState state, float partialTick, CallbackInfo ci) {
        String name = villager.getCustomName() == null ? "" : villager.getCustomName().getString();
        String skin = switch (name) {
            case "Mara Vale" -> "mara";
            case "Elias Venn" -> "elias";
            case "Brother Cael" -> "cael";
            case "Sera Voss" -> "sera";
            case "Bram the Baker" -> "bram";
            case "Nessa the Blacksmith" -> "nessa";
            case "Mira" -> "mira";
            default -> null;
        };
        if (skin == null) {
            MINECRAFTSTORY_SKINS.remove(state);
        } else {
            MINECRAFTSTORY_SKINS.put(state,
                    Identifier.fromNamespaceAndPath("minecraftstory", "textures/entity/skin/" + skin + ".png"));
        }
    }

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void minecraftstory$storyTexture(VillagerRenderState state, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Identifier> cir) {
        Identifier skin = MINECRAFTSTORY_SKINS.get(state);
        if (skin != null) cir.setReturnValue(skin);
    }
}
