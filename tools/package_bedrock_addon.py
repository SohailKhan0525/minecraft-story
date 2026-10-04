#!/usr/bin/env python3
"""Package the Bedrock behavior/resource packs into .mcpack and .mcaddon files."""

from __future__ import annotations

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile
import shutil
import json

ROOT = Path(__file__).resolve().parents[1]
BP = ROOT / "bedrock/behavior_packs/minecraft_story"
RP = ROOT / "bedrock/resource_packs/minecraft_story"
DIST = ROOT / "dist"

BP_MANIFEST = BP / "manifest.json"
RP_MANIFEST = RP / "manifest.json"
SOUND_DEFS = RP / "sounds/sound_definitions.json"
VOICE_DIR = RP / "sounds/minecraftstory/voice"

def zip_dir(source: Path, target: Path) -> None:
    with ZipFile(target, "w", ZIP_DEFLATED) as z:
        for path in sorted(source.rglob("*")):
            if path.is_file():
                z.write(path, path.relative_to(source).as_posix())

def main() -> int:
    for required in (BP_MANIFEST, RP_MANIFEST, SOUND_DEFS):
        if not required.is_file():
            raise SystemExit(f"Missing Bedrock pack file: {required}")

    bp = json.loads(BP_MANIFEST.read_text(encoding="utf-8"))
    rp = json.loads(RP_MANIFEST.read_text(encoding="utf-8"))
    if not any(d.get("uuid") == rp["header"]["uuid"] for d in bp.get("dependencies", [])):
        raise SystemExit("Behavior pack is missing its resource-pack dependency.")

    sounds = json.loads(SOUND_DEFS.read_text(encoding="utf-8"))["sound_definitions"]
    expected = [key.removeprefix("minecraftstory:voice.") for key in sounds if key.startswith("minecraftstory:voice.")]
    missing = [scene for scene in expected if not (VOICE_DIR / f"{scene}.ogg").is_file()]
    if missing:
        raise SystemExit("Voice assets must be generated before packaging. Missing: " + ", ".join(missing))

    DIST.mkdir(exist_ok=True)
    for old in DIST.glob("minecraft-story-*.mcpack"):
        old.unlink()
    addon = DIST / "minecraft-story-chapter-1.mcaddon"
    if addon.exists():
        addon.unlink()

    bp_pack = DIST / "minecraft-story-behavior.mcpack"
    rp_pack = DIST / "minecraft-story-resources.mcpack"
    zip_dir(BP, bp_pack)
    zip_dir(RP, rp_pack)

    with ZipFile(addon, "w", ZIP_DEFLATED) as z:
        z.write(bp_pack, bp_pack.name)
        z.write(rp_pack, rp_pack.name)

    print(f"Created {bp_pack}")
    print(f"Created {rp_pack}")
    print(f"Created {addon}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
