# Bedrock Edition

This folder contains the Bedrock runtime for Minecraft Story.

## Current build

The behavior pack contains a playable scripted vertical slice of Chapter 1: opening, Mara dialogue choices, blue-fire objective, Elias encounter, Sera choice, Heart choice, credits, Mira epilogue and the Chapter 2 coming-soon ending.

It is intentionally separate from the Java Fabric mod. Full Bedrock parity for the physical quest world, custom entities, combat phases, audio and cinematic camera work remains the next port milestone.

## Install

Import the behavior-pack manifest as a Bedrock Add-On. Microsoft documents `.mcpack`/`.mcworld` import on Windows, Android and iOS, plus supported console/Realm workflows.

The script currently targets stable Bedrock Script API versions `@minecraft/server 2.10.0` and `@minecraft/server-ui 2.2.0`.

For a polished public release, package this folder as a `.mcpack` and test it on the target Bedrock release before distribution.
