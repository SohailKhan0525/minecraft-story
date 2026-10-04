package com.minecraftstory.world;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class Chapter2WorldSavedData extends SavedData {
    private static final Codec<Chapter2WorldSavedData> CODEC = Codec.BOOL.xmap(
            Chapter2WorldSavedData::new,
            data -> data.built
    );
    private static final SavedDataType<Chapter2WorldSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("minecraftstory", "chapter2_world"),
            Chapter2WorldSavedData::new,
            CODEC,
            null
    );
    private boolean built;
    public Chapter2WorldSavedData() { this(false); }
    private Chapter2WorldSavedData(boolean built) { this.built = built; }
    public static Chapter2WorldSavedData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    public boolean built() { return built; }
    public void markBuilt() { if (!built) { built = true; setDirty(); } }
}
