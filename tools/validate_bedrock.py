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

    if not any(d.get("uuid") == rp["header"]["uuid"] and d.get("version") == bp["header"]["version"] for d in bp.get("dependencies", [])):
        raise SystemExit("Behavior pack does not depend on the exact voice resource-pack version.")

    native = {d.get("module_name"): d.get("version") for d in bp.get("dependencies", []) if d.get("module_name")}
    if native.get("@minecraft/server") != "2.10.0":
        raise SystemExit("Behavior pack is missing @minecraft/server 2.10.0.")
    if native.get("@minecraft/server-ui") != "2.2.0":
        raise SystemExit("Behavior pack is missing @minecraft/server-ui 2.2.0.")

    script_modules = [m for m in bp["modules"] if m.get("type") == "script"]
    if len(script_modules) != 1 or script_modules[0].get("entry") != "scripts/main.js":
        raise SystemExit("Bedrock behavior pack script module is invalid.")

    if not any(m.get("type") == "resources" for m in rp["modules"]):
        raise SystemExit("Bedrock resource pack has no resources module.")

    expected = ["cold_open","havenfall","chapel","missing_sound","silent_forest",
                "observatory","first_choice","door_below","heart","ending","post_credits",
                "chapter2_opening"]
    for scene in expected:
        key = "minecraftstory:voice." + scene
        if key not in sd.get("sound_definitions", {}):
            raise SystemExit("Missing sound definition: " + key)

    profiles = json.loads((ROOT / "audio/voice_profiles.json").read_text(encoding="utf-8"))
    if profiles.get("schema") != 1 or not profiles.get("characters"):
        raise SystemExit("Shared voice profile manifest is missing or invalid.")

    js = (BP / "scripts/main.js").read_text(encoding="utf-8")
    if "playSound" not in js or "minecraftstory:voice." not in js:
        raise SystemExit("Bedrock runtime is missing player-local voice playback.")
    for npc_id in ["mara","elias","cael","sera","bram","nessa","pip","toma","lio","renn"]:
        if f"minecraftstory_npc:{npc_id}" not in js:
            raise SystemExit(f"Bedrock runtime is missing NPC tag: {npc_id}")

    print("Bedrock pack validation: OK")
    return 0

if __name__ == "__main__":
    sys.exit(main())
