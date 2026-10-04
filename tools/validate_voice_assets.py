#!/usr/bin/env python3
from __future__ import annotations
import subprocess
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "java/src/main/resources/assets/minecraftstory/sounds/voice"
BED = ROOT / "bedrock/resource_packs/minecraft_story/sounds/minecraftstory/voice"
SCENES = ["cold_open","havenfall","chapel","missing_sound","silent_forest",
          "observatory","first_choice","door_below","heart","ending","post_credits"]

def check(path: Path) -> None:
    if not path.exists() or path.stat().st_size < 1024:
        raise SystemExit(f"Missing or empty voice file: {path}")
    r = subprocess.run(
        ["ffprobe","-v","error","-select_streams","a:0",
         "-show_entries","stream=codec_name,channels","-of","csv=p=0",str(path)],
        text=True, capture_output=True, check=True
    )
    codec, channels = r.stdout.strip().split(",")
    if codec != "vorbis" or channels != "1":
        raise SystemExit(f"Expected mono OGG Vorbis: {path}")

def main() -> int:
    for scene in SCENES:
        check(JAVA / f"{scene}.ogg")
        check(BED / f"{scene}.ogg")
    print(f"Validated {len(SCENES) * 2} Chapter 1 voice files.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
