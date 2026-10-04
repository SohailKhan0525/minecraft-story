package com.minecraftstory;

import com.minecraftstory.story.Chapter1Content;
import com.minecraftstory.story.Chapter1Story;
import com.minecraftstory.story.StoryCommands;
import com.minecraftstory.story.StoryNetwork;
import com.minecraftstory.world.StoryWorld;
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
        LOGGER.info("Chapter 1: {}", Chapter1Story.TITLE);
        LOGGER.info("Authored dialogue lines: {}", Chapter1Content.authoredLines());
        LOGGER.info("Dialogue target seconds: {}", Chapter1Content.dialogueSeconds());
        LOGGER.info("NPC count: {}", Chapter1Content.npcs().size());
        LOGGER.info("Quest count: {}", Chapter1Content.quests().size());

        StoryNetwork.register();
        StoryWorld.register();

        // Developer-only tools remain available for testing; normal gameplay never requires them.
        StoryCommands.register();
        // Chapter 1 runtime continues to expand through ordinary in-world interaction.
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
