#!/usr/bin/env python3
"""Package the Bedrock behavior/resource packs into a single .mcaddon."""

from __future__ import annotations

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile
import shutil

ROOT = Path(__file__).resolve().parents[1]
BP = ROOT / "bedrock/behavior_packs/minecraft_story"
RP = ROOT / "bedrock/resource_packs/minecraft_story"
DIST = ROOT / "dist"
DIST.mkdir(exist_ok=True)

bp_pack = DIST / "minecraft-story-behavior.mcpack"
rp_pack = DIST / "minecraft-story-resources.mcpack"
addon = DIST / "minecraft-story-chapter-1.mcaddon"

def zip_dir(source: Path, target: Path) -> None:
    with ZipFile(target, "w", ZIP_DEFLATED) as z:
        for path in sorted(source.rglob("*")):
            if path.is_file():
                z.write(path, path.relative_to(source).as_posix())

zip_dir(BP, bp_pack)
zip_dir(RP, rp_pack)

with ZipFile(addon, "w", ZIP_DEFLATED) as z:
    z.write(bp_pack, bp_pack.name)
    z.write(rp_pack, rp_pack.name)

# Also expose the two packs individually for users who prefer separate imports.
print(addon)
