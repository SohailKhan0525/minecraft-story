package com.minecraftstory.world;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class StoryWorldRevisionSavedData extends SavedData {
    private static final Codec<StoryWorldRevisionSavedData> CODEC =
            Codec.INT.xmap(StoryWorldRevisionSavedData::new, data -> data.revision);

    private static final SavedDataType<StoryWorldRevisionSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("minecraftstory", "world_revision"),
            StoryWorldRevisionSavedData::new,
            CODEC,
            null
    );

    private int revision;

    public StoryWorldRevisionSavedData() {
        this(0);
    }

    private StoryWorldRevisionSavedData(int revision) {
        this.revision = revision;
    }

    public static StoryWorldRevisionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int revision() {
        return revision;
    }

    public void setRevision(int revision) {
        if (this.revision != revision) {
            this.revision = revision;
            setDirty();
        }
    }
}
