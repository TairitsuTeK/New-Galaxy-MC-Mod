#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成月球自然方块的新版 32x32 月壤/月岩/矿石贴图。"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
BLOCK_DIR = ROOT / "src/main/resources/assets/newgc/textures/block"


def noisy_base(seed: str, top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for y in range(32):
        t = y / 31.0
        color = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)) + (255,)
        draw.line([(0, y), (31, y)], fill=color)
    rng = random.Random("newgc-moon-v2::" + seed)
    pixels = img.load()
    for y in range(32):
        for x in range(32):
            d = rng.randint(-10, 10)
            r, g, b, a = pixels[x, y]
            pixels[x, y] = (max(0, min(255, r + d)),
                            max(0, min(255, g + d)),
                            max(0, min(255, b + d)), a)
    return img


def crater(draw: ImageDraw.ImageDraw, cx: int, cy: int, radius: int,
           dark: tuple[int, int, int], light: tuple[int, int, int]) -> None:
    draw.ellipse([cx - radius, cy - radius, cx + radius, cy + radius],
                 fill=dark + (255,), outline=light + (255,))
    if radius >= 3:
        draw.ellipse([cx - radius + 1, cy - radius + 1,
                      cx + radius - 1, cy + radius - 1],
                     fill=tuple(max(0, c - 6) for c in dark) + (255,))
    draw.arc([cx - radius, cy - radius, cx + radius, cy + radius],
             195, 330, fill=light + (220,))


def regolith(seed: str, top, bottom) -> Image.Image:
    img = noisy_base(seed, top, bottom)
    draw = ImageDraw.Draw(img)
    rng = random.Random("newgc-moon-craters::" + seed)
    for _ in range(12):
        x, y = rng.randint(1, 30), rng.randint(1, 30)
        r = rng.choice((1, 1, 2, 2, 3))
        crater(draw, x, y, r, (105, 109, 116), (164, 168, 174))
    for _ in range(38):
        x, y = rng.randint(0, 31), rng.randint(0, 31)
        draw.point((x, y), fill=(rng.randint(80, 180), rng.randint(82, 182),
                                 rng.randint(86, 188), 180))
    return img


def stone() -> Image.Image:
    img = regolith("moon_stone", (94, 99, 108), (48, 52, 60))
    draw = ImageDraw.Draw(img)
    # 月海暗斑与裂缝
    for cx, cy, r in ((9, 23, 7), (24, 8, 5), (22, 25, 3)):
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(43, 47, 55, 230))
    for x, y, dx, dy in ((4, 19, 13, 22), (19, 4, 26, 15), (15, 27, 25, 30)):
        draw.line([(x, y), (dx, dy)], fill=(28, 31, 37, 180), width=1)
    return img


def sub_surface() -> Image.Image:
    img = regolith("moon_sub", (116, 120, 128), (66, 70, 79))
    draw = ImageDraw.Draw(img)
    rng = random.Random("newgc-moon-strata")
    # 断续的压实层理，避免出现整张图重复的横条
    for y in (8, 16, 24):
        x = 0
        while x < 32:
            run = rng.randint(3, 9)
            if rng.random() < 0.7:
                draw.line([(x, y), (min(31, x + run), y)], fill=(42, 47, 56, 90))
                draw.line([(x, y + 1), (min(31, x + run), y + 1)],
                          fill=(172, 178, 186, 32))
            x += run + rng.randint(2, 6)
    for x, y in ((6, 13), (24, 22), (12, 28)):
        draw.ellipse([x - 2, y - 1, x + 2, y + 1], fill=(52, 57, 66, 130))
    return img


def ore(base: Image.Image, seed: str, metal: tuple[int, int, int],
        highlight: tuple[int, int, int], count: int) -> Image.Image:
    img = base.copy()
    draw = ImageDraw.Draw(img)
    rng = random.Random("newgc-moon-ore::" + seed)
    for _ in range(count):
        x, y = rng.randint(4, 27), rng.randint(4, 27)
        r = rng.randint(1, 3)
        draw.ellipse([x - r, y - r, x + r, y + r], fill=metal + (255,))
        draw.ellipse([x - r, y - r, x - r + 2, y - r + 2], fill=highlight + (230,))
        if r > 1:
            draw.line([(x + 1, y + 1), (x + 3, y + 2)], fill=highlight + (150,))
    return img


def main() -> None:
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    soil = regolith("moon_soil", (176, 179, 181), (118, 122, 127))
    sub = sub_surface()
    rock = stone()
    outputs = {
        "moon_soil.png": soil,
        "moon_sub_surface.png": sub,
        "moon_stone.png": rock,
        "moon_ore_iron.png": ore(rock, "iron", (164, 126, 92), (225, 194, 148), 14),
        "moon_ore_copper.png": ore(rock, "copper", (50, 151, 142), (140, 235, 211), 14),
    }
    for name, image in outputs.items():
        image.save(BLOCK_DIR / name, optimize=True)
        print(f"wrote textures/block/{name} ({image.width}x{image.height})")


if __name__ == "__main__":
    main()
