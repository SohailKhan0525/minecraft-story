#!/usr/bin/env python3
"""Generate deterministic story voice performances for every shipped/future chapter scene.

The shared profile table in audio/voice_profiles.json is the only voice identity source.
Chapter 1 and Chapter 2 therefore cannot silently choose different voices for the same
character. Audio is generated in GitHub Actions and bundled into the player releases.
"""

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
CHAPTER_1 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter1Content.java"
CHAPTER_2 = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter2Content.java"
JAVA_OUT = ROOT / "java/src/main/resources/assets/minecraftstory/sounds/voice"
BEDROCK_OUT = ROOT / "bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice"

SCENE_DECL = re.compile(r'(?m)^\s*(?:scene|SCENES\\.put)\("([^"]+)"')
LINE_L_DECL = re.compile(r'l\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)",')
LINE_NEW_DECL = re.compile(r'new Line\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)"\)')

def unescape_java(value: str) -> str:
    return value.replace('\\\\', '\\').replace('\\\"', '"')

def load_profiles() -> tuple[dict, dict]:
    data = json.loads(PROFILES_PATH.read_text(encoding="utf-8"))
    if data.get("schema") != 1 or data.get("engine") != "espeak":
        raise SystemExit("Unsupported voice profile manifest.")
    settings = data.get("engine_arguments", {})
    required = {"amplitude", "gap", "sample_rate"}
    if not required.issubset(settings):
        raise SystemExit("Voice profile manifest is missing engine arguments.")
    return data["characters"], settings

def parse_chapter_1() -> list[tuple[str, list[tuple[str, str]]]]:
    source = CHAPTER_1.read_text(encoding="utf-8")
    decls = list(SCENE_DECL.finditer(source))
    scenes = []
    for i, decl in enumerate(decls):
        start = decl.end()
        end = decls[i + 1].start() if i + 1 < len(decls) else len(source)
        block = source[start:end]
        lines = [(unescape_java(m.group(1)), unescape_java(m.group(2))) for m in LINE_L_DECL.finditer(block)]
        if lines:
            scenes.append((decl.group(1), lines))
    return scenes

def parse_chapter_2() -> list[tuple[str, list[tuple[str, str]]]]:
    source = CHAPTER_2.read_text(encoding="utf-8")
    lines = [(unescape_java(m.group(1)), unescape_java(m.group(2))) for m in LINE_NEW_DECL.finditer(source)]
    dynamic = re.search(r'new Line\("Sera",\s*"You ".*?ending.*?mountain noticed\\."\)', source)
    if dynamic:
        lines.insert(min(3, len(lines)), ("Sera", "You made your choice. The mountain noticed."))
    return [("chapter2_opening", lines)] if lines else []

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
        "espeak",
        "-v", profile["voice"],
        "-s", str(profile["rate"]),
        "-p", str(profile["pitch"]),
        "-a", str(settings["amplitude"]),
        "-g", str(settings["gap"]),
        "-w", str(wav_path),
        text,
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
            w.writeframes(b"\0\0" * int(22050 * 0.18))

        parts = []
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
            str(java_file)
        ], check=True)
        shutil.copy2(java_file, BEDROCK_OUT / java_file.name)
        return java_file.stat().st_size

def main() -> int:
    require_tool("espeak")
    require_tool("ffmpeg")
    profiles, settings = load_profiles()
    scenes = parse_chapter_1() + parse_chapter_2()
    if not scenes:
        raise SystemExit("No story scenes were parsed.")
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
