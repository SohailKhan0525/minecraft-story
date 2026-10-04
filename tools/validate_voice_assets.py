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

def scene_position(source: str, scene_id: str) -> int:
    positions = [p for p in (source.find(f'scene("{scene_id}"'), source.find(f'SCENES.put("{scene_id}"')) if p >= 0]
    if not positions:
        raise SystemExit(f"Missing dialogue scene: {scene_id}")
    return min(positions)

def source_scene(source: str, scene_id: str, all_scene_ids: list[str]) -> list[tuple[str, str]]:
    start = scene_position(source, scene_id)
    declaration_end = max(
        start + len(f'scene("{scene_id}"'),
        start + len(f'SCENES.put("{scene_id}"')
    )
    next_positions = [scene_position(source, other) for other in all_scene_ids if other != scene_id and scene_position(source, other) > start]
    end = min(next_positions) if next_positions else len(source)
    block = source[declaration_end:end]
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
    scenes_data = json.loads(SCENES_MANIFEST.read_text(encoding="utf-8"))
    chapter1 = list(scenes_data.get("chapter1", []))
    chapter2 = list(scenes_data.get("chapter2", []))
    expected_ch1 = ["cold_open","havenfall","chapel","missing_sound","silent_forest","observatory","first_choice","door_below","heart","ending","post_credits"]
    if chapter1 != expected_ch1 or chapter2 != ["chapter2_opening"]:
        raise SystemExit("Canonical voice scene manifest is incomplete or out of order.")

    profiles = json.loads(PROFILES.read_text(encoding="utf-8"))
    characters = profiles.get("characters", {})
    settings = profiles.get("engine_arguments", {})
    if profiles.get("schema") != 1 or profiles.get("engine") != "espeak":
        raise SystemExit("Invalid canonical voice profile manifest.")
    if not {"amplitude", "gap", "sample_rate"}.issubset(settings) or int(settings["sample_rate"]) != 22050:
        raise SystemExit("Invalid canonical voice engine settings.")

    source1 = CHAPTER_1.read_text(encoding="utf-8")
    speakers = set()
    for scene in chapter1:
        speakers.update(s for s, _ in source_scene(source1, scene, chapter1))
    source2 = CHAPTER_2.read_text(encoding="utf-8")
    speakers.update(unescape(m.group(1)) for m in LINE_NEW_DECL.finditer(source2))
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
