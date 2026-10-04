# Architecture

## Core principle

The project has one shared game design/story specification and separate platform implementations.

~~~text
                    STORY / DESIGN
                         |
              +----------+----------+
              |                     |
        JAVA IMPLEMENTATION    BEDROCK IMPLEMENTATION
          Fabric mod             Bedrock add-on
              |                     |
              +----------+----------+
                         |
                 Same story vision
~~~

## Shared concepts

- characters
- dialogue
- quests
- objectives
- rewards
- story flags
- chapter progression
- locations
- encounters
- lore
- item specifications
- enemy specifications
- boss phases
- voice direction
- audio scene IDs

## Java

Initial target:

- Minecraft Java Edition 26.3
- Fabric Loader 0.19.5
- Fabric API 0.161.0+26.3
- JDK 25
- Gradle 9.6.0

Custom Chapter 1 sound events are registered in the common source set and played by the client sound manager.

## Bedrock

Bedrock uses a behavior pack plus resource pack. The behavior pack drives story state, choices and objectives; the resource pack contains the scene OGGs and sounds/sound_definitions.json.

## Repository organization

- java/ — Java/Fabric implementation
- bedrock/ — Bedrock implementation
- audio/ — voice policy and generated voice documentation
- docs/ — design and player/developer documentation
- tools/ — voice generation and packaging tools

## Build pipeline

The Audio and playable builds workflow generates voice audio before building Java, packages Bedrock, uploads playable artifacts, and commits generated OGG files to main.

## Versioning

Each supported Minecraft version is an explicit compatibility target. Never silently assume that a mod or add-on built for one release works on another.
