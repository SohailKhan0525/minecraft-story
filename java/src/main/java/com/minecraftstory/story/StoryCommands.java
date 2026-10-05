package com.minecraftstory.story;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class StoryCommands {
    private StoryCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("story")
                .then(Commands.literal("start").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    StorySessionManager.reset(player.getUUID());
                    StoryState state = StorySessionManager.state(player.getUUID());
                    sendScene(context.getSource(), Chapter1Content.scene("cold_open"), state);
                    return 1;
                }))
                .then(Commands.literal("scene")
                    .then(Commands.argument("id", StringArgumentType.word()).executes(context -> {
                        String id = StringArgumentType.getString(context, "id");
                        sendScene(context.getSource(), Chapter1Content.scene(id), state(context));
                        return 1;
                    })))
                .then(Commands.literal("choose")
                    .then(Commands.argument("choice", StringArgumentType.word()).executes(context -> {
                        StoryState state = state(context);
                        String choice = StringArgumentType.getString(context, "choice");
                        applyChoice(state, choice);
                        context.getSource().sendSuccess(() -> Component.literal("Choice recorded: " + choice), false);
                        return 1;
                    })))
                .then(Commands.literal("guide").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    StoryQuestSystem.sendGuide(player, state(context));
                    return 1;
                }))
                .then(Commands.literal("status").executes(context -> {
                    StoryState state = state(context);
                    context.getSource().sendSuccess(() -> Component.literal(
                        "The Night the Sky Broke | Quest: " + state.activeQuest() +
                        " | Progress: " + state.questProgress() + "/" + StoryQuest.objectives(questId(state.activeQuest())).size() +
                        " | Completed: " + state.completedQuests() +
                        " | Flags: " + state.snapshot()
                    ), false);
                    return 1;
                }))
                .then(Commands.literal("content").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(
                        "Chapter 1: " + Chapter1Content.authoredLines() +
                        " authored dialogue lines across " + Chapter1Content.scenes().size() +
                        " scenes. Dialogue target: " + Chapter1Content.dialogueSeconds() + " seconds."
                    ), false);
                    return 1;
                }))
            )
        );
    }

    private static StoryState state(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context) {
        try {
            return StorySessionManager.state(context.getSource().getPlayerOrException().getUUID());
        } catch (Exception e) {
            throw new IllegalStateException("This story command requires a player.");
        }
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
            default -> "unknown";
        };
    }

    private static void applyChoice(StoryState state, String choice) {
        switch (choice) {
            case "saw_the_light", "memory_missing", "deny_light" ->
                Chapter1Story.chooseIntroduction(state, choice);
            case "mercy" -> Chapter1Story.chooseObservatoryPath(state, true);
            case "knowledge" -> Chapter1Story.chooseObservatoryPath(state, false);
            case "seal", "touch", "destroy" ->
                Chapter1Story.chooseCrystalEnding(state, choice);
            default -> throw new IllegalArgumentException("Unknown choice: " + choice);
        }
    }

    private static void sendScene(net.minecraft.commands.CommandSourceStack source, Chapter1Content.Scene scene, StoryState state) {
        source.sendSuccess(() -> Component.literal("=== " + scene.title() + " ==="), false);
        for (Chapter1Content.Line line : scene.lines()) {
            source.sendSuccess(() -> Component.literal(
                "[" + line.speaker() + " • " + line.mood() + "] " + line.text()
            ), false);
        }
        source.sendSuccess(() -> Component.literal(
            "Target scene time: ~" + scene.targetSeconds() + "s | Optional lines are authored for replay/ambient use."
        ), false);
    }
}
