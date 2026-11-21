#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate data/models/recipes for the metal and machine expansion.

The script only creates files that belong to this expansion and merges the
language / pickaxe tag entries into the existing files. Existing records are
kept intact, so it can be re-run safely.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc"
DATA = ROOT / "src/main/resources/data/newgc"
MINECRAFT_DATA = ROOT / "src/main/resources/data/minecraft"

BASE_METALS = {
    "lead": ("Lead", "铅", 10, -16, 80),
    "nickel": ("Nickel", "镍", 10, -32, 72),
    "platinum": ("Platinum", "铂", 5, -56, 24),
    "titanium": ("Titanium", "钛", 7, -48, 48),
    "uranium": ("Uranium", "铀", 4, -60, 16),
    "zinc": ("Zinc", "锌", 12, -8, 96),
}

ALLOYS = {
    "bronze": ("Bronze", "青铜"),
    "steel": ("Steel", "钢"),
    "electrum": ("Electrum", "琥珀金"),
    "invar": ("Invar", "殷钢"),
}

ORE_NAMES = {
    "lead_ore": ("Lead Ore", "铅矿石"),
    "nickel_ore": ("Nickel Ore", "镍矿石"),
    "platinum_ore": ("Platinum Ore", "铂矿石"),
    "titanium_ore": ("Titanium Ore", "钛矿石"),
    "uranium_ore": ("Uranium Ore", "铀矿石"),
    "zinc_ore": ("Zinc Ore", "锌矿石"),
}

MACHINES = {
    "electric_furnace": ("Electric Furnace", "电力熔炉"),
    "alloy_smelter": ("Alloy Smelter", "合金熔炉"),
    "energy_cell": ("Energy Cell", "能量电池"),
}


def write_json(path: Path, value) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as handle:
        json.dump(value, handle, ensure_ascii=False, indent=2)
        handle.write("\n")


def read_json(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def blockstate(name: str, model: str) -> None:
    write_json(ASSETS / f"blockstates/{name}.json", {
        "variants": {"": {"model": model}}
    })


def cube_model(name: str, texture: str) -> None:
    write_json(ASSETS / f"models/block/{name}.json", {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": texture}
    })


def item_model(name: str, parent: str) -> None:
    write_json(ASSETS / f"models/item/{name}.json", {"parent": parent})


def generated_item_model(name: str) -> None:
    write_json(ASSETS / f"models/item/{name}.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"newgc:item/{name}"}
    })


def simple_loot(name: str) -> None:
    write_json(DATA / f"loot_table/blocks/{name}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "bonus_rolls": 0,
            "entries": [{"type": "minecraft:item", "name": f"newgc:{name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]
        }]
    })


def ore_loot(name: str, raw: str) -> None:
    write_json(DATA / f"loot_table/blocks/{name}.json", {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "bonus_rolls": 0,
            "entries": [{
                "type": "minecraft:alternatives",
                "children": [
                    {
                        "type": "minecraft:item",
                        "name": f"newgc:{name}",
                        "conditions": [{
                            "condition": "minecraft:match_tool",
                            "predicate": {
                                "predicates": {
                                    "minecraft:enchantments": [{
                                        "enchantments": "minecraft:silk_touch",
                                        "levels": {"min": 1}
                                    }]
                                }
                            }
                        }]
                    },
                    {
                        "type": "minecraft:item",
                        "name": f"newgc:{raw}",
                        "functions": [
                            {
                                "function": "minecraft:set_count",
                                "count": {
                                    "type": "minecraft:uniform",
                                    "min": 1.0,
                                    "max": 1.0
                                },
                                "add": False
                            },
                            {
                                "function": "minecraft:apply_bonus",
                                "enchantment": "minecraft:fortune",
                                "formula": "minecraft:ore_drops"
                            },
                            {"function": "minecraft:explosion_decay"}
                        ]
                    }
                ]
            }]
        }]
    })

