#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成火箭组装台的 32x32 方块贴图。"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
BLOCK_DIR = ROOT / "src/main/resources/assets/newgc/textures/block"


def noise(img: Image.Image, seed: str, amount: int = 5) -> None:
    rng = random.Random("newgc-rocket-assembler::" + seed)
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            d = rng.randint(-amount, amount)
            pixels[x, y] = (max(0, min(255, r + d)),
                            max(0, min(255, g + d)),
                            max(0, min(255, b + d)), a)


def side() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (26, 32, 40, 255))
    draw = ImageDraw.Draw(img)
    for y in range(32):
        shade = int(24 + 16 * y / 31)
        draw.line([(0, y), (31, y)], fill=(shade, shade + 6, shade + 13, 255))
    # 外框与竖向面板
    draw.rectangle([1, 1, 30, 30], outline=(83, 96, 110, 255), width=2)
    draw.line([(10, 3), (10, 29)], fill=(48, 58, 70, 255), width=2)
    draw.line([(21, 3), (21, 29)], fill=(48, 58, 70, 255), width=2)
    # 青色状态灯
    draw.rectangle([3, 5, 7, 7], fill=(44, 206, 232, 255))
    draw.rectangle([24, 5, 27, 7], fill=(44, 206, 232, 255))
    # 下部橙色危险条
    for x in range(3, 30, 8):
        draw.polygon([(x, 24), (x + 5, 24), (x + 1, 29), (x - 4, 29)],
                     fill=(228, 151, 48, 255))
    # 铆钉
    for x, y in ((3, 3), (28, 3), (3, 28), (28, 28)):
        draw.point((x, y), fill=(202, 212, 224, 255))
    noise(img, "side")
    return img


def top() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (38, 45, 55, 255))
    draw = ImageDraw.Draw(img)
    draw.rectangle([1, 1, 30, 30], outline=(96, 112, 128, 255), width=2)
    # 中央装配圆盘
    draw.ellipse([7, 7, 24, 24], fill=(24, 30, 38, 255), outline=(45, 204, 229, 255), width=2)
    draw.ellipse([12, 12, 19, 19], fill=(61, 72, 86, 255), outline=(232, 160, 53, 255), width=1)
    # 面板网格
    for x in (5, 26):
        draw.line([(x, 4), (x, 27)], fill=(55, 66, 79, 255))
    for y in (5, 26):
        draw.line([(4, y), (27, y)], fill=(55, 66, 79, 255))
    draw.rectangle([3, 3, 7, 4], fill=(232, 160, 53, 255))
    draw.rectangle([24, 3, 28, 4], fill=(232, 160, 53, 255))
    noise(img, "top")
    return img


def metal() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for y in range(32):
        v = int(118 + 54 * (1.0 - y / 31))
        draw.line([(0, y), (31, y)], fill=(v, v + 5, v + 11, 255))
    for y in (8, 16, 24):
        draw.line([(0, y), (31, y)], fill=(75, 83, 94, 255))
        draw.line([(0, y + 1), (31, y + 1)], fill=(207, 216, 228, 90))
    draw.rectangle([2, 2, 4, 4], fill=(235, 242, 248, 180))
    noise(img, "metal")
    return img


def accent() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (218, 143, 42, 255))
    draw = ImageDraw.Draw(img)
    for x in range(-32, 32, 12):
        draw.polygon([(x, 0), (x + 6, 0), (x + 38, 31), (x + 32, 31)],
                     fill=(255, 194, 63, 210))
    draw.rectangle([0, 0, 31, 31], outline=(112, 71, 26, 255), width=2)
    noise(img, "accent")
    return img


def rocket() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # 小尺寸火箭剖面，用于组装台中央的半成品
    draw.polygon([(16, 2), (11, 9), (21, 9)], fill=(218, 224, 232, 255))
    draw.rectangle([11, 9, 21, 24], fill=(242, 245, 248, 255))
    draw.rectangle([11, 12, 21, 14], fill=(218, 143, 42, 255))
    draw.rectangle([14, 16, 18, 20], fill=(27, 94, 145, 255))
    draw.line([(14, 16), (18, 16)], fill=(143, 232, 255, 255))
    draw.polygon([(11, 20), (5, 27), (11, 27)], fill=(52, 60, 70, 255))
    draw.polygon([(21, 20), (27, 27), (21, 27)], fill=(52, 60, 70, 255))
    draw.rectangle([14, 24, 18, 28], fill=(65, 72, 82, 255))
    noise(img, "rocket")
    return img


def main() -> None:
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    outputs = {
        "rocket_assembler_side.png": side(),
        "rocket_assembler_top.png": top(),
        "rocket_assembler_metal.png": metal(),
        "rocket_assembler_accent.png": accent(),
        "rocket_assembler_rocket.png": rocket(),
    }
    for name, image in outputs.items():
        image.save(BLOCK_DIR / name, optimize=True)
        print(f"wrote textures/block/{name}")


if __name__ == "__main__":
    main()
