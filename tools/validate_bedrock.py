#!/usr/bin/env python3
from __future__ import annotations
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
BP = ROOT / "bedrock/behavior_packs/minecraft_story"
RP = ROOT / "bedrock/resource_packs/minecraft_story"

def load(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))

def main() -> int:
    bp = load(BP / "manifest.json")
    rp = load(RP / "manifest.json")
    sd = load(RP / "sounds/sound_definitions.json")

    if not any(d.get("uuid") == rp["header"]["uuid"] for d in bp.get("dependencies", [])):
        raise SystemExit("Behavior pack does not depend on the voice resource pack.")

    script_modules = [m for m in bp["modules"] if m.get("type") == "script"]
    if len(script_modules) != 1 or script_modules[0].get("entry") != "scripts/main.js":
        raise SystemExit("Bedrock behavior pack script module is invalid.")

    if not any(m.get("type") == "resources" for m in rp["modules"]):
        raise SystemExit("Bedrock resource pack has no resources module.")

    expected = ["cold_open","havenfall","chapel","missing_sound","silent_forest",
                "observatory","first_choice","door_below","heart","ending","post_credits"]
    for scene in expected:
        key = "minecraftstory:voice." + scene
        if key not in sd.get("sound_definitions", {}):
            raise SystemExit("Missing sound definition: " + key)

    js = (BP / "scripts/main.js").read_text(encoding="utf-8")
    if "playSound" not in js or "minecraftstory:voice." not in js:
        raise SystemExit("Bedrock runtime is missing player-local voice playback.")

    print("Bedrock pack validation: OK")
    return 0

if __name__ == "__main__":
    sys.exit(main())
