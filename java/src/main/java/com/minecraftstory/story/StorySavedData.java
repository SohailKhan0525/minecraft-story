package com.minecraftstory.story;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class StorySavedData extends SavedData {
    private static final Codec<StoryPlayerData> PLAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("activeQuest").forGetter(StoryPlayerData::activeQuest),
            Codec.INT.optionalFieldOf("questProgress", 0).forGetter(StoryPlayerData::questProgress),
            Codec.STRING.listOf().optionalFieldOf("flags", java.util.List.of()).forGetter(data -> data.flags().stream().toList()),
            Codec.STRING.listOf().optionalFieldOf("completedQuests", java.util.List.of()).forGetter(data -> data.completedQuests().stream().toList())
    ).apply(instance, (quest, progress, flags, completed) ->
            new StoryPlayerData(quest, progress, Set.copyOf(flags), Set.copyOf(completed))));

    private static final Codec<StorySavedData> CODEC = Codec.unboundedMap(
            Codec.STRING, PLAYER_CODEC
    ).xmap(
            map -> new StorySavedData(new HashMap<>(map)),
            data -> data.players
    );

    private static final SavedDataType<StorySavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("minecraftstory", "story"),
            StorySavedData::new,
            CODEC,
            null
    );

    private final Map<String, StoryPlayerData> players;

    public StorySavedData() {
        this.players = new HashMap<>();
    }

    private StorySavedData(Map<String, StoryPlayerData> players) {
        this.players = players;
    }

    public static StorySavedData get(MinecraftServer server) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) return new StorySavedData();
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public StoryState load(UUID playerId) {
        StoryState state = new StoryState();
        StoryPlayerData data = players.get(playerId.toString());
        if (data == null) return state;
        if (!data.activeQuest().isBlank()) state.setActiveQuest(data.activeQuest());
        state.setQuestProgress(data.questProgress());
        for (String name : data.flags()) {
            try { state.set(StoryFlag.valueOf(name)); } catch (IllegalArgumentException ignored) {}
        }
        for (String questId : data.completedQuests()) {
            state.completeQuest(questId);
        }
        return state;
    }

    public void save(UUID playerId, StoryState state) {
        Set<String> flags = new HashSet<>();
        for (StoryFlag flag : state.snapshot()) flags.add(flag.name());
        players.put(playerId.toString(), new StoryPlayerData(
                state.activeQuest(),
                state.questProgress(),
                flags,
                state.completedQuests()
        ));
        setDirty();
    }

    private record StoryPlayerData(
            String activeQuest,
            int questProgress,
            Set<String> flags,
            Set<String> completedQuests
    ) {}
}
