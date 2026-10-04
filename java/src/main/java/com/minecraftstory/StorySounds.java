package com.minecraftstory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Registry;

import java.util.Map;

public final class StorySounds {
    private StorySounds() {}

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("minecraftstory", path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id,
                SoundEvent.createVariableRangeEvent(id));
    }

    public static final SoundEvent VOICE_COLD_OPEN = register("voice.cold_open");
    public static final SoundEvent VOICE_HAVENFALL = register("voice.havenfall");
    public static final SoundEvent VOICE_CHAPEL = register("voice.chapel");
    public static final SoundEvent VOICE_MISSING_SOUND = register("voice.missing_sound");
    public static final SoundEvent VOICE_SILENT_FOREST = register("voice.silent_forest");
    public static final SoundEvent VOICE_OBSERVATORY = register("voice.observatory");
    public static final SoundEvent VOICE_FIRST_CHOICE = register("voice.first_choice");
    public static final SoundEvent VOICE_DOOR_BELOW = register("voice.door_below");
    public static final SoundEvent VOICE_HEART = register("voice.heart");
    public static final SoundEvent VOICE_ENDING = register("voice.ending");
    public static final SoundEvent VOICE_POST_CREDITS = register("voice.post_credits");

    private static final Map<String, SoundEvent> BY_SCENE = Map.ofEntries(
            Map.entry("cold_open", VOICE_COLD_OPEN),
            Map.entry("havenfall", VOICE_HAVENFALL),
            Map.entry("chapel", VOICE_CHAPEL),
            Map.entry("missing_sound", VOICE_MISSING_SOUND),
            Map.entry("silent_forest", VOICE_SILENT_FOREST),
            Map.entry("observatory", VOICE_OBSERVATORY),
            Map.entry("first_choice", VOICE_FIRST_CHOICE),
            Map.entry("door_below", VOICE_DOOR_BELOW),
            Map.entry("heart", VOICE_HEART),
            Map.entry("ending", VOICE_ENDING),
            Map.entry("post_credits", VOICE_POST_CREDITS)
    );

    public static void register() {
        // Static initialization registers the events. This method intentionally exists
        // so the common initializer has an explicit audio initialization step.
    }

    public static SoundEvent forScene(String sceneId) {
        return BY_SCENE.get(sceneId);
    }

    public static Identifier asset(String sceneId) {
        return Identifier.fromNamespaceAndPath("minecraftstory", "sounds/voice/" + sceneId + ".ogg");
    }
}
