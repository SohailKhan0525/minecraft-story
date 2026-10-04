# Platform Strategy

## Release model

The project has one narrative specification and separate platform runtimes.

| Platform | Runtime | Status |
|---|---|---|
| Windows/Linux/macOS | Minecraft Java 26.3 + Fabric | **Active Chapter 1 implementation** |
| Android | Minecraft Bedrock add-on | **Separate port required** |
| iPhone/iPad | Minecraft Bedrock add-on | **Separate port required** |
| Xbox/PlayStation/Switch | Minecraft Bedrock add-on via supported world/Realm workflows | **Separate port required** |

## Why there cannot be one file for every device

Java mods and Bedrock add-ons are different technologies. A Fabric JAR cannot be installed into Bedrock, and a Bedrock MCPACK cannot be installed into Java.

Therefore the project keeps one story, one quest/choice specification, one character/dialogue specification, and separate Java and Bedrock implementations.

## Bedrock portability

Microsoft's official Bedrock Add-On documentation covers installation on Windows, Android, iOS and supported console/Realm workflows. Bedrock scripting can control entities, blocks and custom gameplay, so Chapter 1 can be ported rather than reduced to a text-only story.

Priorities for the Bedrock port:
1. Chapter 1 world layout.
2. Story flags and save state.
3. NPC dialogue and choices.
4. Quest objectives.
5. Combat encounters.
6. Final crystal choices.
7. Credits/post-credits sequence.
8. Mira epilogue.
9. Chapter 2 coming-soon screen.
10. Resource-pack models/textures and mobile-friendly UI.

## Current truth

> Minecraft Story — Chapter 1 is implemented for Java/Fabric, with Bedrock/mobile/console as a separate port target.