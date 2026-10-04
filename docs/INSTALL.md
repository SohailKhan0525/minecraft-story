# Minecraft Story — Install & Play

## Download

Open the latest stable GitHub Release.

- Java Edition: download the Minecraft Story `.jar`.
- Bedrock Edition: download the Minecraft Story `.mcaddon`.
- Bedrock fallback: the same release also contains separate Behavior Pack and Resource Pack `.mcpack` files.

Voice audio is already bundled. There is no separate audio package.

## Java Edition

Minecraft Java 26.3 with Fabric Loader 0.19.5 and Fabric API 0.161.0+26.3 is required.

Put the downloaded `.jar` and Fabric API in your `.minecraft/mods` folder, launch the Fabric 26.3 profile, create a new world, and start playing.

Story NPCs are persistent and the world contains a visible `CHAPTER 1` block title.

## Bedrock Edition

Open the downloaded `.mcaddon` with Minecraft Bedrock and let Minecraft import it.

Create a new world and enable the imported Minecraft Story behavior/resource packs.

If your device does not register both packs from the `.mcaddon`, import the matching behavior `.mcpack` and resource `.mcpack` separately from the same release.

The voice pack and story scripts are included in the release.

## Mobile

Android and iPhone/iPad use the Bedrock `.mcaddon` import flow. The individual `.mcpack` files are available as a fallback.

## Consoles

Xbox, PlayStation and Switch use the Bedrock edition. Console add-on access depends on the supported Bedrock world/Realm workflow for that platform.

## Chapter 1

**The Night the Sky Broke**

Chapter 1 includes the opening, Havenfall, Blue Fire, Elias's trail, the Silent Forest, the Observatory, Sera's choice, relic puzzles, the Hollow Knight phases, the Heart choice, ending, credits, and Mira's post-credit scene.

The endpoint remains:

**TO BE CONTINUED — CHAPTER 2 COMING SOON**

Chapter 2 remains locked.

## Voices

All story dialogue uses one canonical `audio/voice_profiles.json` profile table. The same character has the same generated voice parameters across Chapter 1 and Chapter 2.

The release pipeline regenerates and validates the audio before packaging it.

## Respawn

Java and Bedrock restore the current Chapter 1 checkpoint after a death. Story progress is persisted per player.

## Updating

Use a fresh world when installing a new release. Existing Java story worlds receive non-destructive world-revision fixes where possible.
