# Chapter 1 — Implementation Plan

## Story state
The Java implementation will store chapter progress and persistent choices as named story flags.

## Runtime systems
1. Chapter state manager
2. Quest progression manager
3. Dialogue scene definitions
4. Choice handling
5. NPC interaction hooks
6. Cinematic event controller
7. Encounter state controller
8. Chapter completion persistence

## First playable vertical slice
The first playable slice is intentionally cinematic rather than a generic quest test:

Wake by river → bell → Havenfall gate → Mara conversation → first choice → village entry → save story flag.

## Second vertical slice
Havenfall → Chapel → Cael recognizes scar → northern edge → blue fire → first supernatural voice.

## Third vertical slice
Elias → impossible map → Silent Forest → observatory entrance → first ancient vision.

## Production order
Story state first, then dialogue, then quest logic, then NPCs, then cinematics, then encounters, then assets/audio, then polish and QA.

## Quality bar
No generic placeholder dialogue, placeholder quest names, or empty story beats are acceptable in the Chapter 1 release branch. Temporary engineering stubs may exist internally, but they must be replaced before release.
