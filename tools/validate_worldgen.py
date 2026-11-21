#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Validate generated dimensions, block references and assets."""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/newgc"
ASSETS = ROOT / "src/main/resources/assets/newgc"
NEW_IDS = {
    "saturn", "uranus", "neptune", "pluto", "makemake", "haumea", "eris",
    "europa", "ganymede", "callisto", "titan", "titania", "oberon",
    "triton", "charon", "hiiaka",
}
ASSET_IDS = NEW_IDS | {"proxima_b"}
SUFFIXES = ("surface", "sub_surface", "stone", "ore_iron", "ore_copper")
EXISTING_EFFECTS = {
    "moon_sky", "mercury_sky", "venus_sky", "ceres_sky", "mars_sky",
    "phobos_sky", "deimos_sky", "jupiter_sky", "io_sky",
}
EXISTING_EFFECTS |= {f"{planet}_sky" for planet in NEW_IDS}
EXISTING_EFFECTS.add("space_sky")


def load(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def check_block(block_id: str, known: set[str], where: str):
    if block_id.startswith("minecraft:"):
        return
    if block_id.startswith("newgc:"):
        block_id = block_id.split(":", 1)[1]
    assert block_id in known, f"{where}: unknown block {block_id}"


def walk_strings(obj):
    if isinstance(obj, dict):
        for value in obj.values():
            yield from walk_strings(value)
    elif isinstance(obj, list):
        for value in obj:
            yield from walk_strings(value)
    elif isinstance(obj, str):
        yield obj


def main():
    java = (ROOT / "src/main/java/com/tairitsutek/newgc/block/PlanetsBlock.java").read_text(encoding="utf-8")
    known_blocks = set(re.findall(r'registerBlock\("([a-z0-9_]+)"', java))
    for planet in ASSET_IDS:
        for suffix in SUFFIXES:
            known_blocks.add(f"{planet}_{suffix}")
    known_blocks |= {"bedrock", "air", "lava"}

    dims = sorted((DATA / "dimension").glob("*.json"))
    assert len(dims) == 27, len(dims)
    seen = set()
    scales = []
    for path in dims:
        dim = load(path)
        dim_id = path.stem
        seen.add(dim_id)

        # 太空漂流维度是空 flat 维度，没有噪声地形、矿物和 surface rule。
        if dim_id == "space":
            assert dim["generator"]["type"] == "minecraft:flat"
            type_id = dim["type"].split(":", 1)[1]
            type_path = DATA / f"dimension_type/{type_id}.json"
            assert type_path.exists(), type_path
            dim_type = load(type_path)
            assert dim_type["effects"] == "newgc:space_sky"
            assert dim_type["natural"] is False
            assert dim_type["bed_works"] is False
            assert dim_type["min_y"] == 0
            assert dim_type["height"] == 256
            biome_id = dim["generator"]["settings"]["biome"].split(":", 1)[1]
            assert (DATA / f"worldgen/biome/{biome_id}.json").exists()
            continue

        type_id = dim["type"].split(":", 1)[1]
        settings_id = dim["generator"]["settings"].split(":", 1)[1]
        biome_id = dim["generator"]["biome_source"]["biome"].split(":", 1)[1]
        type_path = DATA / f"dimension_type/{type_id}.json"
        settings_path = DATA / f"worldgen/noise_settings/{settings_id}.json"
        biome_path = DATA / f"worldgen/biome/{biome_id}.json"
        assert type_path.exists(), type_path
        assert settings_path.exists(), settings_path
        assert biome_path.exists(), biome_path
        dim_type = load(type_path)
        effect = dim_type["effects"].split(":", 1)[1]
        assert effect in EXISTING_EFFECTS, effect
        assert dim_type["cloud_height"] is None, type_path
        assert dim_type["natural"] is True, type_path
        assert dim_type["bed_works"] is True, type_path
        assert dim_type["piglin_safe"] is False, type_path
        assert dim_type["has_skylight"] is True, type_path
        assert dim_type["has_ceiling"] is False, type_path
        assert dim_type["ambient_light"] == 0.0, type_path

        noise = load(settings_path)
        check_block(noise["default_block"]["Name"], known_blocks, settings_path.name)
        check_block(noise["default_fluid"]["Name"], known_blocks, settings_path.name)
        for text in walk_strings(noise["surface_rule"]):
            if text.startswith("newgc:"):
                check_block(text, known_blocks, settings_path.name)
        scales.append((dim_id, noise["noise"]["size_horizontal"],
                       noise["noise"]["size_vertical"]))

        biome = load(biome_path)
        for feature in biome["features"][6]:
            feature_id = feature.split(":", 1)[1]
            assert (DATA / f"worldgen/placed_feature/{feature_id}.json").exists(), feature
        for block_name in (dim_id + "_ore_iron", dim_id + "_ore_copper"):
            assert (DATA / f"worldgen/configured_feature/{block_name}.json").exists()
            assert (DATA / f"worldgen/placed_feature/{block_name}.json").exists()

    assert seen == {p.stem for p in dims}
    for dim_id, horizontal, vertical in scales:
        assert isinstance(horizontal, int) and horizontal == 1, (dim_id, horizontal)
        assert isinstance(vertical, int) and vertical == 1, (dim_id, vertical)
    assert len(scales) == 26
    assert "space" in seen

    for planet in ASSET_IDS:
        for suffix in SUFFIXES:
            block = f"{planet}_{suffix}"
            assert (ASSETS / f"blockstates/{block}.json").exists(), block
            assert (ASSETS / f"models/block/{block}.json").exists(), block
            assert (ASSETS / f"models/item/{block}.json").exists(), block
            assert (DATA / f"loot_table/blocks/{block}.json").exists(), block
            assert (ASSETS / f"textures/block/{block}.png").exists(), block

    for lang in ("en_us", "zh_cn"):
        data = load(ASSETS / f"lang/{lang}.json")
        for planet in ASSET_IDS:
            for suffix in SUFFIXES:
                assert f"block.newgc.{planet}_{suffix}" in data, (lang, planet, suffix)

    tag = load(ROOT / "src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json")
    values = set(tag["values"])
    for planet in ASSET_IDS:
        for suffix in SUFFIXES:
            assert f"newgc:{planet}_{suffix}" in values, (planet, suffix)

    sky_effects = []
    for java_path in (ROOT / "src/main/java").rglob("*.java"):
        text = java_path.read_text(encoding="utf-8")
        if "extends DimensionSpecialEffects" in text:
            assert "getSunriseColor" in text, f"sunrise not disabled: {java_path}"
            sky_effects.append(java_path)

    print(f"OK: 27 dimensions (26 planets + space), {len(NEW_IDS)} new ids, {len(known_blocks)} blocks")
    print("noise cells: 1x1 (per-planet density offsets/waves provide terrain variation)")
    print(f"sunrise/sunset disabled in {len(sky_effects)} custom sky effects")
    print("cloud_height null for every custom dimension type")


if __name__ == "__main__":
    main()
