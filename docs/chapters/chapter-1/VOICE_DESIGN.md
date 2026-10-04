# Chapter 1 — Voice and Conversation Design

## Voice philosophy

Every important character has a recognizable speaking identity.

The goal is not simply to make every NPC speak. The goal is to make the player recognize who is speaking even before seeing the name.

## Main voices

### The Wanderer
Player-controlled voice is optional for the player's spoken lines. In the initial implementation, the player can remain mostly silent while choices appear as authored dialogue. This preserves player identity and keeps voice production manageable.

### Mara Vale
Low, controlled, tired authority. Short sentences. Rarely jokes. When she does, the joke lands because she almost never makes one.

### Elias Venn
Fast, curious, slightly breathless. Talks when nervous. Uses humor as a defense mechanism.

### Brother Cael
Soft, measured, deliberate. Long pauses. Sounds like someone carrying a secret.

### Sera Voss
Dry, blunt, skeptical. Her humor is deadpan.

### Hollow Knight
Very sparse. Deep, restrained, unnatural resonance. Never speaks more than necessary.

### The Warden of the Deep
Not a conversational villain. Its voice is layered with stone, distant choir, and mechanical resonance. Most communication is short command-like phrases.

## Voice implementation

The repository will store voice direction as metadata with each major line:

- speaker
- emotion
- intensity
- pacing
- pause markers
- scene context
- whether the line is foreground dialogue or ambient dialogue

Actual voice assets must be original or properly licensed. AI voice generation may be used only with appropriate rights and consent for any recognizable human voice.

## Realtime conversation

The conversation system should support:

- player choices during dialogue
- NPC interruption
- NPC-to-NPC conversations
- contextual barks
- reactions to player actions
- proximity-triggered lines
- combat barks
- environmental comments
- companion comments
- conditional lines based on story flags

## Background conversations

Background NPCs should not feel like quest terminals.

Examples:

Farmer:
"That is the third chicken."

Fisher:
"Third?"

Farmer:
"The same chicken."

Fisher:
"You've been counting?"

Farmer:
"I've been losing."

---

Guard:
"Did you hear that?"

Guard 2:
"No."

Guard:
"Exactly."

---

Villager:
"I heard the chapel bell."

Shopkeeper:
"The chapel bell hasn't rung in twelve years."

Villager:
"...Then what did I hear?"

These conversations can trigger while the player walks past and should never pause gameplay.

## Dynamic reactions

NPCs can react to:

- time of day
- weather
- nearby combat
- blue fire
- player reputation
- chapter progress
- whether Elias is missing
- which first-choice path was selected
- whether Sera was rescued
- final crystal choice

## Performance rule

Ambient dialogue should be short. Most background exchanges should last 4–12 seconds. Important conversations can be longer and interruptible.
