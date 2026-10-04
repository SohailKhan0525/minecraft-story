#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROFILES = ROOT / "audio/voice_profiles.json"
SCENES_MANIFEST = ROOT / "audio/voice_scenes.json"
CHAPTER_1 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter1Content.java"
CHAPTER_2 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter2Content.java"
JAVA = ROOT / "java/src/main/resources/assets/minecraftstory/sounds/voice"
BED = ROOT / "bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice"

LINE_L_DECL = re.compile(r'l\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)",')
LINE_NEW_DECL = re.compile(r'new Line\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)"\)')

def unescape(value: str) -> str:
    return value.replace('\\\\', '\\').replace('\\"', '"')

def source_scene(source: str, scene_id: str) -> list[tuple[str, str]]:
    start = re.search(r'(?:\bscene|\bSCENES\.put)\("' + re.escape(scene_id) + r'"', source)
    if not start:
        raise SystemExit(f"Missing dialogue scene: {scene_id}")
    remainder = source[start.end():]
    next_match = re.search(r'(?:\bscene|\bSCENES\.put)\("', remainder)
    block = remainder[:next_match.start()] if next_match else remainder
    return [(unescape(m.group(1)), unescape(m.group(2))) for m in LINE_L_DECL.finditer(block)]

def check_ogg(path: Path) -> None:
    if not path.exists() or path.stat().st_size < 1024:
        raise SystemExit(f"Missing or empty voice file: {path}")
    r = subprocess.run([
        "ffprobe", "-v", "error", "-select_streams", "a:0",
        "-show_entries", "stream=codec_name,channels,sample_rate",
        "-of", "csv=p=0", str(path),
    ], text=True, capture_output=True, check=True)
    codec, channels, sample_rate = r.stdout.strip().split(",")
    if codec != "vorbis" or channels != "1" or sample_rate != "22050":
        raise SystemExit(f"Expected mono 22050 Hz OGG Vorbis: {path} ({r.stdout.strip()})")

def main() -> int:
    manifest = json.loads(SCENES_MANIFEST.read_text(encoding="utf-8"))
    chapter1 = list(manifest.get("chapter1", []))
    chapter2 = list(manifest.get("chapter2", []))
    if chapter1 != ["cold_open","havenfall","chapel","missing_sound","silent_forest","observatory","first_choice","door_below","heart","ending","post_credits"]:
        raise SystemExit("Chapter 1 voice scene manifest is incomplete or out of order.")
    if chapter2 != ["chapter2_opening"]:
        raise SystemExit("Chapter 2 voice scene manifest is incomplete or out of order.")

    profiles = json.loads(PROFILES.read_text(encoding="utf-8"))
    characters = profiles.get("characters", {})
    settings = profiles.get("engine_arguments", {})
    if profiles.get("schema") != 1 or profiles.get("engine") != "espeak":
        raise SystemExit("Invalid canonical voice profile manifest.")
    if int(settings.get("sample_rate", 0)) != 22050:
        raise SystemExit("Canonical voice sample rate must remain 22050 Hz.")

    source1 = CHAPTER_1.read_text(encoding="utf-8")
    speakers = set()
    for scene in chapter1:
        speakers.update(s for s, _ in source_scene(source1, scene))

    source2 = CHAPTER_2.read_text(encoding="utf-8")
    speakers.update(s for s, _ in LINE_NEW_DECL.findall(source2))
    missing_profiles = sorted(speakers - set(characters))
    if missing_profiles:
        raise SystemExit("Missing canonical voice profiles: " + ", ".join(missing_profiles))

    all_scenes = chapter1 + chapter2
    for scene in all_scenes:
        check_ogg(JAVA / f"{scene}.ogg")
        check_ogg(BED / f"{scene}.ogg")

    print(f"Validated {len(all_scenes)} scene performances across both chapters and {len(speakers)} shared character voices.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
