#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 TBMK 工作台新增材料与火箭部件的 16x16 物品贴图。"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ITEM_DIR = ROOT / "src/main/resources/assets/newgc/textures/item"


def noise(img: Image.Image, seed: str, amount: int = 3) -> None:
    rng = random.Random("newgc-tbmk-item::" + seed)
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


def heavy_alloy() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon([(3, 4), (12, 4), (14, 7), (12, 12), (3, 12), (1, 8)],
                 fill=(122, 130, 141, 255), outline=(62, 69, 80, 255))
    draw.polygon([(4, 5), (11, 5), (12, 7), (4, 7)],
                 fill=(179, 187, 198, 255))
    draw.rectangle([4, 9, 12, 10], fill=(78, 85, 96, 255))
    draw.point((5, 6), fill=(235, 241, 248, 255))
    draw.point((10, 11), fill=(55, 61, 71, 255))
    noise(img, "heavy_alloy")
    return img


def heavy_alloy_plate() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([2, 3, 13, 13], fill=(91, 99, 111, 255), outline=(38, 43, 51, 255))
    draw.rectangle([3, 4, 12, 5], fill=(174, 183, 195, 255))
    draw.rectangle([3, 10, 12, 12], fill=(57, 64, 75, 255))
    draw.rectangle([5, 7, 10, 8], fill=(24, 28, 34, 255))
    for x, y in ((4, 12), (11, 4), (11, 12), (4, 4)):
        draw.point((x, y), fill=(214, 222, 232, 255))
    noise(img, "heavy_alloy_plate")
    return img


def nose_cone() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon([(8, 1), (4, 7), (12, 7)], fill=(76, 83, 94, 255))
    draw.polygon([(8, 1), (5, 7), (8, 7)], fill=(150, 159, 172, 255))
    draw.rectangle([4, 7, 12, 13], fill=(123, 132, 144, 255), outline=(48, 54, 64, 255))
    draw.rectangle([5, 10, 11, 11], fill=(45, 50, 60, 255))
    draw.rectangle([6, 13, 10, 14], fill=(206, 153, 54, 255))
    noise(img, "nose_cone")
    return img


def rocket_engine() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([5, 1, 11, 9], fill=(107, 115, 126, 255), outline=(42, 47, 56, 255))
    draw.polygon([(4, 9), (12, 9), (14, 13), (2, 13)],
                 fill=(44, 49, 58, 255), outline=(22, 25, 31, 255))
    draw.rectangle([6, 4, 10, 6], fill=(188, 196, 206, 255))
    draw.rectangle([7, 10, 9, 12], fill=(234, 109, 38, 255))
    draw.point((7, 13), fill=(255, 214, 74, 255))
    noise(img, "rocket_engine")
    return img


def booster() -> Image.Image:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([6, 1, 10, 12], fill=(117, 126, 138, 255), outline=(41, 47, 56, 255))
    draw.rectangle([6, 3, 10, 4], fill=(198, 207, 218, 255))
    draw.rectangle([6, 8, 10, 9], fill=(54, 60, 70, 255))
    draw.polygon([(6, 10), (3, 14), (6, 14)], fill=(47, 54, 64, 255))
    draw.polygon([(10, 10), (13, 14), (10, 14)], fill=(47, 54, 64, 255))
    draw.rectangle([7, 13, 9, 15], fill=(229, 98, 35, 255))
    noise(img, "booster")
    return img


