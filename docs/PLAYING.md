# How to Play Chapter 1 — from a clean machine

Minecraft Story is distributed as two runtimes because Minecraft Java Edition and Bedrock Edition use different mod/add-on systems. Use the instructions for the edition you actually play.

## Java Edition — Windows, Linux, macOS

### Player install

You need:

- Minecraft Java Edition 26.3
- Java/JDK 25
- Fabric Loader 0.19.5 for Minecraft 26.3
- Fabric API 0.161.0+26.3
- A Minecraft Story Java build

The easiest path is to use the Java artifact produced by the repository's Audio and playable builds workflow. That build includes the Chapter 1 generated voice resources inside the mod JAR.

1. Install Minecraft Java Edition 26.3 and launch it once.
2. Install Java/JDK 25.
3. Install Fabric Loader 0.19.5 for Minecraft 26.3.
4. Put Fabric API 0.161.0+26.3 in the same .minecraft/mods folder.
5. Put the Minecraft Story JAR in that same mods folder.
6. Launch the Fabric 26.3 profile.
7. Create a new single-player world.
8. Leave Minecraft Master/Player/Voice audio audible.
9. Walk toward Havenfall and follow the quest HUD.

### Build it yourself

From the repository root:

~~~bash
sudo apt-get install espeak ffmpeg
cd java
../tools/generate_story_audio.py
gradle build --stacktrace --warning-mode all
~~~

On Windows, install eSpeak and FFmpeg through your preferred package manager, then run:

~~~powershell
python tools/generate_story_audio.py
cd java
gradle build --stacktrace --warning-mode all
~~~

The generated Java voice files are written to:

java/src/main/resources/assets/minecraftstory/sounds/voice/

and are included in the built JAR.

### Java voice fallback

When a generated audio resource is missing, the mod can fall back to the Minecraft narrator/OS speech for the opening line of a conversation. That fallback is not a recorded actor.

## Bedrock Edition — Windows, Android, iPhone/iPad and supported console workflows

Bedrock uses a behavior pack and a resource pack. The repository contains both.

### Easiest install

Use the minecraft-story-chapter-1.mcaddon artifact from the repository's Audio and playable builds workflow.

1. Download the .mcaddon artifact.
2. Open it with Minecraft Bedrock.
3. Wait for the import to finish.
4. Create a new world.
5. Edit the world before entering it.
6. In Behavior Packs, activate Minecraft Story — The Night the Sky Broke.
7. In Resource Packs, activate Minecraft Story — Chapter 1 Voice Pack.
8. Make sure game audio is audible.
9. Enter the world and follow the on-screen objective.

The story voice performances are player-local. Bedrock's Player.playSound API is designed for sound that only the specific player hears.

### Console note

Xbox, PlayStation and Switch support depends on the Bedrock distribution/workflow available on that device. The repository does not claim that a Java JAR can be installed on a console, and it does not claim that a GitHub download bypasses console content restrictions.

## First-run controls

No keyboard-specific story controls are required.

- Java: normal movement, interaction, inventory and mouse/keyboard controls.
- Bedrock/mobile: normal touch and controller controls.
- Story decisions appear as buttons/forms or dialogue choices.

## Chapter 1 path

1. Cold open and river awakening.
2. Havenfall and Mara's first choice.
3. Blue fire in the chapel.
4. The missing sound and Elias.
5. Silent Forest and Sera.
6. Observatory revelations.
7. Rescue-Sera or archive choice.
8. Three relics and the buried door.
9. Hollow Knight encounter.
10. Memory phase: do not try to defeat the memory; interact with the Heart.
11. Heart Chamber defenses.
12. Final crystal choice.
13. Ending, credits and Mira epilogue.
14. TO BE CONTINUED — CHAPTER 2 COMING SOON.

## Troubleshooting

If voices are missing:

- Java: confirm the generated voice resources are inside the JAR, or run the generator before building.
- Bedrock: confirm both the behavior pack and Chapter 1 Voice Pack resource pack are active.
- Check Minecraft audio sliders and the OS output device.
- After major story-system updates, use a new world.

## Important

There is no single universal Minecraft file. A Fabric JAR is for Java Edition; a Bedrock add-on is for Bedrock Edition.

Chapter 2 remains a coming-soon endpoint after the Chapter 1 ending. It is not automatically started by the Chapter 1 release.
