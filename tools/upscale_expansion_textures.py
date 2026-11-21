#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Bring the newly generated expansion textures to the project's 32x32 scale."""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc/textures"

ITEM_NAMES = [
    "bronze_ingot", "bronze_nugget", "bronze_plate",
    "steel_ingot", "steel_nugget", "steel_plate",
    "electrum_ingot", "electrum_nugget", "electrum_plate",
    "invar_ingot", "invar_nugget", "invar_plate",
]
BLOCK_NAMES = [
    "bronze_block", "steel_block", "electrum_block", "invar_block",
    "electric_furnace", "alloy_smelter", "energy_cell",
]
ORE_NAMES = [
    "lead_ore", "nickel_ore", "platinum_ore",
    "titanium_ore", "uranium_ore", "zinc_ore",
]


def upscale(path: Path) -> None:
    if not path.exists():
        return
    with Image.open(path) as image:
        if image.size == (32, 32):
            return
        image.resize((32, 32), Image.Resampling.NEAREST).save(path, optimize=True)


for name in ITEM_NAMES:
    upscale(ASSETS / "item" / f"{name}.png")
for name in BLOCK_NAMES:
    upscale(ASSETS / "block" / f"{name}.png")
for name in ORE_NAMES:
    upscale(ASSETS / "block" / "ore_overworld" / f"{name}.png")

print("Expansion textures normalized to 32x32.")