def tier2_nose_cone() -> Image.Image:
    """二阶鼻锥：更长的白蓝锥体、青蓝色识别环。"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon([(8, 0), (3, 8), (13, 8)], fill=(57, 72, 88, 255))
    draw.polygon([(8, 0), (5, 8), (8, 8)], fill=(172, 199, 218, 255))
    draw.polygon([(8, 0), (7, 4), (8, 4)], fill=(235, 248, 255, 255))
    draw.rectangle([3, 8, 13, 13], fill=(126, 151, 170, 255), outline=(37, 52, 66, 255))
    draw.rectangle([4, 10, 12, 11], fill=(26, 177, 203, 255))
    draw.rectangle([6, 13, 10, 14], fill=(33, 205, 226, 255))
    noise(img, "tier2_nose_cone")
    return img


def advanced_rocket_engine() -> Image.Image:
    """二阶引擎：双环喷口和青色能量核心。"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([4, 1, 12, 7], fill=(125, 139, 154, 255), outline=(33, 45, 58, 255))
    draw.rectangle([5, 3, 11, 4], fill=(210, 220, 232, 255))
    draw.rectangle([6, 5, 10, 7], fill=(28, 190, 220, 255))
    draw.polygon([(3, 8), (13, 8), (15, 13), (1, 13)],
                 fill=(47, 57, 70, 255), outline=(19, 27, 36, 255))
    draw.rectangle([4, 10, 12, 11], fill=(74, 217, 237, 255))
    draw.rectangle([6, 12, 10, 13], fill=(255, 187, 61, 255))
    draw.point((4, 9), fill=(232, 250, 255, 255))
    draw.point((11, 9), fill=(232, 250, 255, 255))
    noise(img, "advanced_rocket_engine")
    return img


def tier3_nose_cone() -> Image.Image:
    """三阶鼻锥：白金锥体、深色隔热尖端与金色识别环。"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.polygon([(8, 0), (3, 8), (13, 8)], fill=(46, 52, 62, 255))
    draw.polygon([(8, 0), (5, 8), (8, 8)], fill=(236, 240, 245, 255))
    draw.polygon([(8, 0), (7, 3), (8, 3)], fill=(255, 255, 255, 255))
    draw.rectangle([3, 8, 13, 13], fill=(171, 177, 188, 255), outline=(30, 34, 42, 255))
    draw.rectangle([4, 9, 12, 10], fill=(239, 184, 64, 255))
    draw.rectangle([5, 12, 11, 13], fill=(214, 155, 43, 255))
    draw.point((4, 8), fill=(255, 241, 186, 255))
    draw.point((11, 8), fill=(255, 241, 186, 255))
    noise(img, "tier3_nose_cone")
    return img


def tier3_rocket_engine() -> Image.Image:
    """三阶引擎：白金外壳、金色双环喷口与三联喷管。"""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rectangle([3, 0, 12, 7], fill=(205, 211, 220, 255), outline=(29, 34, 42, 255))
    draw.rectangle([4, 2, 11, 3], fill=(245, 248, 251, 255))
    draw.rectangle([5, 4, 10, 6], fill=(240, 178, 48, 255))
    draw.polygon([(2, 7), (14, 7), (15, 13), (1, 13)],
                 fill=(48, 54, 64, 255), outline=(18, 21, 27, 255))
    draw.rectangle([3, 8, 13, 9], fill=(241, 189, 72, 255))
    for x0, x1 in ((3, 6), (7, 10), (11, 13)):
        draw.rectangle([x0, 10, x1, 12], fill=(75, 81, 92, 255))
        draw.rectangle([x0 + 1, 13, x1 - 1, 14], fill=(255, 193, 74, 255))
    draw.point((4, 8), fill=(255, 250, 220, 255))
    draw.point((11, 8), fill=(255, 250, 220, 255))
    noise(img, "tier3_rocket_engine")
    return img


def main() -> None:
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    outputs = {
        "heavy_alloy.png": heavy_alloy(),
        "heavy_alloy_plate.png": heavy_alloy_plate(),
        "tier1_nose_cone.png": nose_cone(),
        "rocket_engine.png": rocket_engine(),
        "booster.png": booster(),
        "tier2_nose_cone.png": tier2_nose_cone(),
        "advanced_rocket_engine.png": advanced_rocket_engine(),
        "tier3_nose_cone.png": tier3_nose_cone(),
        "tier3_rocket_engine.png": tier3_rocket_engine(),
    }
    for name, image in outputs.items():
        image.save(ITEM_DIR / name, optimize=True)
        print(f"wrote textures/item/{name}")


if __name__ == "__main__":
    main()
