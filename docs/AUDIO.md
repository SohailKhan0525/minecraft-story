# Audio and Voice

## What you will hear

Chapter 1 now uses real audio files rather than subtitle-only dialogue.

The voice layer is made of synthetic spoken performances generated for this project. Each major Chapter 1 scene has a dedicated OGG Vorbis performance with distinct pitch, rate and voice settings for the characters.

The current scenes are:

- The Sky / cold open
- Havenfall
- The Blue Fire
- The Missing Sound
- The Silent Forest
- Beneath the Roots
- The First Choice
- The Door Beneath the World
- The Heart of the Observatory
- The Night Is Not Over
- After the Credits / Mira epilogue

## Character voice direction

Mara — controlled, tired authority.

Elias — fast, curious, breathless.

Cael — soft, measured, secretive.

Sera — dry, blunt, skeptical.

Wanderer — grounded, uncertain, restrained.

Hollow Knight — low, sparse, unnatural.

Black Crystal / Unknown Voice — distant and uncanny.

Warden of Deep — slow, heavy, command-like.

Mira — warm, playful, teasing.

## Generation

Canonical source:

java/src/main/java/com/minecraftstory/story/Chapter1Content.java

Run:

~~~bash
python tools/generate_story_audio.py
~~~

Requirements:

- Python 3
- eSpeak
- FFmpeg

The generator parses the canonical scene lines, synthesizes them speaker-by-speaker, adds short pauses, joins each scene and exports mono OGG Vorbis.

Fabric's custom sound documentation recommends OGG Vorbis and mono audio for Minecraft custom sound assets.

## Runtime behavior

On Java, a dialogue screen plays the matching SoundEvent on the client. If the audio resource is missing, the mod uses Minecraft's narrator as a fallback for the opening line.

On Bedrock, the resource pack declares custom sound IDs in sounds/sound_definitions.json, and the behavior pack calls Player.playSound so the voice is heard by that player.

## Replacing the generated voices

Human recordings can replace the generated OGG files without redesigning the story code. Keep the same filenames and scene IDs.

For replacement recordings:

- use OGG Vorbis;
- use mono audio;
- preserve scene order;
- normalize loudness across characters;
- align performances with the canonical dialogue;
- distribute only recordings for which the project has the necessary rights.

## Important limitation

These are synthetic voices, not human actor recordings. The repository does not claim that an actor has recorded Chapter 1.
