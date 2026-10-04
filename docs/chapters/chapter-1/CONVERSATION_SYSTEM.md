# Chapter 1 — Conversation System

## Conversation layers

### Layer 1 — Main dialogue
Cinematic authored scenes with player choices.

### Layer 2 — Reactive dialogue
NPC lines change according to story flags.

### Layer 3 — Ambient dialogue
Short NPC-to-NPC conversations running in the background.

### Layer 4 — Contextual barks
Lines triggered by location, combat, weather, time, nearby events, or player actions.

### Layer 5 — Companion dialogue
Sera can comment on exploration and react to choices if rescued.

## Interruptions

NPCs can interrupt one another.

Example:

Elias:
"I have a theory."

Mara:
"No."

Elias:
"You don't even know the theory."

Mara:
"I know you."

This should feel authored, not procedurally assembled nonsense.

## Player control

During ordinary dialogue:
- movement remains available where appropriate
- camera can remain player-controlled
- important lines can briefly focus the speaker
- player choices pause the conversation but do not freeze the whole world unnecessarily

During major cinematics:
- camera control can temporarily become authored
- scene transitions should be short
- player returns to control immediately after the dramatic beat

## Anti-repetition

NPC lines have cooldowns and alternate pools.

The same ambient exchange should not repeat every time the player walks past.

Important story lines are one-time unless deliberately replayable.
