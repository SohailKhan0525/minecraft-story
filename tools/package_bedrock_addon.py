#!/usr/bin/env python3
"""Package the Bedrock behavior/resource packs into installable .mcpack, .mcaddon and .mcworld files."""

from __future__ import annotations

import json
import shutil
import urllib.request
from pathlib import Path
from tempfile import TemporaryDirectory
from zipfile import ZIP_DEFLATED, ZipFile

ROOT = Path(__file__).resolve().parents[1]
BP = ROOT / "bedrock/behavior_packs/minecraft_story"
RP = ROOT / "bedrock/resource_packs/minecraft_story"
DIST = ROOT / "dist"

BP_MANIFEST = BP / "manifest.json"
RP_MANIFEST = RP / "manifest.json"
SOUND_DEFS = RP / "sounds/sound_definitions.json"
VOICE_DIR = RP / "sounds/minecraftstory/voice"

WORLD_SOURCE_URL = "https://raw.githubusercontent.com/microsoft/minecraft-samples/main/parkour_sample_world/SampleParkourWorld.mcworld"
BEHAVIOR_FOLDER = "mststory"
RESOURCE_FOLDER = "mstvox"

SCENES = [
    "cold_open", "havenfall", "chapel", "missing_sound", "silent_forest",
    "observatory", "first_choice", "door_below", "heart", "ending",
    "post_credits", "chapter2_opening"
]


def zip_dir(source: Path, target: Path) -> None:
    with ZipFile(target, "w", ZIP_DEFLATED) as z:
        for path in sorted(source.rglob("*")):
            if path.is_file():
                z.write(path, path.relative_to(source).as_posix())


def validate_manifest_pair(bp: dict, rp: dict) -> None:
    if not any(
        dependency.get("uuid") == rp["header"]["uuid"]
        and dependency.get("version") == rp["header"]["version"]
        for dependency in bp.get("dependencies", [])
    ):
        raise SystemExit("Behavior pack does not depend on the exact resource pack UUID/version.")


def validate_pack_archive(path: Path, expected_uuid: str, expected_type: str) -> None:
    with ZipFile(path) as z:
        names = set(z.namelist())
        if "manifest.json" not in names:
            raise SystemExit(f"{path.name} has no root manifest.json")
        if any(name.startswith("/") or ".." in Path(name).parts for name in names):
            raise SystemExit(f"{path.name} contains unsafe archive paths")
        manifest = json.loads(z.read("manifest.json").decode("utf-8"))
        if manifest["header"]["uuid"] != expected_uuid:
            raise SystemExit(f"{path.name} has the wrong pack UUID")
        if not any(module.get("type") == expected_type for module in manifest.get("modules", [])):
            raise SystemExit(f"{path.name} has no {expected_type} module")


def build_mcaddon(bp_pack: Path, rp_pack: Path, output: Path) -> None:
    with ZipFile(output, "w", ZIP_DEFLATED) as z:
        z.write(bp_pack, bp_pack.name)
        z.write(rp_pack, rp_pack.name)


def build_mcworld(bp: dict, rp: dict, bp_pack: Path, rp_pack: Path, output: Path) -> None:
    with TemporaryDirectory(prefix="minecraft-story-world-") as tmp:
        tmpdir = Path(tmp)
        source = tmpdir / "SampleParkourWorld.mcworld"
        urllib.request.urlretrieve(WORLD_SOURCE_URL, source)

        world = tmpdir / "world"
        world.mkdir()
        with ZipFile(source) as z:
            z.extractall(world)

        # Remove sample-world pack state so only Minecraft Story is active.
        for rel in (
            "behavior_packs", "resource_packs",
            "world_behavior_packs.json", "world_resource_packs.json",
            "world_behavior_pack_history.json", "world_resource_pack_history.json",
        ):
            target = world / rel
            if target.is_dir():
                shutil.rmtree(target)
            elif target.exists():
                target.unlink()

        behavior_dir = world / "behavior_packs" / BEHAVIOR_FOLDER
        resource_dir = world / "resource_packs" / RESOURCE_FOLDER
        behavior_dir.mkdir(parents=True)
        resource_dir.mkdir(parents=True)

        with ZipFile(bp_pack) as z:
            z.extractall(behavior_dir)
        with ZipFile(rp_pack) as z:
            z.extractall(resource_dir)

        version = bp["header"]["version"]
        (world / "world_behavior_packs.json").write_text(
            json.dumps([{"pack_id": bp["header"]["uuid"], "version": version}], indent=2) + "\n",
            encoding="utf-8",
        )
        (world / "world_resource_packs.json").write_text(
            json.dumps([{"pack_id": rp["header"]["uuid"], "version": version}], indent=2) + "\n",
            encoding="utf-8",
        )
        (world / "world_behavior_pack_history.json").write_text(
            json.dumps([{"pack_id": bp["header"]["uuid"], "version": version}], indent=2) + "\n",
            encoding="utf-8",
        )
        (world / "world_resource_pack_history.json").write_text(
            json.dumps([{"pack_id": rp["header"]["uuid"], "version": version}], indent=2) + "\n",
            encoding="utf-8",
        )
        (world / "levelname.txt").write_text("Minecraft Story — Chapter 1\n", encoding="utf-8")

        # Validate the Xbox-safe short folder names requested by the Bedrock world-template format.
        if len(BEHAVIOR_FOLDER) > 10 or len(RESOURCE_FOLDER) > 10:
            raise SystemExit("Bedrock world pack folder names must be 10 characters or shorter.")

        with ZipFile(output, "w", ZIP_DEFLATED) as z:
            for file in sorted(world.rglob("*")):
                if file.is_file():
                    z.write(file, file.relative_to(world).as_posix())


