# Architecture

## Core principle

The project has one shared **game design/story specification** and separate platform implementations.

```
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
```

## Shared concepts

The following concepts should remain platform-neutral:

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
- cutscene/event specifications

## Java

Initial target:

- Minecraft Java Edition 26.3
- Fabric
- Java
- Gradle

Fabric's current 26.3 guidance specifies Loom 1.17, Gradle 9.6.0, and Fabric Loader 0.19.5 at the time of its September 15, 2026 release notes.

## Bedrock

Bedrock will use Minecraft's add-on/resource-pack systems where possible. We will port shared design specifications rather than attempting to force Java code to run on Bedrock.

## Repository organization

- `java/` — Java/Fabric implementation
- `bedrock/` — Bedrock implementation
- `docs/` — design and development documentation
- `assets/` — source assets that are safe to distribute
- `tools/` — optional project tooling

## Versioning

Each supported Minecraft version should be treated as an explicit compatibility target. Avoid silently assuming that a mod built for one release works on another.
