package com.minecraftstory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinecraftStory implements ModInitializer {
    public static final String MOD_ID = "minecraftstory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Minecraft Story is loading...");
        LOGGER.info("Chapter 1 systems are not implemented yet.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
