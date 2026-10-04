#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import subprocess
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
PROFILES = ROOT / "audio/voice_profiles.json"
CHAPTER_1 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter1Content.java"
CHAPTER_2 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter2Content.java"
JAVA = ROOT / "java/src/main/resources/assets/minecraftstory/sounds/voice"
BED = ROOT / "bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice"

SCENE_RE = re.compile(r'(?m)^\s*(?:scene|SCENES\\.put)\("([^"]+)"')
CH1_LINE_RE = re.compile(r'l\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)",')
CH2_LINE_RE = re.compile(r'new Line\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)"\)')

def unescape(value: str) -> str:
    return value.replace('\\\\', '\\').replace('\\\"', '"')

def discover_scenes() -> tuple[list[str], set[str]]:
    source = CHAPTER_1.read_text(encoding="utf-8")
    decls = list(SCENE_RE.finditer(source))
    scenes = []
    speakers = set()
    for i, decl in enumerate(decls):
        start = decl.end()
        end = decls[i + 1].start() if i + 1 < len(decls) else len(source)
        block = source[start:end]
        lines = [(unescape(m.group(1)), unescape(m.group(2))) for m in CH1_LINE_RE.finditer(block)]
        if lines:
            scenes.append(decl.group(1))
            speakers.update(s for s, _ in lines)

    ch2 = CHAPTER_2.read_text(encoding="utf-8")
    ch2_lines = [(unescape(m.group(1)), unescape(m.group(2))) for m in CH2_LINE_RE.finditer(ch2)]
    if ch2_lines:
        scenes.append("chapter2_opening")
        speakers.update(s for s, _ in ch2_lines)
    return scenes, speakers

def check_ogg(path: Path) -> None:
    if not path.exists() or path.stat().st_size < 1024:
        raise SystemExit(f"Missing or empty voice file: {path}")
    r = subprocess.run(
        ["ffprobe", "-v", "error", "-select_streams", "a:0",
         "-show_entries", "stream=codec_name,channels,sample_rate",
         "-of", "csv=p=0", str(path)],
        text=True, capture_output=True, check=True
    )
    codec, channels, sample_rate = r.stdout.strip().split(",")
    if codec != "vorbis" or channels != "1" or sample_rate != "22050":
        raise SystemExit(f"Expected mono 22050 Hz OGG Vorbis: {path} ({r.stdout.strip()})")

def main() -> int:
    profiles = json.loads(PROFILES.read_text(encoding="utf-8"))
    characters = profiles.get("characters", {})
    settings = profiles.get("engine_arguments", {})
    if profiles.get("schema") != 1 or profiles.get("engine") != "espeak":
        raise SystemExit("Invalid canonical voice profile manifest.")
    if not {"amplitude", "gap", "sample_rate"}.issubset(settings):
        raise SystemExit("Canonical voice profile manifest is missing engine settings.")
    if int(settings["sample_rate"]) != 22050:
        raise SystemExit("Canonical voice sample rate must remain 22050 Hz.")
    scenes, speakers = discover_scenes()

    missing_profiles = sorted(speakers - set(characters))
    if missing_profiles:
        raise SystemExit("Missing canonical voice profiles: " + ", ".join(missing_profiles))

    for scene in scenes:
        check_ogg(JAVA / f"{scene}.ogg")
        check_ogg(BED / f"{scene}.ogg")

    print(f"Validated {len(scenes)} scene performances across both chapters and {len(speakers)} shared character voices.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
