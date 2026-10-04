#!/usr/bin/env python3
"""Generate Chapter 1 synthetic voice performances from the canonical Java dialogue file.

Requires: espeak, ffmpeg, Python 3.
Outputs mono OGG Vorbis files for the Java resource namespace and the Bedrock resource pack.
"""

from __future__ import annotations

import re
import shutil
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "java/src/main/java/com/minecraftstory/story/Chapter1Content.java"
JAVA_OUT = ROOT / "java/src/main/resources/assets/minecraftstory/sounds/voice"
BEDROCK_OUT = ROOT / "bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice"

PROFILES = {
    "Narrator": ("en-sc", 125, 28),
    "Wanderer": ("en-us", 145, 42),
    "Mara": ("en-us+f2", 150, 55),
    "Elias": ("en-us+f3", 175, 68),
    "Cael": ("en-gb", 128, 32),
    "Sera": ("en-us+f4", 158, 62),
    "Bram": ("en-us+f5", 145, 72),
    "Nessa": ("en-us+f3", 150, 48),
    "Pip": ("en-us+f4", 178, 78),
    "Toma": ("en-us+f3", 182, 82),
    "Lio": ("en-us", 130, 40),
    "Old Renn": ("en-sc", 108, 22),
    "Hollow Knight": ("en-gb", 92, 12),
    "Black Crystal": ("en-us", 105, 18),
    "Warden of Deep": ("en-sc", 78, 8),
    "Unknown Voice": ("en-gb", 88, 10),
    "Mira": ("en-us+f2", 165, 70),
}

SCENE_DECL = re.compile(r'(?m)^\s*(?:scene|SCENES\\.put)\\("([^"]+)"')
LINE_DECL = re.compile(r'l\\("((?:\\\\.|[^"])*)",\\s*"((?:\\\\.|[^"])*)",')

def unescape_java(value: str) -> str:
    return value.replace('\\\\', '\\\\').replace('\\\"', '"')

def parse_scenes() -> list[tuple[str, list[tuple[str, str]]]]:
    source = SOURCE.read_text(encoding="utf-8")
    decls = list(SCENE_DECL.finditer(source))
    scenes: list[tuple[str, list[tuple[str, str]]]] = []
    for i, decl in enumerate(decls):
        start = decl.end()
        end = decls[i + 1].start() if i + 1 < len(decls) else len(source)
        block = source[start:end]
        lines = [(unescape_java(m.group(1)), unescape_java(m.group(2))) for m in LINE_DECL.finditer(block)]
        if lines:
            scenes.append((decl.group(1), lines))
    return scenes

def require_tool(name: str) -> None:
    if shutil.which(name) is None:
        raise SystemExit(f"Missing required tool: {name}")

def synthesize(text: str, speaker: str, wav_path: Path) -> None:
    voice, rate, pitch = PROFILES.get(speaker, ("en-us", 150, 50))
    subprocess.run([
        "espeak", "-v", voice, "-s", str(rate), "-p", str(pitch),
        "-a", "92", "-g", "3", "-w", str(wav_path), text
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

def generate_scene(scene_id: str, lines: list[tuple[str, str]]) -> int:
    with tempfile.TemporaryDirectory(prefix="minecraft-story-voice-") as tmp:
        tmpdir = Path(tmp)
        silence = tmpdir / "silence.wav"
        with wave.open(str(silence), "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(22050)
            w.writeframes(b"\\0\\0" * int(22050 * 0.18))

        parts: list[Path] = []
        for i, (speaker, text) in enumerate(lines):
            line = tmpdir / f"{i:03d}.wav"
            synthesize(text, speaker, line)
            parts.extend([line, silence])

        combined = tmpdir / "combined.wav"
        concat_wavs(parts, combined)

        JAVA_OUT.mkdir(parents=True, exist_ok=True)
        BEDROCK_OUT.mkdir(parents=True, exist_ok=True)
        java_file = JAVA_OUT / f"{scene_id}.ogg"
        subprocess.run([
            "ffmpeg", "-y", "-loglevel", "error", "-i", str(combined),
            "-ac", "1", "-ar", "22050", "-c:a", "libvorbis", "-q:a", "3",
            str(java_file)
        ], check=True)
        shutil.copy2(java_file, BEDROCK_OUT / java_file.name)
        return java_file.stat().st_size

def main() -> int:
    require_tool("espeak")
    require_tool("ffmpeg")
    scenes = parse_scenes()
    if not scenes:
        raise SystemExit(f"No scenes were parsed from {SOURCE}")

    total = 0
    print(f"Generating {len(scenes)} scene voice performances...")
    for scene_id, lines in scenes:
        size = generate_scene(scene_id, lines)
        total += size
        print(f"  {scene_id}: {len(lines)} lines, {size:,} bytes")
    print(f"Done. Generated {total:,} bytes of OGG voice assets.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
