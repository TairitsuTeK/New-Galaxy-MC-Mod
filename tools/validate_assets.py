#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Lightweight validation for generated textures and language files."""

from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc"
ITEM_DIR = ASSETS / "textures/item"
BLOCK_DIR = ASSETS / "textures/block"
ORE_DIR = BLOCK_DIR / "ore_overworld"
GUI_DIR = ASSETS / "textures/gui"
PREVIEW_DIR = ROOT / "preview"

# 星系图标统一规格：恒星 8x8、行星 16x16，且不启用线性模糊。
GUI_STYLE_SIZES = {
    "sun_icon.png": (8, 8),
    "alpha_centauri_a_star.png": (8, 8),
    "alpha_centauri_b_star.png": (8, 8),
    "proxima_star.png": (8, 8),
    "proxima_b_icon.png": (16, 16),
}


def main() -> None:
    bad = []
    item_count = 0
    block_count = 0
    valid_sizes = {(16, 16), (32, 32)}

    for path in sorted(ITEM_DIR.glob("*.png")):
        name = path.stem
        if not (name.startswith("raw_")
                or name.endswith("_ingot")
                or name.endswith("_nugget")
                or name.endswith("_plate")
                or name.startswith("circuit_core_")):
            continue
        item_count += 1
        with Image.open(path) as image:
            if image.size not in valid_sizes:
                bad.append(f"item {path.name}: {image.size}")

    for path in sorted(BLOCK_DIR.glob("*.png")):
        name = path.stem
        if not (name.endswith("_ore_iron")
                or name.endswith("_ore_copper")
                or name.endswith("_stone")
                or name.endswith("_sub_surface")
                or name.endswith("_surface")
                or name.endswith("_block")
                or name in {"moon_soil", "sulfur", "jupiter_gravel"}):
            continue
        block_count += 1
        with Image.open(path) as image:
            if image.size not in valid_sizes:
                bad.append(f"block {path.name}: {image.size}")
            elif name.startswith("proxima_b_") and image.size != (16, 16):
                bad.append(f"block {path.name}: Alpha Centauri style must be 16x16")

    for name in ("tier2_rocket", "tier2_nose_cone", "advanced_rocket_engine",
                 "tier3_rocket", "tier3_nose_cone", "tier3_rocket_engine"):
        path = ITEM_DIR / f"{name}.png"
        model = ASSETS / "models/item" / f"{name}.json"
        if not path.exists():
            bad.append(f"missing item texture {name}.png")
        else:
            with Image.open(path) as image:
                if image.size != (32, 32):
                    bad.append(f"item {name}.png: {image.size}, expected (32, 32)")
        if not model.exists():
            bad.append(f"missing item model {name}.json")

    for tier in (2, 3):
        recipe_path = (ROOT / "src/main/resources/data/newgc/recipe/rocket_assembly"
                       / f"tier{tier}_rocket.json")
        if not recipe_path.exists():
            bad.append(f"missing tier{tier} rocket assembly recipe")
        else:
            with recipe_path.open("r", encoding="utf-8") as handle:
                recipe = json.load(handle)
            if len(recipe.get("ingredients", [])) != 14:
                bad.append(f"tier{tier} rocket assembly recipe must have 14 ingredients")
            if recipe.get("result", {}).get("id") != f"newgc:tier{tier}_rocket":
                bad.append(f"tier{tier} rocket assembly recipe has the wrong result")

        entity_texture = ASSETS / f"textures/entity/tier{tier}_rocket.png"
        if not entity_texture.exists():
            bad.append(f"missing textures/entity/tier{tier}_rocket.png")
        else:
            with Image.open(entity_texture) as image:
                if image.size != (256, 128):
                    bad.append(f"tier{tier} entity texture: {image.size}, expected (256, 128)")

    for name in ("electric_furnace", "alloy_smelter", "energy_cell",
                 "rocket_assembler_side", "rocket_assembler_top",
                 "rocket_assembler_metal", "rocket_assembler_accent",
                 "rocket_assembler_rocket"):
        path = BLOCK_DIR / f"{name}.png"
        if path.exists():
            block_count += 1
            with Image.open(path) as image:
                if image.size not in valid_sizes:
                    bad.append(f"machine {path.name}: {image.size}")

    if ORE_DIR.exists():
        for path in sorted(ORE_DIR.glob("*.png")):
            block_count += 1
            with Image.open(path) as image:
                if image.size not in valid_sizes:
                    bad.append(f"ore {path.name}: {image.size}")

    star_map_assets = (ROOT / "src/main/java/com/tairitsutek/newgc/gui"
                       / "StarMapAssets.java")
    if not star_map_assets.exists():
        bad.append("missing StarMapAssets.java")
    else:
        source = star_map_assets.read_text(encoding="utf-8")
        for name in re.findall(r'tex\("([^"]+)"\)', source):
            if not (GUI_DIR / name).exists():
                bad.append(f"missing prewarmed star map icon {name}")

    for name, expected in GUI_STYLE_SIZES.items():
        path = GUI_DIR / name
        if not path.exists():
            bad.append(f"missing icon {name}")
            continue
        with Image.open(path) as image:
            if image.size != expected:
                bad.append(f"icon {name}: {image.size}, expected {expected}")
        meta = GUI_DIR / f"{name}.mcmeta"
        if not meta.exists():
            bad.append(f"missing {meta.name}")
            continue
        with meta.open("r", encoding="utf-8") as handle:
            texture = json.load(handle).get("texture", {})
        if texture.get("blur") is not False:
            bad.append(f"{meta.name}: blur must be false")

    for lang in ("en_us.json", "zh_cn.json"):
        path = ASSETS / "lang" / lang
        with path.open("r", encoding="utf-8") as handle:
            json.load(handle)

    backup = PREVIEW_DIR / "textures_backup.zip"
    backup_entries = 0
    if backup.exists():
        with zipfile.ZipFile(backup) as archive:
            backup_entries = len(archive.namelist())

    print(f"items={item_count}, blocks={block_count}, backup_entries={backup_entries}")
    print(f"preview={ (PREVIEW_DIR / 'texture_preview.png').exists() }")
    if bad:
        print("BAD SIZES:")
        for line in bad:
            print("  " + line)
        raise SystemExit(1)
    print("Textures follow the 16x16/32x32 convention, "
          "galaxy icon style checks pass, and lang JSON is valid.")


if __name__ == "__main__":
    main()
