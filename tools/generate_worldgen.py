#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate all New Galaxy dimensions, dimension types, biomes and ore features.

The base noise settings are copied from tools/templates/noise_settings_base.json
(the original moon settings). Each dimension gets a different horizontal/vertical
noise scale and a different base/squeeze density, so terrain shape is no longer
identical between planets.
"""

from __future__ import annotations

import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/newgc"
TEMPLATES = ROOT / "tools/templates"

NEW_IDS = {
    "saturn", "uranus", "neptune", "pluto", "makemake", "haumea", "eris",
    "europa", "ganymede", "callisto", "titan", "titania", "oberon",
    "triton", "charon", "hiiaka",
}

NEW_MOON_IDS = {
    "europa", "ganymede", "callisto", "titan", "titania", "oberon",
    "triton", "charon", "hiiaka",
}

DWARF_IDS = {"pluto", "makemake", "haumea", "eris"}

DIMENSIONS = [
    dict(id="moon", surface="moon_soil", sub="moon_sub_surface", stone="moon_stone",
         effects="moon_sky", h=0.90, v=2.0, base=0.100, squeeze=0.55,
         sea=0, fluid="air", sky=True, warm=False, light=0.05, temp=2.0),
    dict(id="mercury", surface="mercury_surface", sub="mercury_sub_surface", stone="mercury_stone",
         effects="mercury_sky", h=1.15, v=2.4, base=0.110, squeeze=0.65,
         sea=0, fluid="air", sky=True, warm=True, light=0.05, temp=3.0),
    dict(id="venus", surface="venus_surface", sub="venus_sub_surface", stone="venus_stone",
         effects="venus_sky", h=1.00, v=2.2, base=0.120, squeeze=0.75,
         sea=63, fluid="lava", sky=True, warm=True, light=0.90, temp=4.0),
    dict(id="ceres", surface="ceres_surface", sub="ceres_sub_surface", stone="ceres_stone",
         effects="ceres_sky", h=0.75, v=1.4, base=0.080, squeeze=0.40,
         sea=0, fluid="air", sky=True, warm=False, light=0.05, temp=1.0),
    dict(id="mars", surface="mars_surface", sub="mars_sub_surface", stone="mars_stone",
         effects="mars_sky", h=1.25, v=2.6, base=0.130, squeeze=0.70,
         sea=0, fluid="air", sky=True, warm=False, light=0.05, temp=1.0),
    dict(id="phobos", surface="phobos_surface", sub="phobos_sub_surface", stone="phobos_stone",
         effects="phobos_sky", h=0.65, v=1.1, base=0.060, squeeze=0.35,
         sea=0, fluid="air", sky=True, warm=False, light=0.05, temp=1.0),
    dict(id="deimos", surface="deimos_surface", sub="deimos_sub_surface", stone="deimos_stone",
         effects="deimos_sky", h=0.60, v=1.0, base=0.055, squeeze=0.30,
         sea=0, fluid="air", sky=True, warm=False, light=0.05, temp=1.0),
    dict(id="jupiter", surface="jupiter_surface", sub="jupiter_sub_surface", stone="jupiter_stone",
         effects="jupiter_sky", h=2.00, v=3.6, base=0.160, squeeze=0.85,
         sea=0, fluid="air", sky=True, warm=False, light=0.08, temp=1.5),
    dict(id="io", surface="sulfur", sub="io_sub_surface", stone="io_stone",
         effects="io_sky", h=1.45, v=2.9, base=0.140, squeeze=0.80,
         sea=0, fluid="air", sky=True, warm=True, light=0.08, temp=2.0),
    dict(id="saturn", surface="saturn_surface", sub="saturn_sub_surface", stone="saturn_stone",
         effects="generic_planet_sky", h=1.80, v=3.2, base=0.150, squeeze=0.80,
         sea=0, fluid="air", sky=True, warm=False, light=0.08, temp=1.0),
    dict(id="uranus", surface="uranus_surface", sub="uranus_sub_surface", stone="uranus_stone",
         effects="generic_planet_sky", h=1.50, v=2.8, base=0.120, squeeze=0.65,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.8),
    dict(id="neptune", surface="neptune_surface", sub="neptune_sub_surface", stone="neptune_stone",
         effects="generic_planet_sky", h=1.60, v=3.0, base=0.130, squeeze=0.70,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.8),
    dict(id="pluto", surface="pluto_surface", sub="pluto_sub_surface", stone="pluto_stone",
         effects="pluto_sky", h=0.70, v=1.2, base=0.110, squeeze=0.65,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.5),
    dict(id="makemake", surface="makemake_surface", sub="makemake_sub_surface", stone="makemake_stone",
         effects="makemake_sky", h=0.65, v=1.1, base=0.110, squeeze=0.65,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.4),
    dict(id="haumea", surface="haumea_surface", sub="haumea_sub_surface", stone="haumea_stone",
         effects="haumea_sky", h=0.60, v=1.0, base=0.110, squeeze=0.65,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.4),
    dict(id="eris", surface="eris_surface", sub="eris_sub_surface", stone="eris_stone",
         effects="eris_sky", h=0.75, v=1.3, base=0.110, squeeze=0.65,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.5),
    dict(id="europa", surface="europa_surface", sub="europa_sub_surface", stone="europa_stone",
         effects="generic_planet_sky", h=0.80, v=1.5, base=0.080, squeeze=0.45,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.6),
    dict(id="ganymede", surface="ganymede_surface", sub="ganymede_sub_surface", stone="ganymede_stone",
         effects="generic_planet_sky", h=1.00, v=1.8, base=0.090, squeeze=0.50,
         sea=0, fluid="air", sky=True, warm=False, light=0.08, temp=0.8),
    dict(id="callisto", surface="callisto_surface", sub="callisto_sub_surface", stone="callisto_stone",
         effects="generic_planet_sky", h=0.90, v=1.6, base=0.085, squeeze=0.48,
         sea=0, fluid="air", sky=True, warm=False, light=0.08, temp=0.7),
    dict(id="titan", surface="titan_surface", sub="titan_sub_surface", stone="titan_stone",
         effects="generic_planet_sky", h=1.10, v=2.0, base=0.100, squeeze=0.55,
         sea=0, fluid="air", sky=True, warm=False, light=0.08, temp=0.9),
    dict(id="titania", surface="titania_surface", sub="titania_sub_surface", stone="titania_stone",
         effects="generic_planet_sky", h=0.80, v=1.4, base=0.070, squeeze=0.40,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.5),
    dict(id="oberon", surface="oberon_surface", sub="oberon_sub_surface", stone="oberon_stone",
         effects="generic_planet_sky", h=0.75, v=1.3, base=0.065, squeeze=0.38,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.5),
    dict(id="triton", surface="triton_surface", sub="triton_sub_surface", stone="triton_stone",
         effects="generic_planet_sky", h=0.85, v=1.5, base=0.075, squeeze=0.42,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.5),
    dict(id="charon", surface="charon_surface", sub="charon_sub_surface", stone="charon_stone",
         effects="generic_planet_sky", h=0.70, v=1.2, base=0.060, squeeze=0.35,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.4),
    dict(id="hiiaka", surface="hiiaka_surface", sub="hiiaka_sub_surface", stone="hiiaka_stone",
         effects="generic_planet_sky", h=0.60, v=1.0, base=0.050, squeeze=0.30,
         sea=0, fluid="air", sky=True, warm=False, light=0.10, temp=0.4),
    # Alpha Centauri system: tidally locked super-Earth around Proxima Centauri.
    dict(id="proxima_b", surface="proxima_b_surface", sub="proxima_b_sub_surface",
         stone="proxima_b_stone", effects="pluto_sky", h=1.10, v=2.2,
         base=0.115, squeeze=0.62, sea=0, fluid="air", sky=True,
         warm=False, light=0.08, temp=1.2),
]

# The noise settings codec only accepts integer scale values in [1, 4].
# Use all 15 usable combinations across the 25 bodies; the remaining variation
# comes from the per-body density constants below.
SIZE_PAIRS = {
    "moon": (1, 2),
    "mercury": (2, 3),
    "venus": (1, 3),
    "ceres": (1, 1),
    "mars": (2, 4),
    "phobos": (1, 1),
    "deimos": (1, 1),
    "jupiter": (3, 4),
    "io": (3, 3),
    "saturn": (4, 4),
    "uranus": (3, 2),
    "neptune": (4, 3),
    "pluto": (1, 2),
    "makemake": (1, 1),
    "haumea": (1, 1),
    "eris": (1, 2),
    "europa": (2, 1),
    "ganymede": (2, 2),
    "callisto": (2, 1),
    "titan": (2, 2),
    "titania": (1, 2),
    "oberon": (1, 1),
    "triton": (3, 1),
    "charon": (4, 1),
    "hiiaka": (4, 2),
    "proxima_b": (1, 1),
}

# 1.21.1's chunk section lookup is not safe when the noise cell height is
# larger than 4 with min_y=-64 (it can start one cell below the chunk). Keep
# the cell size at 1x1 and create terrain differences through the per-body
# base/squeeze density constants instead.
SIZE_PAIRS = {planet: (1, 1) for planet in SIZE_PAIRS}

# Per-body vertical density offset. Positive values lift the surface, negative
# values lower it. This is what makes the terrain profile visibly different
# even though the 1x1 noise cell size is shared.
DENSITY_OFFSETS = {
    "moon": -0.04, "mercury": -0.02, "venus": 0.0, "ceres": -0.10,
    "mars": -0.03, "phobos": -0.16, "deimos": -0.18, "jupiter": -0.02,
    "io": -0.05, "saturn": -0.03, "uranus": -0.08, "neptune": -0.05,
    "pluto": 0.0, "makemake": 0.0, "haumea": 0.0, "eris": 0.0,
    "europa": -0.06, "ganymede": -0.02, "callisto": -0.04, "titan": 0.0,
    "titania": -0.08, "oberon": -0.10, "triton": -0.05, "charon": -0.12,
    "hiiaka": -0.16,
    "proxima_b": -0.02,
}


def replace_strings(obj, mapping):
    if isinstance(obj, dict):
        for key, value in list(obj.items()):
            if isinstance(value, str) and value in mapping:
                obj[key] = mapping[value]
            else:
                replace_strings(value, mapping)
    elif isinstance(obj, list):
        for index, value in enumerate(obj):
            if isinstance(value, str) and value in mapping:
                obj[index] = mapping[value]
            else:
                replace_strings(value, mapping)
    return obj


def load(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def save(path: Path, data) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, ensure_ascii=False)
        handle.write("\n")


def replace_density_values(obj, base: float, squeeze: float):
    """Replace the original overworld density constants in a density function."""
    mapping = {
        0.1171875: base,
        -0.1171875: -base,
        0.078125: base * 2.0 / 3.0,
        -0.078125: -base * 2.0 / 3.0,
        0.64: squeeze,
    }

    if isinstance(obj, dict):
        for key, value in list(obj.items()):
            if isinstance(value, (int, float)) and not isinstance(value, bool):
                for source, target in mapping.items():
                    if abs(float(value) - source) < 1e-9:
                        obj[key] = target
                        break
            else:
                replace_density_values(value, base, squeeze)
    elif isinstance(obj, list):
        for index, value in enumerate(list(obj)):
            if isinstance(value, (int, float)) and not isinstance(value, bool):
                for source, target in mapping.items():
                    if abs(float(value) - source) < 1e-9:
                        obj[index] = target
                        break
            else:
                replace_density_values(value, base, squeeze)
    return obj


def build_noise(dim: dict):
    noise = copy.deepcopy(load(TEMPLATES / "noise_settings_base.json"))
    noise["default_block"]["Name"] = f"newgc:{dim['stone']}"
    noise["default_fluid"]["Name"] = (
        "minecraft:lava" if dim["fluid"] == "lava" else "minecraft:air"
    )
    noise["sea_level"] = dim["sea"]
    horizontal, vertical = SIZE_PAIRS[dim["id"]]
    noise["noise"]["size_horizontal"] = horizontal
    noise["noise"]["size_vertical"] = vertical
    replace_density_values(
        noise["noise_router"]["final_density"], dim["base"], dim["squeeze"]
    )
    replace_density_values(
        noise["noise_router"]["initial_density_without_jaggedness"],
        dim["base"], dim["squeeze"],
    )
    offset = DENSITY_OFFSETS[dim["id"]]
    if offset:
        for key in ("final_density", "initial_density_without_jaggedness"):
            original = noise["noise_router"][key]
            noise["noise_router"][key] = {
                "type": "minecraft:add",
                "argument1": offset,
                "argument2": original,
            }
    # Dwarf planets must stay continuous, non-floating worlds. The normal
    # overworld density profile is enough there; the extra wave is only used
    # on the larger planets/moons.
    if dim["id"] not in DWARF_IDS:
        wave_sources = (
            "minecraft:overworld/continents",
            "minecraft:overworld/erosion",
            "minecraft:overworld/ridges",
        )
        wave_source = wave_sources[sum(ord(c) for c in dim["id"]) % len(wave_sources)]
        wave_amplitude = 0.02 + abs(dim["squeeze"] - 0.5) * 0.08
        wave = {
            "type": "minecraft:mul",
            "argument1": wave_amplitude,
            "argument2": wave_source,
        }
        for key in ("final_density", "initial_density_without_jaggedness"):
            noise["noise_router"][key] = {
                "type": "minecraft:add",
                "argument1": wave,
                "argument2": noise["noise_router"][key],
            }
    replace_strings(noise["surface_rule"], {
        "newgc:moon_soil": f"newgc:{dim['surface']}",
        "newgc:moon_sub_surface": f"newgc:{dim['sub']}",
    })
    return noise


def build_dimension_type(dim: dict):
    # Every newly added body has its own Java sky class and effect id.
    effects = f"{dim['id']}_sky" if dim["id"] in NEW_IDS else dim["effects"]
    return {
        # Use Overworld-style rules for every custom body. The previous
        # Nether-style natural/bed/piglin/raids/ambient values made the new
        # dimensions feel like Nether dimensions even with a custom sky.
        "ultrawarm": False,
        "natural": True,
        "coordinate_scale": 1.0,
        "has_skylight": True,
        "has_ceiling": False,
        "ambient_light": 0.0,
        "monster_spawn_light_level": {
            "type": "minecraft:uniform",
            "min_inclusive": 0,
            "max_inclusive": 7,
        },
        "monster_spawn_block_light_limit": 0,
        "piglin_safe": False,
        "bed_works": True,
        "respawn_anchor_works": False,
        "cloud_height": None,
        "has_raids": True,
        "logical_height": 384,
        "min_y": -64,
        "height": 384,
        "infiniburn": "#minecraft:infiniburn_overworld",
        "effects": f"newgc:{effects}",
    }


LAKES = {
    "venus": ["newgc:venus_acid_lake", "newgc:venus_lava_lake"],
    "io": ["newgc:io_lava_lake"],
    "jupiter": ["newgc:jupiter_lava_lake"],
}


def build_biome(dim: dict):
    biome = copy.deepcopy(load(TEMPLATES / "biome_base.json"))
    biome["temperature"] = dim["temp"]
    biome["downfall"] = 0.0
    biome["has_precipitation"] = False
    biome["effects"] = {
        "sky_color": 0,
        "fog_color": 0,
        "water_color": 4159204,
        "water_fog_color": 329011,
    }
    features = [[] for _ in range(11)]
    features[6].append(f"newgc:{dim['id']}_ore_iron")
    features[6].append(f"newgc:{dim['id']}_ore_copper")
    features[6].extend(LAKES.get(dim["id"], []))
    biome["features"] = features
    return biome


def build_ore_feature(dim: dict, metal: str):
    ore_name = f"{dim['id']}_ore_{metal}"
    target_blocks = [dim["stone"], dim["sub"], dim["surface"]]
    targets = []
    for block_name in target_blocks:
        targets.append({
            "target": {
                "predicate_type": "minecraft:block_match",
                "block": f"newgc:{block_name}",
            },
            "state": {"Name": f"newgc:{ore_name}"},
        })
    configured = {
        "type": "minecraft:ore",
        "config": {
            "size": 9,
            "discard_chance_on_air_exposure": 0.0,
            "targets": targets,
        },
    }
    placed = {
        "feature": f"newgc:{ore_name}",
        "placement": [
            {"type": "minecraft:count", "count": 16},
            {"type": "minecraft:in_square"},
            {
                "type": "minecraft:height_range",
                "height": {
                    "type": "minecraft:uniform",
                    "min_inclusive": {"absolute": -64},
                    "max_inclusive": {"absolute": 256},
                },
            },
            {"type": "minecraft:biome"},
        ],
    }
    return configured, placed


def build_dimension(dim: dict):
    return {
        "type": f"newgc:{dim['id']}_type",
        "generator": {
            "type": "minecraft:noise",
            "biome_source": {
                "type": "minecraft:fixed",
                "biome": f"newgc:{dim['id']}_plains",
            },
            "settings": f"newgc:{dim['id']}_overworld",
        },
    }



def validate(dims):
    assert len(dims) == 26, len(dims)
    ids = {dim["id"] for dim in dims}
    assert NEW_IDS <= ids, NEW_IDS - ids
    known_blocks = {
        "moon_soil", "moon_sub_surface", "moon_stone",
        "mercury_surface", "mercury_sub_surface", "mercury_stone",
        "venus_surface", "venus_sub_surface", "venus_stone",
        "ceres_surface", "ceres_sub_surface", "ceres_stone",
        "mars_surface", "mars_sub_surface", "mars_stone",
        "phobos_surface", "phobos_sub_surface", "phobos_stone",
        "deimos_surface", "deimos_sub_surface", "deimos_stone",
        "jupiter_surface", "jupiter_sub_surface", "jupiter_stone",
        "sulfur", "io_sub_surface", "io_stone",
    }
    for dim in dims:
        for suffix in ("surface", "sub_surface", "stone", "ore_iron", "ore_copper"):
            known_blocks.add(f"{dim['id']}_{suffix}")
    for dim in dims:
        for block_name in (dim["surface"], dim["sub"], dim["stone"],
                           f"{dim['id']}_ore_iron", f"{dim['id']}_ore_copper"):
            assert block_name in known_blocks, block_name
        assert dim["fluid"] in {"air", "lava"}, dim["fluid"]
    return known_blocks


def main():
    validate(DIMENSIONS)
    dims_dir = DATA / "dimension"
    type_dir = DATA / "dimension_type"
    noise_dir = DATA / "worldgen/noise_settings"
    biome_dir = DATA / "worldgen/biome"
    configured_dir = DATA / "worldgen/configured_feature"
    placed_dir = DATA / "worldgen/placed_feature"

    for dim in DIMENSIONS:
        save(dims_dir / f"{dim['id']}.json", build_dimension(dim))
        save(type_dir / f"{dim['id']}_type.json", build_dimension_type(dim))
        save(noise_dir / f"{dim['id']}_overworld.json", build_noise(dim))
        save(biome_dir / f"{dim['id']}_plains.json", build_biome(dim))
        for metal in ("iron", "copper"):
            configured, placed = build_ore_feature(dim, metal)
            save(configured_dir / f"{dim['id']}_ore_{metal}.json", configured)
            save(placed_dir / f"{dim['id']}_ore_{metal}.json", placed)

    print(f"generated {len(DIMENSIONS)} dimensions "
          f"({len(NEW_IDS)} new), {len(DIMENSIONS) * 2} ore features")
    print("new ids:", ", ".join(sorted(NEW_IDS)))


if __name__ == "__main__":
    main()
