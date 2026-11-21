#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate block/item models, blockstates, loot and language keys for the new worlds."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc"
DATA = ROOT / "src/main/resources/data"
TEXTURES = ASSETS / "textures/block"

NEW_NAMES = {
    "saturn": ("Saturn", "土星"),
    "uranus": ("Uranus", "天王星"),
    "neptune": ("Neptune", "海王星"),
    "pluto": ("Pluto", "冥王星"),
    "makemake": ("Makemake", "鸟神星"),
    "haumea": ("Haumea", "妊神星"),
    "eris": ("Eris", "阋神星"),
    "europa": ("Europa", "木卫二"),
    "ganymede": ("Ganymede", "木卫三"),
    "callisto": ("Callisto", "木卫四"),
    "titan": ("Titan", "土卫六"),
    "titania": ("Titania", "天卫三"),
    "oberon": ("Oberon", "天卫四"),
    "triton": ("Triton", "海卫一"),
    "charon": ("Charon", "冥卫一"),
    "hiiaka": ("Hi'iaka", "妊卫一"),
    "proxima_b": ("Proxima b", "比邻星b"),
}

SUFFIXES = {
    "surface": ("Surface Rock", "地表岩石"),
    "sub_surface": ("Subsurface Rock", "浅层岩石"),
    "stone": ("Stone", "岩石"),
    "ore_iron": ("Iron Ore", "铁矿石"),
    "ore_copper": ("Copper Ore", "铜矿石"),
}


def load(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def save(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def write_block_assets(block: str) -> None:
    save(ASSETS / "blockstates" / f"{block}.json", {
        "variants": {"": {"model": f"newgc:block/{block}"}}
    })
    save(ASSETS / "models/block" / f"{block}.json", {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"newgc:block/{block}"},
    })
    save(ASSETS / "models/item" / f"{block}.json", {
        "parent": f"newgc:block/{block}"
    })
    save(DATA / "newgc/loot_table/blocks" / f"{block}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"newgc:{block}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })


def add_lang(lang: str, entries: dict[str, str]) -> None:
    path = ASSETS / "lang" / f"{lang}.json"
    data = load(path)
    added = 0
    for key, value in entries.items():
        if key not in data:
            data[key] = value
            added += 1
    save(path, data)
    print(f"{lang}: added {added} keys")


def main():
    english = {}
    chinese = {}
    new_blocks = []
    for planet_id, (en_name, zh_name) in NEW_NAMES.items():
        for suffix, (en_suffix, zh_suffix) in SUFFIXES.items():
            block = f"{planet_id}_{suffix}"
            texture = TEXTURES / f"{block}.png"
            if not texture.exists():
                raise FileNotFoundError(texture)
            write_block_assets(block)
            new_blocks.append(block)
            english[f"block.newgc.{block}"] = f"{en_name} {en_suffix}"
            chinese[f"block.newgc.{block}"] = f"{zh_name}{zh_suffix}"

    add_lang("en_us", english)
    add_lang("zh_cn", chinese)

    tag_path = DATA / "minecraft/tags/block/mineable/pickaxe.json"
    tag = load(tag_path)
    values = tag.setdefault("values", [])
    for block in new_blocks:
        if f"newgc:{block}" not in values:
            values.append(f"newgc:{block}")
    save(tag_path, tag)
    print(f"generated {len(new_blocks)} block asset sets; pickaxe tag now has {len(values)} entries")


if __name__ == "__main__":
    main()