def validate_mcaddon(path: Path, bp_uuid: str, rp_uuid: str) -> None:
    with ZipFile(path) as z:
        names = set(z.namelist())
        expected = {"minecraft-story-behavior.mcpack", "minecraft-story-resources.mcpack"}
        if not expected.issubset(names):
            raise SystemExit("MCADDON is missing one or more nested MCPACK files.")
        with z.open("minecraft-story-behavior.mcpack") as raw:
            data = raw.read()
        with TemporaryDirectory() as tmp:
            root = Path(tmp)
            bp_path = root / "bp.mcpack"
            bp_path.write_bytes(data)
            validate_pack_archive(bp_path, bp_uuid, "data")


def validate_mcworld(path: Path, bp: dict, rp: dict) -> None:
    with ZipFile(path) as z:
        names = set(z.namelist())
        required = {
            "level.dat",
            "levelname.txt",
            "world_behavior_packs.json",
            "world_resource_packs.json",
        }
        missing = sorted(required - names)
        if missing:
            raise SystemExit("MCWORLD is missing: " + ", ".join(missing))
        behavior = json.loads(z.read("world_behavior_packs.json").decode("utf-8"))
        resources = json.loads(z.read("world_resource_packs.json").decode("utf-8"))
        if behavior != [{"pack_id": bp["header"]["uuid"], "version": bp["header"]["version"]}]:
            raise SystemExit("MCWORLD behavior-pack activation is not canonical.")
        if resources != [{"pack_id": rp["header"]["uuid"], "version": rp["header"]["version"]}]:
            raise SystemExit("MCWORLD resource-pack activation is not canonical.")
        if f"behavior_packs/{BEHAVIOR_FOLDER}/manifest.json" not in names:
            raise SystemExit("MCWORLD does not contain the behavior pack at the expected root path.")
        if f"resource_packs/{RESOURCE_FOLDER}/manifest.json" not in names:
            raise SystemExit("MCWORLD does not contain the resource pack at the expected root path.")


def main() -> int:
    for required in (BP_MANIFEST, RP_MANIFEST, SOUND_DEFS):
        if not required.is_file():
            raise SystemExit(f"Missing Bedrock pack file: {required}")

    bp = json.loads(BP_MANIFEST.read_text(encoding="utf-8"))
    rp = json.loads(RP_MANIFEST.read_text(encoding="utf-8"))
    validate_manifest_pair(bp, rp)

    sounds = json.loads(SOUND_DEFS.read_text(encoding="utf-8"))["sound_definitions"]
    expected_scenes = [key.removeprefix("minecraftstory:voice.") for key in sounds if key.startswith("minecraftstory:voice.")]
    missing = [scene for scene in SCENES if not (VOICE_DIR / f"{scene}.ogg").is_file()]
    if missing:
        raise SystemExit("Voice assets must be generated before packaging. Missing: " + ", ".join(missing))
    undefined = [scene for scene in SCENES if "minecraftstory:voice." + scene not in sounds]
    if undefined:
        raise SystemExit("Missing sound definitions for: " + ", ".join(undefined))
    if not expected_scenes:
        raise SystemExit("No Minecraft Story voice sound definitions were found.")

    DIST.mkdir(exist_ok=True)
    for old in DIST.glob("minecraft-story-*"):
        if old.is_file():
            old.unlink()

    bp_pack = DIST / "minecraft-story-behavior.mcpack"
    rp_pack = DIST / "minecraft-story-resources.mcpack"
    addon = DIST / "minecraft-story-chapter-1.mcaddon"
    world = DIST / "minecraft-story-chapter-1.mcworld"

    zip_dir(BP, bp_pack)
    zip_dir(RP, rp_pack)

    validate_pack_archive(bp_pack, bp["header"]["uuid"], "data")
    validate_pack_archive(rp_pack, rp["header"]["uuid"], "resources")

    build_mcaddon(bp_pack, rp_pack, addon)
    validate_mcaddon(addon, bp["header"]["uuid"], rp["header"]["uuid"])

    build_mcworld(bp, rp, bp_pack, rp_pack, world)
    validate_mcworld(world, bp, rp)

    print(f"Created {bp_pack}")
    print(f"Created {rp_pack}")
    print(f"Created {addon}")
    print(f"Created {world}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
