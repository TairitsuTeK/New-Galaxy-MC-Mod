#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Regenerate New Galaxy ore / ingot / nugget / plate / circuit-core textures.

The script is deterministic: every texture uses a seed derived from its file name,
so repeated runs produce the same result. It backs up the current textures to
preview/textures_backup.zip on the first run and writes a 4x preview sheet to
preview/texture_preview.png.
"""

from __future__ import annotations

import math
import random
import shutil
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc/textures"
ITEM_DIR = ASSETS / "item"
BLOCK_DIR = ASSETS / "block"
ORE_DIR = BLOCK_DIR / "ore_overworld"
PREVIEW_DIR = ROOT / "preview"

# (base, light, dark, deep)
METALS = {
    "iron": ((184, 184, 184), (222, 222, 222), (132, 132, 132), (92, 92, 92)),
    "copper": ((196, 112, 72), (232, 150, 100), (140, 74, 46), (96, 48, 28)),
    "gold": ((230, 190, 70), (255, 226, 122), (170, 130, 40), (112, 82, 20)),
    "diamond": ((100, 220, 220), (162, 250, 250), (60, 160, 170), (34, 104, 114)),
    "lapis": ((60, 90, 190), (96, 132, 232), (38, 58, 130), (22, 34, 84)),
    "emerald": ((60, 200, 120), (112, 240, 160), (34, 140, 80), (20, 90, 50)),
    "netherite": ((90, 78, 76), (126, 110, 104), (60, 50, 48), (36, 28, 27)),
    "tin": ((190, 205, 212), (226, 238, 242), (134, 152, 160), (88, 104, 112)),
    "aluminum": ((205, 214, 220), (238, 244, 248), (150, 162, 170), (100, 112, 120)),
    "silver": ((210, 210, 222), (242, 242, 252), (154, 154, 172), (104, 104, 122)),
    "lead": ((125, 132, 145), (160, 168, 182), (90, 96, 108), (58, 62, 72)),
    "nickel": ((175, 175, 150), (210, 210, 186), (124, 124, 104), (82, 82, 68)),
    "platinum": ((205, 215, 215), (240, 248, 248), (150, 165, 165), (100, 115, 115)),
    "titanium": ((180, 190, 200), (220, 230, 240), (130, 140, 150), (84, 94, 104)),
    "uranium": ((90, 205, 90), (140, 240, 140), (54, 145, 54), (30, 90, 30)),
    "zinc": ((170, 190, 205), (210, 225, 236), (120, 140, 156), (78, 94, 108)),
    "bronze": ((176, 110, 54), (218, 150, 82), (128, 74, 34), (82, 46, 20)),
    "steel": ((130, 140, 150), (178, 188, 198), (92, 100, 110), (58, 64, 72)),
    "electrum": ((225, 195, 100), (255, 230, 150), (165, 135, 58), (110, 88, 30)),
    "invar": ((145, 150, 145), (180, 185, 178), (105, 110, 106), (68, 72, 70)),
}

PLANETS = {
    "moon": ((122, 122, 126), (150, 150, 154), (98, 98, 102), (72, 72, 76)),
    "mercury": ((112, 102, 94), (138, 126, 116), (88, 80, 74), (62, 56, 52)),
    "venus": ((188, 158, 92), (216, 188, 120), (150, 122, 66), (112, 88, 48)),
    "ceres": ((118, 112, 100), (146, 140, 124), (94, 88, 78), (68, 64, 56)),
    "mars": ((166, 88, 58), (196, 116, 80), (132, 64, 42), (92, 44, 30)),
    "proxima_b": ((148, 76, 62), (184, 108, 84), (112, 52, 44), (78, 36, 32)),
    "phobos": ((96, 90, 84), (120, 112, 104), (74, 70, 64), (52, 48, 44)),
    "deimos": ((106, 94, 78), (132, 118, 98), (82, 72, 60), (58, 50, 42)),
    "jupiter": ((176, 148, 104), (204, 176, 132), (142, 116, 78), (100, 80, 54)),
    "io": ((186, 168, 70), (214, 196, 98), (148, 132, 50), (108, 94, 36)),
    "earth": ((104, 126, 96), (132, 156, 120), (78, 96, 72), (54, 68, 50)),
    "callisto": ((104, 94, 84), (130, 118, 104), (80, 72, 64), (56, 50, 44)),
    "charon": ((124, 124, 132), (150, 150, 158), (98, 98, 106), (70, 70, 78)),
    "eris": ((148, 148, 160), (178, 178, 190), (116, 116, 128), (82, 82, 94)),
    "europa": ((188, 204, 220), (220, 232, 244), (150, 168, 186), (108, 126, 144)),
    "ganymede": ((130, 116, 98), (160, 146, 124), (102, 90, 76), (72, 62, 52)),
    "haumea": ((150, 144, 136), (180, 174, 164), (118, 112, 104), (82, 78, 72)),
    "hiiaka": ((110, 104, 100), (136, 130, 124), (84, 80, 76), (58, 54, 50)),
    "makemake": ((150, 110, 92), (180, 138, 116), (116, 84, 70), (78, 56, 46)),
    "neptune": ((70, 110, 180), (100, 145, 215), (50, 82, 140), (34, 56, 100)),
    "oberon": ((100, 96, 104), (126, 122, 132), (76, 72, 80), (52, 50, 56)),
    "pluto": ((130, 116, 104), (158, 142, 126), (102, 90, 80), (70, 62, 54)),
    "saturn": ((190, 170, 120), (220, 200, 150), (152, 134, 90), (108, 94, 62)),
    "titan": ((180, 140, 80), (210, 170, 105), (144, 110, 60), (102, 78, 42)),
    "titania": ((118, 114, 120), (146, 142, 150), (92, 88, 96), (64, 62, 68)),
    "triton": ((150, 140, 150), (180, 170, 180), (118, 110, 120), (80, 74, 84)),
    "uranus": ((110, 190, 200), (150, 225, 232), (80, 150, 160), (54, 108, 116)),
}

OVERWORLD_STONE = ((124, 124, 124), (150, 150, 150), (100, 100, 100), (76, 76, 76))

# 本轮新增内容没有预先准备贴图，由脚本生成。
EXTRA_ITEMS = [
    "bronze_ingot", "bronze_nugget", "bronze_plate",
    "steel_ingot", "steel_nugget", "steel_plate",
    "electrum_ingot", "electrum_nugget", "electrum_plate",
    "invar_ingot", "invar_nugget", "invar_plate",
]

EXTRA_BLOCKS = [
    "bronze_block", "steel_block", "electrum_block", "invar_block",
]

EXTRA_ORES = [
    "lead_ore", "nickel_ore", "platinum_ore",
    "titanium_ore", "uranium_ore", "zinc_ore",
]

MACHINE_ACCENTS = {
    "electric_furnace": ((255, 110, 40), (255, 180, 90)),
    "alloy_smelter": ((0, 210, 190), (120, 245, 230)),
    "energy_cell": ((0, 190, 255), (160, 235, 255)),
}


def clamp(value: float) -> int:
    return max(0, min(255, int(value)))


def shade(color, factor: float):
    return (clamp(color[0] * factor), clamp(color[1] * factor), clamp(color[2] * factor))


def mix(a, b, t: float):
    return (
        clamp(a[0] + (b[0] - a[0]) * t),
        clamp(a[1] + (b[1] - a[1]) * t),
        clamp(a[2] + (b[2] - a[2]) * t),
    )


def rng_for(name: str) -> random.Random:
    return random.Random(f"newgc-texture::{name}")


def material_for(name: str) -> str:
    if name.startswith("raw_"):
        return name[4:]
    if name.endswith("_block"):
        return name[:-6]
    if name.endswith("_nugget"):
        return name[:-7]
    if name.endswith("_ingot"):
        return name[:-6]
    if name.endswith("_plate"):
        return name[:-6]
    if name == "circuit_core_golden":
        return "gold"
    if name == "circuit_core_nether":
        return "netherite"
    if name.startswith("circuit_core_"):
        return name[len("circuit_core_"):]
    return name


def metal_palette(name: str):
    material = material_for(name)
    return METALS.get(material, METALS["iron"])


def planet_palette(name: str):
    key = name
    for suffix in ("_ore_iron", "_ore_copper", "_sub_surface", "_surface", "_stone", "_ore"):
        if key.endswith(suffix):
            key = key[:-len(suffix)]
            break
    if key in {"tin", "aluminum", "silver"}:
        return OVERWORLD_STONE
    return PLANETS.get(key, OVERWORLD_STONE)


def ore_palette(name: str):
    if name.endswith("_ore_iron"):
        return METALS["iron"]
    if name.endswith("_ore_copper"):
        return METALS["copper"]
    if name.endswith("_ore"):
        return METALS.get(name[:-4], METALS["iron"])
    return metal_palette(name)


def new_item() -> Image.Image:
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def new_block() -> Image.Image:
    return Image.new("RGBA", (16, 16), (0, 0, 0, 255))


def draw_raw(name: str) -> Image.Image:
    img = new_item()
    draw = ImageDraw.Draw(img)
    rng = rng_for(name)
    p = metal_palette(name)
    rock = (108, 100, 92)

    points = [(4, 11), (4, 6), (6, 4), (10, 3), (13, 6), (13, 10), (11, 13), (7, 13)]
    draw.polygon(points, fill=rock)
    draw.line(points + [points[0]], fill=shade(rock, 0.55))
    draw.line([(6, 5), (9, 4)], fill=shade(rock, 1.25))

    for _ in range(6):
        x = rng.randint(5, 11)
        y = rng.randint(5, 11)
        draw.rectangle([x, y, x + 1, y + 1], fill=p[0])
        draw.point((x, y), fill=p[1])
        if rng.random() < 0.5:
            draw.point((x + 1, y + 1), fill=p[2])

    return img


def draw_ingot(name: str) -> Image.Image:
    img = new_item()
    draw = ImageDraw.Draw(img)
    p = metal_palette(name)

    top = [(4, 5), (12, 5), (11, 3), (5, 3)]
    front = [(4, 5), (12, 5), (13, 11), (3, 11)]
    bottom = [(3, 11), (13, 11), (12, 13), (4, 13)]

    draw.polygon(top, fill=p[1])
    draw.polygon(front, fill=p[0])
    draw.polygon(bottom, fill=p[2])
    draw.line(top + [top[0]], fill=p[3])
    draw.line([(4, 5), (3, 11), (4, 13), (12, 13), (13, 11), (12, 5)], fill=p[3])
    draw.line([(5, 6), (11, 6)], fill=p[1])
    draw.line([(5, 7), (10, 7)], fill=shade(p[0], 1.1))
    draw.point((5, 4), fill=(255, 255, 255, 210))
    return img


def draw_nugget(name: str) -> Image.Image:
    img = new_item()
    draw = ImageDraw.Draw(img)
    p = metal_palette(name)

    points = [(5, 5), (9, 4), (12, 7), (11, 11), (7, 12), (4, 9)]
    draw.polygon(points, fill=p[0])
    draw.line(points + [points[0]], fill=p[3])
    draw.polygon([(6, 6), (9, 5), (10, 7), (8, 8), (5, 8)], fill=p[1])
    draw.point((6, 6), fill=(255, 255, 255, 220))
    draw.point((10, 9), fill=p[2])
    return img


def draw_plate(name: str) -> Image.Image:
    img = new_item()
    draw = ImageDraw.Draw(img)
    p = metal_palette(name)

    draw.rectangle([2, 3, 13, 12], fill=p[0])
    draw.rectangle([2, 3, 13, 4], fill=p[1])
    draw.rectangle([2, 11, 13, 12], fill=p[2])
    draw.rectangle([3, 4, 12, 11], outline=p[3])

    for x, y in [(4, 5), (11, 5), (4, 10), (11, 10)]:
        draw.point((x, y), fill=p[1])
        draw.point((x + 1, y + 1), fill=p[3])

    draw.line([(5, 6), (8, 5)], fill=(255, 255, 255, 180))
    draw.line([(8, 9), (11, 8)], fill=shade(p[1], 1.1))
    return img


def draw_circuit_core(name: str) -> Image.Image:
    img = new_item()
    draw = ImageDraw.Draw(img)
    p = metal_palette(name)

    draw.rectangle([2, 2, 13, 13], fill=(30, 45, 35))
    draw.rectangle([2, 2, 13, 3], fill=(70, 95, 72))
    draw.rectangle([2, 12, 13, 13], fill=(18, 28, 22))

    for y in (5, 8, 11):
        draw.line([(3, y), (5, y)], fill=(110, 160, 110))
        draw.line([(10, y), (12, y)], fill=(110, 160, 110))

    draw.rectangle([5, 5, 10, 10], fill=p[3])
    draw.rectangle([6, 6, 9, 9], fill=p[0])
    draw.rectangle([7, 7, 8, 8], fill=p[1])

    for x in (5, 7, 9):
        draw.point((x, 4), fill=(180, 180, 160))
        draw.point((x, 11), fill=(180, 180, 160))
    return img


def generate_item(name: str, path: Path) -> None:
    if name.startswith("raw_"):
        img = draw_raw(name)
    elif name.endswith("_ingot"):
        img = draw_ingot(name)
    elif name.endswith("_nugget"):
        img = draw_nugget(name)
    elif name.endswith("_plate"):
        img = draw_plate(name)
    elif name.startswith("circuit_core_"):
        img = draw_circuit_core(name)
    else:
        return
    img.save(path, optimize=True)


def terrain_base(name: str, palette, kind: str) -> Image.Image:
    img = new_block()
    draw = ImageDraw.Draw(img)
    rng = rng_for(name)

    for y in range(16):
        for x in range(16):
            idx = rng.choices([0, 1, 2, 3], weights=[45, 25, 20, 10])[0]
            color = palette[idx]
            if rng.random() < 0.12:
                color = shade(color, 0.9 if rng.random() < 0.5 else 1.1)
            draw.point((x, y), fill=color + (255,))

    if kind == "stone":
        for _ in range(5):
            x, y = rng.randint(1, 14), rng.randint(1, 14)
            for _ in range(rng.randint(2, 4)):
                if 0 <= x < 16 and 0 <= y < 16:
                    draw.point((x, y), fill=palette[3] + (255,))
                x += rng.choice([-1, 0, 1])
                y += rng.choice([-1, 0, 1])
    elif kind == "sub":
        for y in (5, 10, 14):
            for x in range(16):
                if rng.random() < 0.55:
                    draw.point((x, y), fill=palette[2] + (255,))
            for x in range(16):
                if rng.random() < 0.18:
                    draw.point((x, y + 1), fill=palette[3] + (255,))
    elif kind == "surface":
        for _ in range(7):
            x, y = rng.randint(1, 14), rng.randint(1, 14)
            draw.rectangle([x, y, x + 1, y + 1], fill=palette[1] + (255,))
            draw.point((x, y), fill=shade(palette[1], 1.15) + (255,))
    return img


def ore_block(name: str) -> Image.Image:
    stone = planet_palette(name)
    ore = ore_palette(name)
    img = terrain_base(name + "_base", stone, "stone")
    draw = ImageDraw.Draw(img)
    rng = rng_for(name + "_ore")

    for _ in range(5):
        cx, cy = rng.randint(2, 13), rng.randint(2, 13)
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                x, y = cx + dx, cy + dy
                if 0 <= x < 16 and 0 <= y < 16:
                    draw.point((x, y), fill=ore[3] + (255,))
        draw.rectangle([cx - 1, cy - 1, cx + 1, cy + 1], fill=ore[0] + (255,))
        draw.point((cx, cy), fill=ore[1] + (255,))
        draw.point((cx - 1, cy - 1), fill=ore[2] + (255,))
    return img


def metal_block(name: str) -> Image.Image:
    p = metal_palette(name)
    img = new_block()
    draw = ImageDraw.Draw(img)

    draw.rectangle([0, 0, 15, 15], fill=p[3] + (255,))
    draw.rectangle([1, 1, 14, 14], fill=p[0] + (255,))
    draw.rectangle([1, 1, 14, 2], fill=p[1] + (255,))
    draw.rectangle([1, 13, 14, 14], fill=p[2] + (255,))
    draw.rectangle([3, 3, 12, 12], fill=shade(p[0], 1.05) + (255,))
    draw.rectangle([2, 2, 13, 13], outline=p[3] + (255,))

    for x, y in [(2, 2), (12, 2), (2, 12), (12, 12)]:
        draw.point((x, y), fill=p[1] + (255,))
        draw.point((x + 1, y + 1), fill=p[3] + (255,))

    draw.line([(4, 4), (11, 4)], fill=p[1] + (255,))
    draw.line([(4, 5), (8, 5)], fill=(255, 255, 255, 120))
    return img


def moon_soil() -> Image.Image:
    base = PLANETS["moon"]
    img = terrain_base("moon_soil_base", base, "surface")
    draw = ImageDraw.Draw(img)
    rng = rng_for("moon_soil_craters")
    for _ in range(6):
        cx, cy = rng.randint(2, 13), rng.randint(2, 13)
        r = rng.randint(1, 2)
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=base[3] + (255,))
        draw.ellipse([cx - r + 1, cy - r + 1, cx + r - 1, cy + r - 1],
                     fill=base[2] + (255,))
    return img


def sulfur_block() -> Image.Image:
    base = ((190, 180, 70), (220, 210, 100), (150, 140, 50), (110, 100, 34))
    img = terrain_base("sulfur_base", base, "surface")
    draw = ImageDraw.Draw(img)
    rng = rng_for("sulfur_crystals")
    for _ in range(7):
        x, y = rng.randint(2, 12), rng.randint(2, 12)
        draw.polygon([(x, y), (x + 2, y + 1), (x + 1, y + 3), (x - 1, y + 2)],
                     fill=base[1] + (255,))
        draw.point((x, y), fill=(255, 255, 190, 255))
    return img


def jupiter_gravel() -> Image.Image:
    base = PLANETS["jupiter"]
    img = terrain_base("jupiter_gravel_base", base, "surface")
    draw = ImageDraw.Draw(img)
    rng = rng_for("jupiter_gravel_pebbles")
    for _ in range(8):
        x, y = rng.randint(1, 13), rng.randint(1, 13)
        draw.rectangle([x, y, x + 1, y + 1], fill=base[2] + (255,))
        draw.point((x, y), fill=base[1] + (255,))
    return img


def machine_texture(name: str) -> Image.Image:
    """为新增机器生成统一的 16x16 金属机壳贴图。"""
    dark = (24, 30, 36)
    panel = (62, 72, 82)
    highlight = (112, 124, 136)
    shadow = (42, 50, 58)
    accent, glow = MACHINE_ACCENTS[name]

    img = new_block()
    draw = ImageDraw.Draw(img)
    draw.rectangle([0, 0, 15, 15], fill=dark + (255,))
    draw.rectangle([1, 1, 14, 14], fill=panel + (255,))
    draw.rectangle([1, 1, 14, 2], fill=highlight + (255,))
    draw.rectangle([1, 13, 14, 14], fill=shadow + (255,))
    draw.rectangle([2, 3, 13, 12], fill=dark + (255,))
    draw.rectangle([2, 3, 13, 4], fill=accent + (255,))

    for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        draw.point((x, y), fill=highlight + (255,))
        draw.point((x + 1, y + 1), fill=shadow + (255,))

    if name == "electric_furnace":
        for y in (6, 8, 10):
            draw.line([(5, y), (10, y)], fill=accent + (255,))
            draw.point((10, y), fill=glow + (255,))
        draw.rectangle([4, 11, 11, 11], fill=shadow + (255,))
    elif name == "alloy_smelter":
        draw.ellipse([4, 5, 7, 8], fill=accent + (255,))
        draw.ellipse([8, 5, 11, 8], fill=glow + (255,))
        draw.rectangle([5, 9, 10, 11], fill=shadow + (255,))
        draw.line([(5, 10), (10, 10)], fill=accent + (255,))
    else:
        for y in (6, 8, 10):
            draw.rectangle([6, y, 9, y + 1], fill=accent + (255,))
            draw.point((7, y), fill=glow + (255,))
        draw.rectangle([5, 4, 10, 5], fill=glow + (255,))

    return img


MOON_V2_TEXTURES = {
    "moon_soil", "moon_sub_surface", "moon_stone",
    "moon_ore_iron", "moon_ore_copper",
}


def generate_block(name: str, path: Path) -> None:
    # 月球自然方块由 tools/generate_moon_textures.py 单独维护，避免被旧版
    # 批量生成器覆盖成 16x16 的旧月壤。
    if name in MOON_V2_TEXTURES:
        return
    if name.endswith("_ore_iron") or name.endswith("_ore_copper"):
        img = ore_block(name)
    elif name.endswith("_stone"):
        img = terrain_base(name, planet_palette(name), "stone")
    elif name.endswith("_sub_surface"):
        img = terrain_base(name, planet_palette(name), "sub")
    elif name.endswith("_surface"):
        img = terrain_base(name, planet_palette(name), "surface")
    elif name.endswith("_block"):
        img = metal_block(name)
    elif name == "moon_soil":
        img = moon_soil()
    elif name == "sulfur":
        img = sulfur_block()
    elif name == "jupiter_gravel":
        img = jupiter_gravel()
    elif name in {"tin_ore", "aluminum_ore", "silver_ore"} or name.endswith("_ore"):
        img = ore_block(name)
    else:
        return
    img.save(path, optimize=True)


def backup_textures() -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    backup = PREVIEW_DIR / "textures_backup.zip"
    if backup.exists():
        print(f"Backup already exists: {backup}")
        return
    if not ASSETS.exists():
        raise SystemExit(f"Texture directory not found: {ASSETS}")
    shutil.make_archive(str(PREVIEW_DIR / "textures_backup"), "zip", root_dir=str(ASSETS))
    print(f"Backup written to {backup}")


def write_preview(items, blocks) -> None:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    entries = items + blocks
    tile = 56
    label_h = 10
    cols = 10
    pad = 6

    rows = (len(entries) + cols - 1) // cols
    width = cols * (tile + pad) + pad
    height = rows * (tile + label_h + pad) + pad + 16
    sheet = Image.new("RGBA", (width, height), (12, 16, 24, 255))
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype("C:/Windows/Fonts/consola.ttf", 8)
    except Exception:
        font = ImageFont.load_default()

    draw.text((pad, 4), "New Galaxy regenerated textures (4x preview)",
              fill=(200, 230, 245, 255), font=font)

    for index, (name, image) in enumerate(entries):
        col = index % cols
        row = index // cols
        x = pad + col * (tile + pad)
        y = 16 + pad + row * (tile + label_h + pad)

        for cy in range(0, tile, 8):
            for cx in range(0, tile, 8):
                color = (34, 40, 50, 255) if ((cx // 8 + cy // 8) % 2 == 0) else (42, 50, 62, 255)
                draw.rectangle([x + cx, y + cy, x + cx + 7, y + cy + 7], fill=color)

        sheet.alpha_composite(image.resize((tile, tile), Image.Resampling.NEAREST), (x, y))
        draw.text((x, y + tile + 1), name, fill=(200, 220, 235, 255), font=font)

    sheet.save(PREVIEW_DIR / "texture_preview.png", optimize=True)
    print(f"Preview written to {PREVIEW_DIR / 'texture_preview.png'}")


def generate_all() -> None:
    backup_textures()

    items = []
    generated_items = set()
    for path in sorted(ITEM_DIR.glob("*.png")):
        name = path.stem
        if not (name.startswith("raw_")
                or name.endswith("_ingot")
                or name.endswith("_nugget")
                or name.endswith("_plate")
                or name.startswith("circuit_core_")):
            continue
        generate_item(name, path)
        generated_items.add(name)
        items.append((name, Image.open(path).copy()))

    for name in EXTRA_ITEMS:
        path = ITEM_DIR / f"{name}.png"
        if name not in generated_items:
            generate_item(name, path)
            items.append((name, Image.open(path).copy()))

    block_names = set()
    for path in sorted(BLOCK_DIR.glob("*.png")):
        name = path.stem
        if (name.endswith("_ore_iron")
                or name.endswith("_ore_copper")
                or name.endswith("_stone")
                or name.endswith("_sub_surface")
                or name.endswith("_surface")
                or name.endswith("_block")
                or name in {"moon_soil", "sulfur", "jupiter_gravel"}):
            block_names.add(name)
    block_names.update(EXTRA_BLOCKS)

    blocks = []
    for name in sorted(block_names):
        path = BLOCK_DIR / f"{name}.png"
        generate_block(name, path)
        blocks.append((name, Image.open(path).copy()))

    for name in MACHINE_ACCENTS:
        path = BLOCK_DIR / f"{name}.png"
        machine_texture(name).save(path, optimize=True)
        blocks.append((name, Image.open(path).copy()))

    if ORE_DIR.exists():
        ore_names = set()
        for path in sorted(ORE_DIR.glob("*.png")):
            name = path.stem
            if name.endswith("_ore_iron") or name.endswith("_ore_copper") or name.endswith("_ore"):
                ore_names.add(name)
        ore_names.update(EXTRA_ORES)
        for name in sorted(ore_names):
            path = ORE_DIR / f"{name}.png"
            generate_block(name, path)
            blocks.append(("ore_overworld/" + name, Image.open(path).copy()))

    write_preview(items, blocks)
    print(f"Generated {len(items)} item textures and {len(blocks)} block textures.")


if __name__ == "__main__":
    generate_all()
