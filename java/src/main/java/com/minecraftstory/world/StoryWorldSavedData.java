package com.minecraftstory.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class StoryWorldSavedData extends SavedData {
    private static final Codec<StoryWorldSavedData> CODEC = Codec.BOOL.xmap(
            StoryWorldSavedData::new,
            data -> data.built
    );

    private static final SavedDataType<StoryWorldSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("minecraftstory", "world"),
            StoryWorldSavedData::new,
            CODEC,
            null
    );

    private boolean built;

    public StoryWorldSavedData() {
        this(false);
    }

    private StoryWorldSavedData(boolean built) {
        this.built = built;
    }

    public static StoryWorldSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean built() {
        return built;
    }

    public void markBuilt() {
        if (!built) {
            built = true;
            setDirty();
        }
    }
}
