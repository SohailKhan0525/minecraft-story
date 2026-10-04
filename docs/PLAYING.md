# How to Play

## What is playable right now

The current fully implemented runtime is Minecraft Java Edition 26.3 + Fabric.

Chapter 1: **The Night the Sky Broke**

1. Wake beside the river.
2. Follow the river to Havenfall.
3. Meet Mara and make the first dialogue choice.
4. Investigate the chapel's blue fire.
5. Find Elias through the physical clue trail.
6. Enter the Silent Forest and Observatory.
7. Make the Sera choice.
8. Recover the three relics and open the buried door.
9. Survive the Hollow Knight encounter.
10. In the Memory phase, **stop attacking and touch the Heart crystal**.
11. Survive the Heart Chamber.
12. Make the final crystal choice.
13. Escape Havenfall's collapse.
14. Watch the credits.
15. After the credits, meet Mira.
16. Finish the wholesome epilogue.
17. See **TO BE CONTINUED — CHAPTER 2 COMING SOON**.

## Java Edition — PC

Requirements:
- Minecraft Java Edition 26.3
- Java/JDK 25
- Fabric Loader 0.19.5
- Fabric API 0.161.0+26.3
- The built Minecraft Story JAR from java/build/libs/

### Install
1. Install Minecraft Java Edition 26.3.
2. Install Java 25.
3. Install Fabric Loader 0.19.5 for Minecraft 26.3.
4. Install Fabric API 0.161.0+26.3 into the same mods folder.
5. Put the Minecraft Story JAR into that mods folder.
6. Start the Fabric 26.3 profile.
7. Create a **new world** for the story.
8. Enter the world and follow the on-screen objective.

## Building it yourself

    cd java
    gradle build

The JAR is produced in java/build/libs/.

## Android / iOS / console

There is not one Java JAR that can honestly run natively on every Minecraft device.
- Android/iOS/console Minecraft is normally Bedrock Edition.
- Java Edition and Bedrock Edition use different mod/add-on systems.
- Android can sometimes run Java through third-party launchers, but that is not the supported release path.
- Bedrock add-ons can be distributed to Windows, Android, iOS and supported console/Realm setups.

Do not install the Java JAR into Bedrock; it will not work.

## Bedrock status

Bedrock parity is a separate engineering target. The story specification is platform-neutral so the same Chapter 1 can be reproduced in a Bedrock behavior/resource pack.

## Troubleshooting

- Use a fresh world if the story area looks wrong.
- Progress is stored per player.
- If the mod does not load, verify Minecraft 26.3, JDK 25, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, and remove duplicate JARs.

## Development reality

A successful source build is not the same thing as full runtime certification. Test the actual Minecraft 26.3 client/server before calling a release production-ready.