# Platform Strategy

## Java Edition

Java is the first implementation because it gives us the most flexible programming environment for the story systems we want to prototype.

Initial target:

**Minecraft Java Edition 26.3 + Fabric**

Development references:

- Fabric developer documentation
- Fabric project templates
- Fabric API
- Minecraft Java Edition release notes

## Bedrock Edition

Bedrock is a separate implementation.

Target devices include:

- Windows
- Android
- iOS/iPadOS
- consoles where the supported add-on format permits

We will keep the shared story and design data platform-neutral so Chapter 1 can be ported without redesigning the narrative.

## Important constraint

Java mods and Bedrock add-ons are not the same technology.

We should therefore design:

- one story
- one content specification
- two platform implementations

rather than promising a single binary that runs everywhere.