def shaped(name: str, pattern, key, result: str, count: int = 1) -> None:
    write_json(DATA / f"recipe/{name}.json", {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": pattern,
        "key": {slot: ({"item": value} if isinstance(value, str) else value)
                for slot, value in key.items()},
        "result": {"id": f"newgc:{result}", "count": count}
    })


def shapeless(name: str, ingredients, result: str, count: int = 1) -> None:
    write_json(DATA / f"recipe/{name}.json", {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "ingredients": [{"item": value} if isinstance(value, str) else value
                        for value in ingredients],
        "result": {"id": f"newgc:{result}", "count": count}
    })


def smelt(kind: str, metal: str, cooking_time: int) -> None:
    write_json(DATA / f"recipe/{kind}_{metal}_ingot.json", {
        "type": f"minecraft:{kind}",
        "category": "misc",
        "ingredient": {"item": f"newgc:raw_{metal}"},
        "result": {"id": f"newgc:{metal}_ingot", "count": 1},
        "experience": 0.7,
        "cookingtime": cooking_time
    })


def compressing(metal: str) -> None:
    write_json(DATA / f"recipe/compressing/{metal}_plate.json", {
        "type": "newgc:compressing",
        "ingredient": {"item": f"newgc:{metal}_ingot"},
        "inputCount": 2,
        "result": {"id": f"newgc:{metal}_plate", "count": 1}
    })


def alloy_recipe(name: str, first, second, result: str, count: int = 2) -> None:
    write_json(DATA / f"recipe/alloy_smelting/{name}.json", {
        "type": "newgc:alloy_smelting",
        "ingredients": [{"item": value} if isinstance(value, str) else value
                        for value in (first, second)],
        "result": {"id": f"newgc:{result}", "count": count}
    })


def metal_common(name: str) -> None:
    """Block / nugget / plate recipes shared by base metals and alloys."""
    shaped(f"{name}_block", ["III", "III", "III"],
           {"I": f"newgc:{name}_ingot"}, f"{name}_block")
    shapeless(f"{name}_ingot_from_block", [f"newgc:{name}_block"],
              f"{name}_ingot", 9)
    shapeless(f"{name}_nugget", [f"newgc:{name}_ingot"],
              f"{name}_nugget", 9)
    shaped(f"{name}_ingot_from_nuggets", ["NNN", "NNN", "NNN"],
           {"N": f"newgc:{name}_nugget"}, f"{name}_ingot")
    compressing(name)


def generate_materials() -> None:
    for metal, (english, chinese, _, _, _) in BASE_METALS.items():
        for suffix in ("raw_", "_ingot", "_nugget", "_plate"):
            generated_item_model(f"{suffix}{metal}" if suffix == "raw_"
                                 else f"{metal}{suffix}")
        cube_model(f"{metal}_block", f"newgc:block/{metal}_block")
        blockstate(f"{metal}_block", f"newgc:block/{metal}_block")
        item_model(f"{metal}_block", f"newgc:block/{metal}_block")
        simple_loot(f"{metal}_block")
        metal_common(metal)
        smelt("smelting", metal, 200)
        smelt("blasting", metal, 100)

        ore = f"{metal}_ore"
        cube_model(f"ore_overworld/{ore}", f"newgc:block/ore_overworld/{ore}")
        blockstate(ore, f"newgc:block/ore_overworld/{ore}")
        item_model(ore, f"newgc:block/ore_overworld/{ore}")
        ore_loot(ore, f"raw_{metal}")

    for alloy in ALLOYS:
        for suffix in ("_ingot", "_nugget", "_plate"):
            generated_item_model(f"{alloy}{suffix}")
        cube_model(f"{alloy}_block", f"newgc:block/{alloy}_block")
        blockstate(f"{alloy}_block", f"newgc:block/{alloy}_block")
        item_model(f"{alloy}_block", f"newgc:block/{alloy}_block")
        simple_loot(f"{alloy}_block")
        metal_common(alloy)

    for metal in ("tin", "aluminum", "silver"):
        generated_item_model(f"{metal}_nugget")
        shapeless(f"{metal}_nugget", [f"newgc:{metal}_ingot"],
                  f"{metal}_nugget", 9)
        shaped(f"{metal}_ingot_from_nuggets", ["NNN", "NNN", "NNN"],
               {"N": f"newgc:{metal}_nugget"}, f"{metal}_ingot")

    alloy_recipe("bronze", "minecraft:copper_ingot", "newgc:tin_ingot",
                 "bronze_ingot")
    alloy_recipe("steel", "minecraft:iron_ingot",
                 {"tag": "minecraft:coals"}, "steel_ingot", 1)
    alloy_recipe("electrum", "minecraft:gold_ingot", "newgc:silver_ingot",
                 "electrum_ingot")
    alloy_recipe("invar", "minecraft:iron_ingot", "newgc:nickel_ingot",
                 "invar_ingot")


