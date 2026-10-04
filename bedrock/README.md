# Bedrock Edition

This folder contains the Minecraft Bedrock Edition runtime for Minecraft Story.

## Chapter 1

Chapter 1 — **The Night the Sky Broke** — now includes:

- scripted opening and story progression;
- Mara and Sera choice forms;
- physical story locations;
- Heart defense/memory flow;
- final crystal choice;
- credits and Mira post-credit scene;
- Chapter 2 coming-soon endpoint;
- generated spoken voice audio through a Bedrock resource pack.

The Bedrock implementation is separate from the Java Fabric mod because the two Minecraft editions use different runtime systems.

## Packs

Behavior pack:

`bedrock/behavior_packs/minecraft_story/`

Resource pack:

`bedrock/resource_packs/minecraft_story/`

The behavior pack declares the voice resource-pack dependency. The resource pack contains the OGG performances and sound definitions.

## Install

For a normal player, use the generated `.mcaddon` artifact from the repository's **Audio and playable builds** workflow.

1. Open the `.mcaddon` file with Minecraft Bedrock.
2. Let Minecraft import both packs.
3. Create a new world.
4. Activate **Minecraft Story — The Night the Sky Broke** under Behavior Packs.
5. Activate **Minecraft Story — Chapter 1 Voice Pack** under Resource Packs.
6. Enter the new world.
7. Keep game audio enabled.

Windows, Android and iOS can use the normal Bedrock import path. Console use depends on the supported Bedrock world/add-on distribution workflow on that platform.

## Voice audio

Bedrock calls the player's sound API using custom sound IDs declared by the resource pack. The same scene performances are produced by:

`tools/generate_story_audio.py`

The audio is synthetic original speech, not a human actor recording or a recognizable actor clone.

## Packaging

Run:

```bash
python tools/generate_story_audio.py
python tools/package_bedrock_addon.py
```

This creates `dist/minecraft-story-chapter-1.mcaddon`.

## Current limitation

The Bedrock runtime is designed for Chapter 1 compatibility across Bedrock devices, but console-specific distribution and every hardware/OS combination still require real-device validation. The repository does not claim universal console certification from source review alone.
