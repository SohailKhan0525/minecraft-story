# Minecraft Story

An original, chapter-based story/RPG project for Minecraft.

## Vision

Build an episodic Minecraft adventure that combines:

- story-driven exploration
- NPCs and dialogue
- quests and objectives
- custom items and abilities
- enemies and boss encounters
- dungeons and puzzles
- cinematic moments
- persistent story progression
- spoken story audio

The long-term goal is to support both Minecraft: Java Edition and Minecraft: Bedrock Edition through platform-specific implementations that share the same story, lore, design, and content specifications.

## Current status

**Chapter 1 implementation in active development.**

Chapter 1: **The Night the Sky Broke**

The Java runtime is the primary implementation. The Bedrock runtime contains a separate playable implementation with touch/controller-friendly story UI and generated voice playback.

The project now includes:

- persistent Chapter 1 quest and story state;
- physical world objectives and combat encounters on Java;
- Bedrock scripted Chapter 1 progression;
- generated synthetic voice performances for the main Chapter 1 scenes;
- Java SoundEvent playback and narrator fallback;
- Bedrock resource-pack sound definitions and player-local voice playback;
- Chapter 1 ending, credits and Mira post-credit epilogue;
- Chapter 2 preserved as a coming-soon endpoint.

## Build / play

See docs/PLAYING.md for a clean-machine installation guide.

See docs/AUDIO.md for the voice/audio pipeline.

See:
- docs/ROADMAP.md
- docs/ARCHITECTURE.md
- docs/PLATFORMS.md
- docs/chapters/chapter-1/README.md

> This is an independent project and is not an official Minecraft product.