def generate_machines() -> None:
    for machine in MACHINES:
        cube_model(machine, f"newgc:block/{machine}")
        blockstate(machine, f"newgc:block/{machine}")
        item_model(machine, f"newgc:block/{machine}")
        simple_loot(machine)

    shaped("electric_furnace", ["ICI", "CMC", "IRI"], {
        "I": "newgc:iron_plate",
        "C": "newgc:copper_plate",
        "M": "newgc:mechanical_casing",
        "R": "minecraft:redstone"
    }, "electric_furnace")

    shaped("alloy_smelter", ["IPI", "CMC", "IFI"], {
        "I": "newgc:iron_plate",
        "P": "newgc:circuit_core_copper",
        "C": "newgc:copper_plate",
        "M": "newgc:mechanical_casing",
        "F": "minecraft:furnace"
    }, "alloy_smelter")

    shaped("energy_cell", ["IPI", "CMC", "IRI"], {
        "I": "newgc:tin_plate",
        "P": "newgc:circuit_core_golden",
        "C": "newgc:copper_plate",
        "M": "newgc:mechanical_casing",
        "R": "minecraft:redstone"
    }, "energy_cell")

    shaped("compressor", ["IPI", "CMC", "IRI"], {
        "I": "newgc:iron_plate",
        "P": "newgc:circuit_core_copper",
        "C": "newgc:copper_plate",
        "M": "newgc:mechanical_casing",
        "R": "minecraft:redstone"
    }, "compressor")

    shaped("oxygen_extractor", ["IGI", "GMG", "ICI"], {
        "I": "newgc:tin_plate",
        "G": "minecraft:glass",
        "M": "newgc:mechanical_casing",
        "C": "newgc:circuit_core_copper"
    }, "oxygen_extractor")

    shaped("wire", ["CCC"], {"C": "newgc:copper_plate"}, "wire", 6)


def ore_feature(ore: str, count: int, min_y: int, max_y: int) -> None:
    write_json(DATA / f"worldgen/configured_feature/{ore}.json", {
        "type": "minecraft:ore",
        "config": {
            "size": 9,
            "discard_chance_on_air_exposure": 0.0,
            "targets": [
                {
                    "target": {
                        "predicate_type": "minecraft:tag_match",
                        "tag": "minecraft:stone_ore_replaceables"
                    },
                    "state": {"Name": f"newgc:{ore}"}
                },
                {
                    "target": {
                        "predicate_type": "minecraft:tag_match",
                        "tag": "minecraft:deepslate_ore_replaceables"
                    },
                    "state": {"Name": f"newgc:{ore}"}
                }
            ]
        }
    })
    write_json(DATA / f"worldgen/placed_feature/{ore}.json", {
        "feature": f"newgc:{ore}",
        "placement": [
            {"type": "minecraft:count", "count": count},
            {"type": "minecraft:in_square"},
            {
                "type": "minecraft:height_range",
                "height": {
                    "type": "minecraft:uniform",
                    "min_inclusive": {"absolute": min_y},
                    "max_inclusive": {"absolute": max_y}
                }
            },
            {"type": "minecraft:biome"}
        ]
    })


