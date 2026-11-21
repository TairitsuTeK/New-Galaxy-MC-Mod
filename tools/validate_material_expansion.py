#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Validate the generated metal / alloy / machine expansion."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc"
DATA = ROOT / "src/main/resources/data/newgc"
TAGS = ROOT / "src/main/resources/data/minecraft/tags"

BASE = ("lead", "nickel", "platinum", "titanium", "uranium", "zinc")
ALLOYS = ("bronze", "steel", "electrum", "invar")
MACHINES = ("electric_furnace", "alloy_smelter", "energy_cell")

items = set()
for metal in BASE:
    items.update({f"raw_{metal}", f"{metal}_ingot", f"{metal}_nugget",
                  f"{metal}_plate", f"{metal}_block", f"{metal}_ore"})
for alloy in ALLOYS:
    items.update({f"{alloy}_ingot", f"{alloy}_nugget", f"{alloy}_plate",
                  f"{alloy}_block"})
items.update({"tin_nugget", "aluminum_nugget", "silver_nugget"})
items.update(MACHINES)

blocks = {f"{metal}_ore" for metal in BASE}
blocks.update({f"{metal}_block" for metal in BASE})
blocks.update({f"{alloy}_block" for alloy in ALLOYS})
blocks.update(MACHINES)

errors = []


def load(path: Path):
    try:
        with path.open("r", encoding="utf-8") as handle:
            return json.load(handle)
    except Exception as exc:
        errors.append(f"JSON parse failed: {path}: {exc}")
        return None


for path in sorted((ROOT / "src/main/resources").rglob("*.json")):
    load(path)

for item in sorted(items):
    model = ASSETS / f"models/item/{item}.json"
    if not model.exists():
        errors.append(f"missing item model: {item}")
        continue
    value = load(model)
    if value and value.get("parent") == "minecraft:item/generated":
        texture = ASSETS / f"textures/item/{item}.png"
        if not texture.exists():
            errors.append(f"missing item texture: {item}")

for block in sorted(blocks):
    if not (ASSETS / f"blockstates/{block}.json").exists():
        errors.append(f"missing blockstate: {block}")
    model_path = (ASSETS / f"models/block/ore_overworld/{block}.json"
                  if block.endswith("_ore")
                  else ASSETS / f"models/block/{block}.json")
    if not model_path.exists():
        errors.append(f"missing block model: {block}")
    else:
        value = load(model_path)
        texture = value.get("textures", {}).get("all") if value else None
        if texture:
            texture_path = (ASSETS / "textures" /
                            texture.removeprefix("newgc:"))
            texture_path = texture_path.with_suffix(".png")
            if not texture_path.exists():
                errors.append(f"missing block texture: {block} -> {texture}")
    if not (ASSETS / f"models/item/{block}.json").exists():
        errors.append(f"missing block item model: {block}")
    if not (DATA / f"loot_table/blocks/{block}.json").exists():
        errors.append(f"missing loot table: {block}")

language_keys = {}
for lang in ("en_us.json", "zh_cn.json"):
    path = ASSETS / "lang" / lang
    language_keys[lang] = load(path) or {}

for item in sorted(items):
    if item in blocks:
        key = f"block.newgc.{item}"
    elif item.endswith("_nugget") or item.startswith("raw_") or item.endswith("_ingot") or item.endswith("_plate"):
        key = f"item.newgc.{item}"
    else:
        continue
    for lang, data in language_keys.items():
        if key not in data:
            errors.append(f"missing lang key: {lang} -> {key}")

for machine in MACHINES:
    for lang, data in language_keys.items():
        key = f"container.newgc.{machine}"
        if key not in data:
            errors.append(f"missing lang key: {lang} -> {key}")

pickaxe = TAGS / "block/mineable/pickaxe.json"
if pickaxe.exists():
    data = load(pickaxe) or {}
    values = set(data.get("values", []))
    for block in sorted(blocks):
        if f"newgc:{block}" not in values:
            errors.append(f"missing pickaxe tag: {block}")

for metal in BASE:
    ore = f"{metal}_ore"
    for folder in ("configured_feature", "placed_feature"):
        if not (DATA / f"worldgen/{folder}/{ore}.json").exists():
            errors.append(f"missing worldgen {folder}: {ore}")

modifier = DATA / "neoforge/biome_modifier/add_overworld_metal_ores.json"
if not modifier.exists():
    errors.append("missing overworld biome modifier")
else:
    data = load(modifier) or {}
    if data.get("type") != "neoforge:add_features":
        errors.append("biome modifier type is wrong")
    if data.get("step") != "underground_ores":
        errors.append("biome modifier step is wrong")
    if len(data.get("features", [])) != 9:
        errors.append("biome modifier must reference 9 overworld ores")

if errors:
    print(f"{len(errors)} problem(s):")
    for error in errors:
        print("  " + error)
    raise SystemExit(1)

print(f"OK: {len(items)} items, {len(blocks)} blocks, all models/loot/lang/tags/worldgen present.")
