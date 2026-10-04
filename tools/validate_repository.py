#!/usr/bin/env python3
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

SEMVER = re.compile(r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$")
SCENE_RE = re.compile(r'(?:\bscene|\bSCENES\\.put)\("([^"]+)"')
CH1_LINE_RE = re.compile(r'l\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)",')
CH2_LINE_RE = re.compile(r'new Line\("((?:\\\\.|[^"])*)",\s*"((?:\\\\.|[^"])*)"\)')

def unescape(value: str) -> str:
    return value.replace('\\\\', '\\').replace('\\\"', '"')

def read_json(path: Path) -> dict:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        raise SystemExit(f"Invalid JSON: {path}: {exc}")

def project_version() -> str:
    text = (ROOT / "java/gradle.properties").read_text(encoding="utf-8")
    m = re.search(r"(?m)^version=(.+)$", text)
    if not m or not SEMVER.fullmatch(m.group(1).strip()):
        raise SystemExit("java/gradle.properties does not contain a valid semantic version.")
    return m.group(1).strip()

def discover_dialogue() -> tuple[list[str], set[str]]:
    scenes: list[str] = []
    speakers: set[str] = set()

    source = (ROOT / "java/src/main/java/com/minecraftstory/story/Chapter1Content.java").read_text(encoding="utf-8")
    decls = list(SCENE_RE.finditer(source))
    for i, decl in enumerate(decls):
        end = decls[i + 1].start() if i + 1 < len(decls) else len(source)
        block = source[decl.end():end]
        lines = [(unescape(m.group(1)), unescape(m.group(2))) for m in CH1_LINE_RE.finditer(block)]
        if lines:
            scenes.append(decl.group(1))
            speakers.update(s for s, _ in lines)

    source = (ROOT / "java/src/main/java/com/minecraftstory/story/Chapter2Content.java").read_text(encoding="utf-8")
    lines = [(unescape(m.group(1)), unescape(m.group(2))) for m in CH2_LINE_RE.finditer(source)]
    if lines:
        scenes.append("chapter2_opening")
        speakers.update(s for s, _ in lines)
    return scenes, speakers

def main() -> int:
    version = project_version()

    # Player-facing docs are intentionally minimal.
    allowed_md = {ROOT / "README.md", ROOT / "docs/INSTALL.md", ROOT / "LICENSE"}
    for path in ROOT.rglob("*.md"):
        if ".git" not in path.parts and path not in allowed_md:
            raise SystemExit(f"Stale/duplicate documentation remains: {path.relative_to(ROOT)}")

    workflows = sorted((ROOT / ".github/workflows").glob("*"))
    workflow_names = [p.name for p in workflows if p.is_file()]
    if workflow_names != ["ci.yml", "release.yml"]:
        raise SystemExit(f"Unexpected GitHub workflows: {workflow_names}")

    bp = read_json(ROOT / "bedrock/behavior_packs/minecraft_story/manifest.json")
    rp = read_json(ROOT / "bedrock/resource_packs/minecraft_story/manifest.json")
    sounds = read_json(ROOT / "bedrock/resource_packs/minecraft_story/sounds/sound_definitions.json")
    profiles = read_json(ROOT / "audio/voice_profiles.json")

    def as_version(v):
        return ".".join(str(x) for x in v)

    if as_version(bp["header"]["version"]) != version or as_version(rp["header"]["version"]) != version:
        raise SystemExit("Java and Bedrock project versions are not aligned.")

    for manifest_name, manifest in [("behavior", bp), ("resource", rp)]:
        header_version = as_version(manifest["header"]["version"])
        for module in manifest.get("modules", []):
            if as_version(module["version"]) != header_version:
                raise SystemExit(f"{manifest_name} pack module version drift detected.")
        for dependency in manifest.get("dependencies", []):
            # Native Script API dependencies use their own API versions.
            if "uuid" in dependency and as_version(dependency["version"]) != header_version:
                raise SystemExit(f"{manifest_name} pack dependency version drift detected.")
    if not any(d["uuid"] == rp["header"]["uuid"] for d in bp.get("dependencies", [])):
        raise SystemExit("Behavior pack does not depend on the canonical voice resource pack.")
    release_text = (ROOT / ".github/workflows/release.yml").read_text(encoding="utf-8")
    if "VERSION:" in release_text:
        raise SystemExit("Release workflow still contains a hard-coded VERSION environment value.")

    scenes, speakers = discover_dialogue()
    character_profiles = profiles.get("characters", {})
    missing = sorted(speakers - set(character_profiles))
    if missing:
        raise SystemExit("Dialogue speakers without a shared voice profile: " + ", ".join(missing))

    for scene in scenes:
        key = "minecraftstory:voice." + scene
        if key not in sounds.get("sound_definitions", {}):
            raise SystemExit(f"Missing Bedrock sound definition: {key}")

    generator = (ROOT / "tools/generate_story_audio.py").read_text(encoding="utf-8")
    if "voice_profiles.json" not in generator or "CHAPTER_2" not in generator:
        raise SystemExit("Audio generator is not using the shared cross-chapter voice profile source.")

    release = (ROOT / ".github/workflows/release.yml").read_text(encoding="utf-8")
    lower_release = release.lower()
    if "latest published stable version" not in lower_release or "gh release list" not in lower_release or "semver" not in lower_release:
        raise SystemExit("Release workflow does not contain dynamic semantic-version detection.")

    if not (ROOT / ".gitignore").read_text(encoding="utf-8").count("dist/"):
        raise SystemExit(".gitignore must exclude generated dist/ release packaging output.")

    if "PROFILES =" in generator or "PROFILES = {" in generator:
        raise SystemExit("Voice identities must come from the canonical profile manifest only.")

    print(f"Repository audit OK — version {version}, {len(scenes)} voice scenes, {len(speakers)} shared speakers.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
