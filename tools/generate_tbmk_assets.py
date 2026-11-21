#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 TBMK 工作台的黑灰 3 格高方块模型与 32x32 方块贴图。"""

from __future__ import annotations

import json
import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
BLOCK_DIR = ROOT / "src/main/resources/assets/newgc/textures/block"
MODEL_DIR = ROOT / "src/main/resources/assets/newgc/models/block"


def noise(img: Image.Image, seed: str, amount: int = 4) -> None:
    rng = random.Random("newgc-tbmk-block::" + seed)
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
    img = Image.new("RGBA", (32, 32), (25, 29, 35, 255))
    draw = ImageDraw.Draw(img)
    for y in range(32):
        v = int(23 + 15 * y / 31)
        draw.line([(0, y), (31, y)], fill=(v, v + 4, v + 9, 255))
    draw.rectangle([1, 1, 30, 30], outline=(104, 112, 122, 255), width=2)
    draw.line([(10, 3), (10, 29)], fill=(45, 51, 60, 255), width=2)
    draw.line([(21, 3), (21, 29)], fill=(45, 51, 60, 255), width=2)
    draw.rectangle([3, 5, 7, 7], fill=(142, 160, 170, 255))
    draw.rectangle([24, 5, 27, 7], fill=(142, 160, 170, 255))
    for x in range(3, 30, 8):
        draw.polygon([(x, 24), (x + 5, 24), (x + 1, 29), (x - 4, 29)],
                     fill=(80, 88, 99, 255))
    for x, y in ((3, 3), (28, 3), (3, 28), (28, 28)):
        draw.point((x, y), fill=(200, 208, 216, 255))
    noise(img, "side")
    return img


def top() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (32, 36, 43, 255))
    draw = ImageDraw.Draw(img)
    draw.rectangle([1, 1, 30, 30], outline=(112, 122, 134, 255), width=2)
    draw.ellipse([7, 7, 24, 24], fill=(19, 23, 29, 255), outline=(132, 150, 162, 255), width=2)
    draw.ellipse([12, 12, 19, 19], fill=(64, 72, 84, 255), outline=(157, 168, 179, 255), width=1)
    for x in (5, 26):
        draw.line([(x, 4), (x, 27)], fill=(50, 57, 67, 255))
    for y in (5, 26):
        draw.line([(4, y), (27, y)], fill=(50, 57, 67, 255))
    draw.rectangle([3, 3, 7, 4], fill=(142, 160, 170, 255))
    draw.rectangle([24, 3, 28, 4], fill=(142, 160, 170, 255))
    noise(img, "top")
    return img


def metal() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for y in range(32):
        v = int(92 + 76 * (1.0 - y / 31))
        draw.line([(0, y), (31, y)], fill=(v, v + 5, v + 11, 255))
    for y in (8, 16, 24):
        draw.line([(0, y), (31, y)], fill=(62, 69, 79, 255))
        draw.line([(0, y + 1), (31, y + 1)], fill=(205, 213, 222, 95))
    draw.rectangle([2, 2, 4, 4], fill=(224, 231, 238, 180))
    noise(img, "metal")
    return img


def accent() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (81, 90, 102, 255))
    draw = ImageDraw.Draw(img)
    for x in range(-32, 32, 12):
        draw.polygon([(x, 0), (x + 5, 0), (x + 37, 31), (x + 32, 31)],
                     fill=(135, 150, 163, 150))
    draw.rectangle([0, 0, 31, 31], outline=(42, 48, 57, 255), width=2)
    noise(img, "accent")
    return img


def rocket_texture() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon([(16, 2), (11, 9), (21, 9)], fill=(126, 137, 150, 255))
    draw.rectangle([11, 9, 21, 25], fill=(179, 190, 200, 255))
    draw.rectangle([11, 13, 21, 15], fill=(70, 78, 90, 255))
    draw.rectangle([14, 17, 18, 21], fill=(52, 77, 98, 255))
    draw.polygon([(11, 21), (5, 28), (11, 28)], fill=(45, 51, 61, 255))
    draw.polygon([(21, 21), (27, 28), (21, 28)], fill=(45, 51, 61, 255))
    draw.rectangle([14, 25, 18, 29], fill=(58, 65, 76, 255))
    noise(img, "rocket")
    return img


def machine_texture() -> Image.Image:
    img = Image.new("RGBA", (32, 32), (36, 41, 49, 255))
    draw = ImageDraw.Draw(img)
    for y in range(32):
        v = int(37 + 24 * (1.0 - y / 31))
        draw.line([(0, y), (31, y)], fill=(v, v + 5, v + 11, 255))
    for x in (4, 16, 27):
        draw.line([(x, 0), (x, 31)], fill=(91, 101, 113, 255))
    for y in (4, 16, 27):
        draw.line([(0, y), (31, y)], fill=(72, 81, 93, 255))
    draw.rectangle([7, 7, 12, 12], fill=(143, 157, 171, 255))
    draw.rectangle([19, 19, 24, 24], fill=(23, 27, 33, 255))
    noise(img, "machine")
    return img


