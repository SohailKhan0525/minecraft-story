# Chapter 1 — Voice and Conversation Design

## Voice philosophy

Every important character has a recognizable speaking identity.

The shipped Chapter 1 voice layer uses synthetic original performances so players can actually hear the dialogue while the story runs. It is not a recording or imitation of a recognizable human actor.

## Main voices

### The Wanderer
Grounded, uncertain, restrained. The player remains the viewpoint character, so the performance is intentionally understated.

### Mara Vale
Low, controlled, tired authority. Short sentences. Rarely jokes.

### Elias Venn
Fast, curious, slightly breathless. Humor appears when he is nervous.

### Brother Cael
Soft, measured and deliberate. Pauses feel intentional.

### Sera Voss
Dry, blunt and skeptical. Deadpan timing is important.

### Hollow Knight
Sparse, low and unnatural. Never speaks more than necessary.

### The Warden of the Deep
Slow, command-like and heavy, with an intentionally synthetic edge.

### Mira
Warm, playful and teasing. The post-credit scene should feel lighter than the main horror arc.

## Audio architecture

The canonical dialogue remains in Chapter1Content.java.

tools/generate_story_audio.py:

1. reads the canonical scenes;
2. selects a speaker profile;
3. synthesizes each line;
4. adds short pauses;
5. joins the scene into one performance;
6. exports mono OGG Vorbis;
7. writes the same performance into Java and Bedrock resource locations.

This keeps dialogue and voice assets synchronized.

## Realtime conversation

The runtime is designed to support:

- player choices during dialogue;
- NPC interruption;
- NPC-to-NPC conversations;
- contextual barks;
- reactions to player actions;
- proximity-triggered lines;
- combat barks;
- environmental comments;
- companion comments;
- conditional lines based on story flags.

The shipped Chapter 1 voice layer prioritizes authored main scenes. Ambient NPC barks remain a separate expansion area.

## Human cast replacement

Actual human recordings can replace the synthetic files while keeping the same resource filenames and scene IDs. Any future actor recordings must be original or properly licensed.
