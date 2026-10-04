# Chapter 1 Voice Assets

The Chapter 1 voice pack is **synthetic speech generated specifically for this project**. It is not a recording, clone, or imitation of a recognizable actor or public figure.

The canonical dialogue remains in `java/src/main/java/com/minecraftstory/story/Chapter1Content.java`. The generator in `tools/generate_story_audio.py` reads that file and renders the scene performances.

Human voice replacements must use recordings for which the project has the necessary permission and redistribution rights.

## Audio format

Java uses mono OGG Vorbis files under the Fabric resource namespace. Bedrock copies the same performances into its resource pack and exposes them through `sound_definitions.json`.
