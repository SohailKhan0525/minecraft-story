#!/usr/bin/env python3
"""Generate deterministic story voice performances from canonical dialogue and voice manifests."""

from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROFILES_PATH = ROOT / "audio/voice_profiles.json"
SCENES_PATH = ROOT / "audio/voice_scenes.json"
CHAPTER_1 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter1Content.java"
CHAPTER_2 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter2Content.java"
JAVA_OUT = ROOT / "java/src/main/resources/assets/minecraftstory/sounds/voice"
BEDROCK_OUT = ROOT / "bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice"

LINE_L_DECL = re.compile(r'l\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)",')
LINE_NEW_DECL = re.compile(r'new Line\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)"\)')

def unescape_java(value: str) -> str:
    return value.replace('\\\\', '\\').replace('\\"', '"')

def load_json(path: Path) -> dict:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        raise SystemExit(f"Invalid JSON manifest {path}: {exc}")

def load_profiles() -> tuple[dict, dict]:
    data = load_json(PROFILES_PATH)
    if data.get("schema") != 1 or data.get("engine") != "espeak":
        raise SystemExit("Unsupported voice profile manifest.")
    settings = data.get("engine_arguments", {})
    if not {"amplitude", "gap", "sample_rate"}.issubset(settings):
        raise SystemExit("Voice profile manifest is missing engine arguments.")
    return data["characters"], settings

def load_scene_manifest() -> tuple[list[str], list[str]]:
    data = load_json(SCENES_PATH)
    if data.get("schema") != 1:
        raise SystemExit("Unsupported voice scene manifest.")
    chapter1 = list(data.get("chapter1", []))
    chapter2 = list(data.get("chapter2", []))
    combined = chapter1 + chapter2
    if not chapter1 or not chapter2 or len(set(combined)) != len(combined):
        raise SystemExit("Voice scene manifest is empty or contains duplicates.")
    return chapter1, chapter2

def scene_position(source: str, scene_id: str) -> int:
    positions = [p for p in (source.find(f'scene("{scene_id}"'), source.find(f'SCENES.put("{scene_id}"')) if p >= 0]
    if not positions:
        raise SystemExit(f"Canonical scene missing from Chapter 1 source: {scene_id}")
    return min(positions)

def extract_scene(source: str, scene_id: str, all_scene_ids: list[str]) -> list[tuple[str, str]]:
    start = scene_position(source, scene_id)
    declaration_end = start + len(f'scene("{scene_id}"')
    alt_end = start + len(f'SCENES.put("{scene_id}"')
    declaration_end = max(declaration_end, alt_end)
    next_positions = [scene_position(source, other) for other in all_scene_ids if other != scene_id and scene_position(source, other) > start]
    end = min(next_positions) if next_positions else len(source)
    block = source[declaration_end:end]
    lines = [(unescape_java(m.group(1)), unescape_java(m.group(2))) for m in LINE_L_DECL.finditer(block)]
    if not lines:
        raise SystemExit(f"Canonical scene has no dialogue lines: {scene_id}")
    return lines

def parse_chapter_1(scene_ids: list[str]) -> list[tuple[str, list[tuple[str, str]]]]:
    source = CHAPTER_1.read_text(encoding="utf-8")
    return [(scene_id, extract_scene(source, scene_id, scene_ids)) for scene_id in scene_ids]

def parse_chapter_2(scene_ids: list[str]) -> list[tuple[str, list[tuple[str, str]]]]:
    if "chapter2_opening" not in scene_ids:
        return []
    source = CHAPTER_2.read_text(encoding="utf-8")
    lines = [(unescape_java(m.group(1)), unescape_java(m.group(2))) for m in LINE_NEW_DECL.finditer(source)]
    if not lines:
        raise SystemExit("Chapter 2 opening has no dialogue lines.")
    return [("chapter2_opening", lines)]

def require_tool(name: str) -> None:
    if shutil.which(name) is None:
        raise SystemExit(f"Missing required tool: {name}")

def verify_speakers(scenes: list[tuple[str, list[tuple[str, str]]]], profiles: dict) -> None:
    speakers = sorted({speaker for _, lines in scenes for speaker, _ in lines})
    missing = [speaker for speaker in speakers if speaker not in profiles]
    if missing:
        raise SystemExit("Missing canonical voice profiles for: " + ", ".join(missing))

def synthesize(text: str, speaker: str, wav_path: Path, profiles: dict, settings: dict) -> None:
    profile = profiles[speaker]
    subprocess.run([
        "espeak", "-v", profile["voice"], "-s", str(profile["rate"]),
        "-p", str(profile["pitch"]), "-a", str(settings["amplitude"]),
        "-g", str(settings["gap"]), "-w", str(wav_path), text,
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

def concat_wavs(paths: list[Path], out_path: Path) -> None:
    with wave.open(str(paths[0]), "rb") as first:
        params = first.getparams()
        frames = [first.readframes(first.getnframes())]
    for path in paths[1:]:
        with wave.open(str(path), "rb") as current:
            if current.getparams()[:3] != params[:3]:
                raise RuntimeError("Generated WAV formats do not match.")
            frames.append(current.readframes(current.getnframes()))
    with wave.open(str(out_path), "wb") as out:
        out.setparams(params)
        for frame in frames:
            out.writeframes(frame)

def generate_scene(scene_id: str, lines: list[tuple[str, str]], profiles: dict, settings: dict) -> int:
    with tempfile.TemporaryDirectory(prefix="minecraft-story-voice-") as tmp:
        tmpdir = Path(tmp)
        silence = tmpdir / "silence.wav"
        with wave.open(str(silence), "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(int(settings["sample_rate"]))
            w.writeframes(b"\\0\\0" * int(int(settings["sample_rate"]) * 0.18))
        parts: list[Path] = []
        for i, (speaker, text) in enumerate(lines):
            line = tmpdir / f"{i:03d}.wav"
            synthesize(text, speaker, line, profiles, settings)
            parts.extend([line, silence])
        combined = tmpdir / "combined.wav"
        concat_wavs(parts, combined)
        JAVA_OUT.mkdir(parents=True, exist_ok=True)
        BEDROCK_OUT.mkdir(parents=True, exist_ok=True)
        java_file = JAVA_OUT / f"{scene_id}.ogg"
        subprocess.run([
            "ffmpeg", "-y", "-loglevel", "error", "-i", str(combined),
            "-ac", "1", "-ar", str(settings["sample_rate"]), "-c:a", "libvorbis", "-q:a", "3",
            str(java_file),
        ], check=True)
        shutil.copy2(java_file, BEDROCK_OUT / java_file.name)
        return java_file.stat().st_size

def main() -> int:
    require_tool("espeak")
    require_tool("ffmpeg")
    profiles, settings = load_profiles()
    chapter1_ids, chapter2_ids = load_scene_manifest()
    scenes = parse_chapter_1(chapter1_ids) + parse_chapter_2(chapter2_ids)
    expected = chapter1_ids + chapter2_ids
    actual = [scene_id for scene_id, _ in scenes]
    if actual != expected:
        raise SystemExit(f"Voice scene mismatch: expected {expected}, got {actual}")
    verify_speakers(scenes, profiles)
    total = 0
    print(f"Generating {len(scenes)} deterministic story voice performances...")
    for scene_id, lines in scenes:
        size = generate_scene(scene_id, lines, profiles, settings)
        total += size
        print(f"  {scene_id}: {len(lines)} lines, {size:,} bytes")
    print(f"Done. Generated {total:,} bytes of OGG voice assets.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