def generate_worldgen() -> None:
    features = []
    for metal, (english, chinese, count, min_y, max_y) in BASE_METALS.items():
        ore = f"{metal}_ore"
        ore_feature(ore, count, min_y, max_y)
        features.append(f"newgc:{ore}")

    for ore, count, min_y, max_y in (
            ("tin_ore", 12, -24, 96),
            ("aluminum_ore", 10, 0, 128),
            ("silver_ore", 8, -32, 64)):
        ore_feature(ore, count, min_y, max_y)
        features.append(f"newgc:{ore}")

    write_json(DATA / "neoforge/biome_modifier/add_overworld_metal_ores.json", {
        "type": "neoforge:add_features",
        "biomes": "#minecraft:is_overworld",
        "features": features,
        "step": "underground_ores"
    })


def merge_lang() -> None:
    en = {}
    zh = {}
    for metal, (english, chinese, _, _, _) in BASE_METALS.items():
        en[f"block.newgc.{metal}_ore"] = f"{english} Ore"
        en[f"block.newgc.{metal}_block"] = f"Block of {english}"
        en[f"item.newgc.raw_{metal}"] = f"Raw {english}"
        en[f"item.newgc.{metal}_ingot"] = f"{english} Ingot"
        en[f"item.newgc.{metal}_nugget"] = f"{english} Nugget"
        en[f"item.newgc.{metal}_plate"] = f"{english} Plate"
        zh[f"block.newgc.{metal}_ore"] = f"{chinese}矿石"
        zh[f"block.newgc.{metal}_block"] = f"{chinese}块"
        zh[f"item.newgc.raw_{metal}"] = f"粗{chinese}"
        zh[f"item.newgc.{metal}_ingot"] = f"{chinese}锭"
        zh[f"item.newgc.{metal}_nugget"] = f"{chinese}粒"
        zh[f"item.newgc.{metal}_plate"] = f"{chinese}板"

    for alloy, (english, chinese) in ALLOYS.items():
        en[f"block.newgc.{alloy}_block"] = f"Block of {english}"
        en[f"item.newgc.{alloy}_ingot"] = f"{english} Ingot"
        en[f"item.newgc.{alloy}_nugget"] = f"{english} Nugget"
        en[f"item.newgc.{alloy}_plate"] = f"{english} Plate"
        zh[f"block.newgc.{alloy}_block"] = f"{chinese}块"
        zh[f"item.newgc.{alloy}_ingot"] = f"{chinese}锭"
        zh[f"item.newgc.{alloy}_nugget"] = f"{chinese}粒"
        zh[f"item.newgc.{alloy}_plate"] = f"{chinese}板"

    for machine, (english, chinese) in MACHINES.items():
        en[f"block.newgc.{machine}"] = english
        en[f"container.newgc.{machine}"] = english
        zh[f"block.newgc.{machine}"] = chinese
        zh[f"container.newgc.{machine}"] = chinese

    for metal, english, chinese in (("tin", "Tin", "锡"),
                                    ("aluminum", "Aluminum", "铝"),
                                    ("silver", "Silver", "银")):
        en[f"item.newgc.{metal}_nugget"] = f"{english} Nugget"
        zh[f"item.newgc.{metal}_nugget"] = f"{chinese}粒"

    for lang, additions in (("en_us.json", en), ("zh_cn.json", zh)):
        path = ASSETS / "lang" / lang
        data = read_json(path)
        data.update(additions)
        write_json(path, dict(sorted(data.items())))


def merge_tags() -> None:
    path = MINECRAFT_DATA / "tags/block/mineable/pickaxe.json"
    data = read_json(path)
    values = data.setdefault("values", [])
    additions = []
    for metal in BASE_METALS:
        additions.extend([f"newgc:{metal}_ore", f"newgc:{metal}_block"])
    for alloy in ALLOYS:
        additions.append(f"newgc:{alloy}_block")
    additions.extend([f"newgc:{machine}" for machine in MACHINES])
    for value in additions:
        if value not in values:
            values.append(value)
    write_json(path, data)


if __name__ == "__main__":
    generate_materials()
    generate_machines()
    generate_worldgen()
    merge_lang()
    merge_tags()
    print("Generated metal, alloy, machine, worldgen and language data.")