def icon() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([2, 12, 14, 15], fill=(67, 75, 86, 255))
    draw.rectangle([4, 4, 5, 13], fill=(145, 157, 170, 255))
    draw.rectangle([11, 4, 12, 13], fill=(145, 157, 170, 255))
    draw.rectangle([4, 5, 12, 6], fill=(116, 128, 141, 255))
    draw.rectangle([7, 5, 9, 12], fill=(185, 195, 205, 255))
    draw.polygon([(8, 1), (6, 5), (10, 5)], fill=(104, 116, 129, 255))
    draw.rectangle([7, 10, 9, 12], fill=(55, 62, 73, 255))
    return img


def element(frm, to, texture, rotation=None):
    data = {
        "from": frm,
        "to": to,
        "faces": {
            "down": {"texture": texture},
            "up": {"texture": texture},
            "north": {"texture": texture},
            "south": {"texture": texture},
            "west": {"texture": texture},
            "east": {"texture": texture},
        },
    }
    if rotation is not None:
        data["rotation"] = rotation
    return data


def model() -> dict:
    elements = [
        element([0, 0, 0], [16, 5, 16], "#side"),
        element([2, 5, 2], [14, 8, 14], "#metal"),
        element([2, 8, 2], [14, 12, 4], "#side"),
        element([2, 8, 3.5], [4, 44, 5.5], "#metal"),
        element([12, 8, 3.5], [14, 44, 5.5], "#metal"),
        element([2, 42, 3], [14, 48, 5.5], "#metal"),
        element([4, 18, 3.5], [12, 20, 4.5], "#accent"),
        element([5, 8, 5], [11, 10, 11], "#metal"),
        element([7, 10, 7], [9, 30, 9], "#rocket"),
        element([7.25, 30, 7.25], [8.75, 35, 8.75], "#rocket"),
        element([6.5, 7, 6.5], [9.5, 10, 9.5], "#accent"),
        element([6.5, 10, 6.5], [7, 13, 9.5], "#metal"),
        element([9, 10, 6.5], [9.5, 13, 9.5], "#metal"),
        element([3, 14, 4.5], [9, 16, 6],
                "#metal", {"origin": [3, 15, 5], "axis": "y", "angle": 22.5}),
        element([3, 28, 4.5], [10, 30, 6],
                "#metal", {"origin": [3, 29, 5], "axis": "y", "angle": 22.5}),
        element([3, 38, 4.5], [9, 40, 6],
                "#metal", {"origin": [3, 39, 5], "axis": "y", "angle": 22.5}),
        element([7, 14, 10], [13, 16, 11.5],
                "#metal", {"origin": [13, 15, 11], "axis": "y", "angle": -22.5}),
        element([6, 28, 10], [13, 30, 11.5],
                "#metal", {"origin": [13, 29, 11], "axis": "y", "angle": -22.5}),
        element([7, 38, 10], [13, 40, 11.5],
                "#metal", {"origin": [13, 39, 11], "axis": "y", "angle": -22.5}),
        element([6, 17, 6], [7.5, 20, 9], "#accent"),
        element([8.5, 17, 6], [10, 20, 9], "#accent"),
        element([1, 5, 1], [2, 7, 2], "#accent"),
        element([14, 5, 1], [15, 7, 2], "#accent"),
    ]
    return {
        "textures": {
            "particle": "newgc:block/rocket_assembler_side",
            "side": "newgc:block/rocket_assembler_side",
            "top": "newgc:block/rocket_assembler_top",
            "metal": "newgc:block/rocket_assembler_metal",
            "accent": "newgc:block/rocket_assembler_accent",
            "rocket": "newgc:block/rocket_assembler_rocket",
        },
        "elements": elements,
    }


def base_model() -> dict:
    # 只是物品/破坏粒子使用的合法 1 格模型；真正的 3 格高结构由 BER 绘制。
    return {
        "textures": {
            "particle": "newgc:block/rocket_assembler_side",
            "side": "newgc:block/rocket_assembler_side",
            "top": "newgc:block/rocket_assembler_top",
            "metal": "newgc:block/rocket_assembler_metal",
        },
        "elements": [
            element([0, 0, 0], [16, 5, 16], "#side"),
            element([2, 5, 2], [14, 8, 14], "#metal"),
        ],
    }


def main() -> None:
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    images = {
        "rocket_assembler_side.png": side(),
        "rocket_assembler_top.png": top(),
        "rocket_assembler_metal.png": metal(),
        "rocket_assembler_accent.png": accent(),
        "rocket_assembler_rocket.png": rocket_texture(),
        "rocket_assembler_machine.png": machine_texture(),
        "rocket_assembler_icon.png": icon(),
    }
    for name, image in images.items():
        image.save(BLOCK_DIR / name, optimize=True)
        print(f"wrote textures/block/{name}")
    (MODEL_DIR / "rocket_assembler.json").write_text(
        json.dumps(base_model(), indent=2) + "\n", encoding="utf-8")
    print("wrote models/block/rocket_assembler.json (valid item base model)")


if __name__ == "__main__":
    main()
