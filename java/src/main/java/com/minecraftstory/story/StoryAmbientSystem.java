package com.minecraftstory.story;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StoryAmbientSystem {
    private static final Map<UUID, Long> LAST_LINE = new ConcurrentHashMap<>();
    private static final Map<String, Integer> CURSOR = new ConcurrentHashMap<>();

    private StoryAmbientSystem() {}

    public static void tick(ServerLevel level) {
        long time = level.getGameTime();
        if (time % 80 != 0) return;

        for (ServerPlayer player : level.players()) {
            Villager npc = nearestStoryNpc(level, player);
            if (npc == null || player.distanceToSqr(npc) > 12 * 12) continue;

            UUID id = player.getUUID();
            long last = LAST_LINE.getOrDefault(id, Long.MIN_VALUE);
            if (time - last < 320) continue;

            String name = npc.getCustomName() == null ? "" : npc.getCustomName().getString();
            StoryState state = StorySessionManager.state(player);
            String line = nextLine(name, state);
            if (line == null) continue;

            player.sendSystemMessage(Component.literal(line), true);
            LAST_LINE.put(id, time);
        }
    }

    private static Villager nearestStoryNpc(ServerLevel level, ServerPlayer player) {
        return level.getEntitiesOfClass(Villager.class,
                new AABB(player.getX() - 12, player.getY() - 5, player.getZ() - 12,
                        player.getX() + 12, player.getY() + 5, player.getZ() + 12))
                .stream()
                .filter(v -> v.getCustomName() != null)
                .filter(v -> isStoryName(v.getCustomName().getString()))
                .min((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
                .orElse(null);
    }

    private static boolean isStoryName(String name) {
        return switch (name) {
            case "Mara Vale", "Elias Venn", "Brother Cael", "Sera Voss",
                 "Bram the Baker", "Nessa the Blacksmith", "Old Renn", "Lio" -> true;
            default -> false;
        };
    }

    private static String nextLine(String name, StoryState state) {
        String key = name + "|" + state.activeQuest();
        String[] lines = switch (name) {
            case "Mara Vale" -> new String[]{
                    "Mara: Keep your eyes on the tree line.",
                    "Mara: Havenfall has survived worse nights. Probably.",
                    "Mara: If you hear whispering, tell me before you answer it."
            };
            case "Elias Venn" -> new String[]{
                    "Elias: I have a theory. I also have several worse theories.",
                    "Elias: Maps are supposed to explain places. This one keeps changing them.",
                    "Elias: Please don't touch anything ancient until I finish writing it down."
            };
            case "Brother Cael" -> new String[]{
                    "Cael: The chapel flame has been cold since dawn.",
                    "Cael: Some prayers are better left unanswered.",
                    "Cael: If the silence speaks, do not speak back."
            };
            case "Sera Voss" -> new String[]{
                    "Sera: I have survived worse expeditions. Not many.",
                    "Sera: If the floor starts glowing, I vote we leave.",
                    "Sera: Ancient ruins have terrible hospitality."
            };
            case "Bram the Baker" -> new String[]{
                    "Bram: Bread first. Existential dread second.",
                    "Bram: I baked enough for everyone. Even the mysterious stranger.",
                    "Bram: If the apocalypse arrives before breakfast, I'm blaming the clock."
            };
            case "Nessa the Blacksmith" -> new String[]{
                    "Nessa: Don't lean on the anvil.",
                    "Nessa: If that thing underground needs a sword, it can wait its turn.",
                    "Nessa: I fix tools. I don't fix curses."
            };
            default -> new String[]{
                    name + ": Something feels different today."
            };
        };
        int index = CURSOR.merge(key, 1, Integer::sum) - 1;
        return lines[index % lines.length];
    }
}
