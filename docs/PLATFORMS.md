# Platform Support

## Goal

The project has one canonical Chapter 1 story specification and separate implementations for Minecraft Java Edition and Bedrock Edition.

| Device / edition | Distribution | Chapter 1 | Voice audio |
|---|---|---|---|
| Windows Java | Fabric mod JAR | Implemented target | Generated scene performances |
| Linux Java | Fabric mod JAR | Implemented target | Generated scene performances |
| macOS Java | Fabric mod JAR | Implemented target | Generated scene performances |
| Windows Bedrock | Add-on (.mcaddon) | Implemented runtime target | Resource-pack scene performances |
| Android Bedrock | Add-on (.mcaddon) | Implemented runtime target | Resource-pack scene performances |
| iPhone/iPad Bedrock | Add-on (.mcaddon) | Implemented runtime target | Resource-pack scene performances |
| Xbox / PlayStation / Switch | Bedrock add-on/world/Realm workflow | Platform-specific validation required | Resource-pack audio is part of the design |

## Why there are separate files

Java mods and Bedrock add-ons are different systems. A Fabric JAR cannot be dropped into Bedrock, and a Bedrock add-on cannot be installed as a Java mod.

The project shares:

- story canon
- characters
- dialogue
- choices
- quest names
- persistent story flags
- chapter order
- audio source text

but uses platform-specific runtime code.

## Voice distribution

tools/generate_story_audio.py reads the canonical Java dialogue file and renders mono OGG Vorbis scene performances.

Java output:

java/src/main/resources/assets/minecraftstory/sounds/voice/

Bedrock output:

bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice/

The audio is synthetic original speech, not a cloned or recognizable actor voice. A future human-recorded cast can replace the files without changing the story runtime.

## Build artifacts

The GitHub Actions Audio and playable builds workflow:

1. generates Chapter 1 voice audio;
2. packages the Java mod;
3. packages the Bedrock .mcaddon;
4. publishes downloadable workflow artifacts;
5. commits generated voice resources to main.

The workflow does not create development branches.

## Current support truth

Java 26.3 + Fabric is the primary implementation.

Bedrock includes:

- scripted Chapter 1 progression;
- dialogue choices;
- generated voice playback;
- resource-pack sound definitions;
- the Heart interaction gate;
- credits and Mira epilogue;
- a Chapter 2 coming-soon endpoint.

Console-specific import/distribution rules still need real-device validation and cannot be honestly certified from source review alone.
