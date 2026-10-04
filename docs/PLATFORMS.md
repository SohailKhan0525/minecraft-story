# Platform Strategy

## Release model

The project has one narrative specification and separate platform runtimes.

| Platform | Runtime | Status |
|---|---|---|
| Windows/Linux/macOS | Minecraft Java 26.3 + Fabric | **Active Chapter 1 implementation** |
| Android | Minecraft Bedrock add-on | **Bedrock vertical slice added; parity in progress** |
| iPhone/iPad | Minecraft Bedrock add-on | **Bedrock vertical slice added; parity in progress** |
| Xbox/PlayStation/Switch | Minecraft Bedrock add-on via supported world/Realm workflows | **Bedrock vertical slice; parity in progress** |

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

> Java/Fabric has the full Chapter 1 implementation. Bedrock now has a real scripted vertical slice with touch/controller-friendly dialogue and the Chapter 1 ending flow; full Chapter 1 parity is still in progress.