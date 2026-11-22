#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成 32x32 的火箭部件、机器方块和新材料贴图。

机器方块共用同一套深色钢制底框、铆钉和散热槽，再叠加各机器专属的
燃烧室 / 氧气罐 / 活塞 / 加热线圈 / 坩埚 / 电池图形。其余仍为 16x16
的物品贴图会统一最近邻放大到 32x32，保证物品栏像素规格一致。
"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc/textures"
ITEM_DIR = ASSETS / "item"
BLOCK_DIR = ASSETS / "block"

SIZE = 32


def new_item() -> Image.Image:
    return Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))


def noise(img: Image.Image, seed: str, amount: int = 4) -> None:
    rng = random.Random("newgc-hd::" + seed)
    px = img.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            d = rng.randint(-amount, amount)
            px[x, y] = (max(0, min(255, r + d)),
                        max(0, min(255, g + d)),
                        max(0, min(255, b + d)), a)


def metal_ingot(name: str, base, light, dark) -> Image.Image:
    img = new_item()
    d = ImageDraw.Draw(img)
    top = [(7, 10), (25, 10), (22, 5), (10, 5)]
    front = [(7, 10), (25, 10), (27, 23), (5, 23)]
    bottom = [(5, 23), (27, 23), (24, 28), (8, 28)]
    d.polygon(top, fill=light + (255,))
    d.polygon(front, fill=base + (255,))
    d.polygon(bottom, fill=dark + (255,))
    d.line(top + [top[0]], fill=dark + (255,))
    d.line([(7, 10), (5, 23), (8, 28), (24, 28), (27, 23), (25, 10)],
           fill=dark + (255,))
    d.line([(9, 12), (23, 12)], fill=(245, 250, 255, 190))
    d.line([(9, 14), (22, 14)], fill=light + (255,))
    d.rectangle([10, 18, 22, 20], fill=dark + (255,))
    d.point((10, 7), fill=(255, 255, 255, 230))
    noise(img, name)
    return img


def metal_plate(name: str, base, light, dark, accent) -> Image.Image:
    img = new_item()
    d = ImageDraw.Draw(img)
    d.rectangle([3, 6, 28, 25], fill=base + (255,))
    d.rectangle([3, 6, 28, 9], fill=light + (255,))
    d.rectangle([3, 22, 28, 25], fill=dark + (255,))
    d.rectangle([4, 7, 27, 24], outline=dark + (255,))
    d.rectangle([5, 11, 26, 12], fill=accent + (255,))
    for x, y in ((6, 8), (25, 8), (6, 22), (25, 22)):
        d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=light + (255,))
        d.point((x, y), fill=(255, 255, 255, 220))
        d.point((x + 1, y + 1), fill=dark + (255,))
    d.rectangle([10, 16, 21, 19], fill=dark + (255,))
    d.line([(10, 17), (21, 17)], fill=accent + (255,))
    d.line([(7, 6), (12, 6)], fill=(255, 255, 255, 150))
    noise(img, name)
    return img


def rocket(name: str, tier: int) -> Image.Image:
    img = new_item()
    d = ImageDraw.Draw(img)
    body = (225, 232, 240, 255) if tier < 3 else (246, 246, 246, 255)
    edge = (151, 164, 176, 255) if tier < 3 else (193, 184, 166, 255)
    accent = (208, 72, 50, 255)
    if tier == 2:
        accent = (36, 201, 226, 255)
    elif tier == 3:
        accent = (241, 187, 65, 255)
    dark = (39, 45, 54, 255)
    glass = (125, 230, 248, 255)
    flame = (255, 168, 46, 255)

    if tier >= 2:
        for x in (4, 22):
            d.rectangle([x, 17, x + 5, 27], fill=(111, 126, 140, 255), outline=dark)
            d.rectangle([x + 1, 18, x + 4, 19], fill=accent)
            d.rectangle([x + 2, 27, x + 3, 29], fill=flame)
    if tier >= 3:
        for x in (1, 25):
            d.rectangle([x, 20, x + 4, 27], fill=(94, 105, 118, 255), outline=dark)
            d.point((x + 2, 28), fill=flame)

    d.polygon([(16, 1), (11, 9), (21, 9)], fill=accent)
    d.polygon([(16, 1), (14, 9), (16, 9)], fill=(255, 225, 171, 255) if tier == 3 else accent)
    d.rectangle([11, 9, 20, 26], fill=body)
    d.rectangle([11, 9, 11, 26], fill=edge)
    d.rectangle([20, 9, 20, 26], fill=edge)
    d.rectangle([12, 11, 19, 12], fill=(250, 252, 255, 220))
    d.rectangle([14, 13, 17, 16], fill=dark)
    d.rectangle([15, 14, 17, 15], fill=glass)
    d.point((15, 14), fill=(255, 255, 255, 255))
    d.rectangle([12, 19, 19, 20], fill=accent)
    d.rectangle([12, 22, 19, 23], fill=(73, 81, 93, 255))
    d.polygon([(11, 23), (5, 29), (11, 29)], fill=accent)
    d.polygon([(20, 23), (26, 29), (20, 29)], fill=accent)
    d.rectangle([13, 26, 18, 27], fill=dark)
    d.rectangle([14, 28, 17, 29], fill=(102, 109, 119, 255))
    d.rectangle([15, 30, 16, 31], fill=flame)
    noise(img, name)
    return img


def nose_cone(name: str, tier: int) -> Image.Image:
    img = new_item()
    d = ImageDraw.Draw(img)
    if tier == 1:
        shell, light, ring = (103, 112, 124), (166, 176, 188), (208, 152, 53)
    elif tier == 2:
        shell, light, ring = (91, 121, 145), (190, 218, 235), (31, 194, 221)
    else:
        shell, light, ring = (175, 167, 151), (247, 248, 250), (239, 181, 56)
    d.polygon([(16, 1), (7, 15), (25, 15)], fill=shell)
    d.polygon([(16, 1), (13, 15), (16, 15)], fill=light)
    d.rectangle([7, 15, 24, 27], fill=shell)
    d.rectangle([8, 16, 23, 18], fill=light)
    d.rectangle([9, 21, 22, 23], fill=ring)
    d.rectangle([11, 27, 20, 30], fill=(rings := ring if tier != 1 else (184, 124, 44)))
    d.line([(7, 15), (7, 27)], fill=(44, 50, 60, 255))
    d.line([(24, 15), (24, 27)], fill=(44, 50, 60, 255))
    d.point((14, 15), fill=(255, 255, 255, 240))
    noise(img, name)
    return img


def engine(name: str, tier: int) -> Image.Image:
    img = new_item()
    d = ImageDraw.Draw(img)
    shell = (127, 139, 152) if tier == 1 else ((151, 166, 181) if tier == 2 else (213, 215, 220))
    glow = (239, 108, 38) if tier == 1 else ((44, 205, 232) if tier == 2 else (244, 185, 60))
    d.rectangle([10, 2, 21, 15], fill=shell + (255,), outline=(29, 35, 43, 255))
    d.rectangle([12, 4, 19, 6], fill=(238, 246, 252, 255))
    d.rectangle([13, 8, 18, 13], fill=(42, 49, 59, 255))
    d.rectangle([14, 9, 17, 12], fill=glow + (255,))
    d.polygon([(9, 15), (22, 15), (27, 24), (4, 24)],
              fill=(47, 54, 65, 255), outline=(20, 24, 30, 255))
    d.rectangle([6, 20, 25, 22], fill=glow + (255,))
    if tier >= 3:
        for x0, x1 in ((6, 10), (13, 18), (21, 25)):
            d.rectangle([x0, 24, x1, 27], fill=(76, 83, 95, 255))
            d.rectangle([x0 + 1, 28, x1 - 1, 29], fill=glow + (255,))
    else:
        d.rectangle([10, 25, 21, 28], fill=(31, 36, 44, 255))
        d.rectangle([12, 29, 19, 30], fill=glow + (255,))
    noise(img, name)
    return img


def booster_texture(name: str) -> Image.Image:
    img = new_item()
    d = ImageDraw.Draw(img)
    d.rectangle([12, 2, 20, 24], fill=(119, 131, 145, 255), outline=(37, 43, 52, 255))
    d.rectangle([13, 4, 19, 5], fill=(216, 225, 234, 255))
    d.rectangle([13, 10, 19, 11], fill=(52, 59, 70, 255))
    d.rectangle([13, 18, 19, 19], fill=(52, 59, 70, 255))
    d.polygon([(12, 22), (5, 30), (12, 30)], fill=(47, 54, 65, 255))
    d.polygon([(20, 22), (27, 30), (20, 30)], fill=(47, 54, 65, 255))
    d.rectangle([13, 25, 19, 28], fill=(33, 39, 48, 255))
    d.rectangle([14, 29, 18, 30], fill=(245, 142, 47, 255))
    d.point((15, 31), fill=(255, 223, 111, 255))
    noise(img, name)
    return img


def machine_base(name: str, accent, glow) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    """所有机器共用的深色钢制底框：渐变面板、内框、铆钉、散热槽。"""
    img = Image.new("RGBA", (SIZE, SIZE), (24, 30, 38, 255))
    d = ImageDraw.Draw(img)
    for y in range(SIZE):
        v = int(37 + 23 * (1.0 - y / (SIZE - 1)))
        d.line([(0, y), (SIZE - 1, y)], fill=(v, v + 5, v + 12, 255))
    d.rectangle([0, 0, SIZE - 1, SIZE - 1], outline=(17, 21, 27, 255))
    d.rectangle([2, 2, SIZE - 3, SIZE - 3], outline=(112, 126, 140, 255))
    d.rectangle([4, 4, SIZE - 5, SIZE - 5], fill=(27, 33, 42, 255))
    d.rectangle([5, 5, SIZE - 6, SIZE - 6], fill=(38, 46, 57, 255))
    d.rectangle([5, 5, SIZE - 6, 8], fill=(55, 65, 78, 255))
    d.line([(5, 28), (26, 28)], fill=(22, 27, 34, 255))
    for x, y in ((3, 3), (28, 3), (3, 28), (28, 28)):
        d.rectangle([x, y, x + 1, y + 1], fill=(174, 190, 204, 255))
        d.point((x + 1, y + 1), fill=(19, 23, 29, 255))
    for x in (10, 16, 22):
        d.line([(x, 6), (x, 8)], fill=(26, 32, 40, 255))
    d.rectangle([5, 4, 26, 5], fill=accent)
    d.rectangle([6, 9, 25, 10], fill=glow + (80,))
    return img, d


def machine_texture(name: str) -> Image.Image:
    accents = {
        "mechanical_casing": ((135, 153, 168), (207, 222, 232)),
        "solid_fuel_generator": ((213, 119, 43), (255, 205, 98)),
        "oxygen_extractor": ((38, 193, 205), (176, 249, 255)),
        "compressor": ((223, 173, 57), (255, 231, 133)),
        "electric_furnace": ((225, 83, 52), (255, 177, 93)),
        "alloy_smelter": ((27, 199, 180), (151, 250, 236)),
        "energy_cell": ((38, 151, 238), (152, 227, 255)),
    }
    accent, glow = accents[name]
    img, d = machine_base(name, accent + (255,), glow)

    d.rectangle([8, 12, 23, 25], fill=(19, 24, 31, 255), outline=accent + (255,))
    if name == "mechanical_casing":
        d.ellipse([12, 15, 20, 23], fill=(63, 73, 85, 255), outline=accent + (255,))
        d.ellipse([15, 18, 17, 20], fill=glow + (255,))
        for x0, y0, x1, y1 in ((15, 13, 17, 15), (15, 23, 17, 25),
                               (9, 18, 11, 20), (21, 18, 23, 20)):
            d.rectangle([x0, y0, x1, y1], fill=accent + (255,))
    elif name == "solid_fuel_generator":
        d.rectangle([10, 17, 21, 24], fill=(52, 29, 21, 255))
        for x in range(12, 21, 3):
            d.rectangle([x, 14, x + 1, 24], fill=accent + (255,))
        d.rectangle([12, 23, 20, 26], fill=(255, 142, 42, 255))
    elif name == "oxygen_extractor":
        for x in (10, 18):
            d.rectangle([x, 13, x + 4, 24], fill=(30, 92, 106, 255), outline=glow + (255,))
            d.rectangle([x + 1, 15, x + 3, 17], fill=glow + (255,))
        d.rectangle([9, 11, 22, 12], fill=accent + (255,))
        d.ellipse([13, 18, 19, 24], fill=None, outline=glow + (255,))
    elif name == "compressor":
        d.rectangle([10, 12, 21, 16], fill=accent + (255,))
        d.rectangle([14, 16, 17, 23], fill=(93, 103, 115, 255))
        d.rectangle([9, 24, 22, 26], fill=(24, 29, 36, 255), outline=glow + (255,))
        d.line([(10, 14), (21, 14)], fill=glow + (255,))
    elif name == "electric_furnace":
        for y in (15, 18, 21):
            d.line([(10, y), (21, y)], fill=accent + (255,))
            d.point((21, y), fill=glow + (255,))
        d.rectangle([11, 23, 20, 25], fill=(255, 132, 45, 255))
    elif name == "alloy_smelter":
        d.ellipse([10, 13, 16, 19], fill=accent + (255,))
        d.ellipse([16, 13, 22, 19], fill=glow + (255,))
        d.polygon([(12, 19), (20, 19), (18, 25), (14, 25)], fill=(70, 79, 91, 255))
        d.line([(16, 20), (16, 24)], fill=glow + (255,))
    elif name == "energy_cell":
        d.rectangle([12, 12, 19, 24], fill=(18, 44, 67, 255), outline=glow + (255,))
        d.rectangle([14, 14, 17, 17], fill=(92, 212, 255, 255))
        d.polygon([(17, 17), (13, 22), (16, 22), (14, 25), (20, 19), (17, 19)],
                  fill=glow + (255,))
        d.rectangle([14, 10, 17, 12], fill=accent + (255,))

    d.rectangle([7, 27, 24, 28], fill=accent + (255,))
    noise(img, "machine_" + name, 3)
    return img


def wire_texture() -> Image.Image:
    """黑色电缆方块贴图：两端为白色网格接头。"""
    img = Image.new("RGBA", (SIZE, SIZE), (12, 12, 14, 255))
    d = ImageDraw.Draw(img)

    # 黑色金属电缆主体，保留非常克制的亮度变化，避免整块纯黑。
    for y in range(SIZE):
        shade = 12 + (y % 4) * 2
        d.line([(0, y), (SIZE - 1, y)], fill=(shade, shade, shade + 2, 255))
    d.line([(6, 15), (25, 15)], fill=(54, 54, 60, 255))
    d.line([(6, 16), (25, 16)], fill=(30, 30, 35, 255))

    def grid_end(x0: int, x1: int) -> None:
        d.rectangle([x0, 0, x1, SIZE - 1], fill=(20, 20, 24, 255))
        for x in range(x0, x1 + 1, 4):
            d.line([(x, 0), (x, SIZE - 1)], fill=(224, 229, 235, 255))
        for y in range(0, SIZE, 4):
            d.line([(x0, y), (x1, y)], fill=(224, 229, 235, 255))
        d.rectangle([x0, 0, x1, SIZE - 1], outline=(248, 250, 252, 255))

    grid_end(0, 6)
    grid_end(25, 31)
    return img


def wire_item_texture() -> Image.Image:
    """物品栏只显示一条横向电缆，两端是白色网格接头。"""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 14, 27, 17], fill=(18, 18, 22, 255))
    d.line([(4, 15), (27, 15)], fill=(72, 72, 80, 255))
    for x0, x1 in ((0, 5), (26, 31)):
        d.rectangle([x0, 10, x1, 21], fill=(20, 20, 24, 255),
                    outline=(246, 248, 250, 255))
        for x in range(x0 + 1, x1, 3):
            d.line([(x, 11), (x, 20)], fill=(232, 236, 242, 255))
        d.line([(x0 + 1, 14), (x1 - 1, 14)], fill=(232, 236, 242, 255))
        d.line([(x0 + 1, 17), (x1 - 1, 17)], fill=(232, 236, 242, 255))
    d.point((15, 16), fill=(185, 190, 198, 255))
    return img


def wire_core_texture() -> Image.Image:
    """中心接线块：纯黑金属，不与两端的白色网格混在一起。"""
    img = Image.new("RGBA", (SIZE, SIZE), (10, 10, 12, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, SIZE - 1, SIZE - 1], outline=(44, 44, 50, 255))
    d.rectangle([8, 8, 23, 23], fill=(17, 17, 20, 255),
                outline=(62, 62, 70, 255))
    d.line([(12, 14), (19, 14)], fill=(94, 94, 104, 255))
    d.point((15, 17), fill=(170, 174, 182, 255))
    return img


def main() -> None:
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)

    items = {
        "rocket": rocket("rocket", 1),
        "tier2_rocket": rocket("tier2_rocket", 2),
        "tier3_rocket": rocket("tier3_rocket", 3),
        "tier1_nose_cone": nose_cone("tier1_nose_cone", 1),
        "tier2_nose_cone": nose_cone("tier2_nose_cone", 2),
        "tier3_nose_cone": nose_cone("tier3_nose_cone", 3),
        "rocket_engine": engine("rocket_engine", 1),
        "advanced_rocket_engine": engine("advanced_rocket_engine", 2),
        "tier3_rocket_engine": engine("tier3_rocket_engine", 3),
        "booster": booster_texture("booster"),
        "heavy_alloy": metal_ingot("heavy_alloy",
                                   (124, 132, 144), (193, 202, 214), (67, 74, 86)),
        "reinforced_heavy_alloy": metal_ingot("reinforced_heavy_alloy",
                                              (112, 142, 165), (190, 225, 240), (52, 80, 100)),
        "elite_heavy_alloy": metal_ingot("elite_heavy_alloy",
                                         (170, 163, 149), (245, 240, 222), (85, 76, 60)),
        "heavy_alloy_plate": metal_plate("heavy_alloy_plate",
                                         (124, 132, 144), (193, 202, 214),
                                         (67, 74, 86), (190, 199, 210)),
        "reinforced_heavy_alloy_plate": metal_plate(
            "reinforced_heavy_alloy_plate", (112, 142, 165),
            (190, 225, 240), (52, 80, 100), (52, 204, 226)),
        "elite_heavy_alloy_plate": metal_plate(
            "elite_heavy_alloy_plate", (170, 163, 149),
            (245, 240, 222), (85, 76, 60), (244, 190, 67)),
    }
    for name, image in items.items():
        image.save(ITEM_DIR / f"{name}.png", optimize=True)
        print(f"wrote HD item {name}.png")

    for name in ("mechanical_casing", "solid_fuel_generator", "oxygen_extractor",
                 "compressor", "electric_furnace", "alloy_smelter", "energy_cell"):
        machine_texture(name).save(BLOCK_DIR / f"{name}.png", optimize=True)
        print(f"wrote HD machine block {name}.png")
    wire_texture().save(BLOCK_DIR / "wire.png", optimize=True)
    wire_core_texture().save(BLOCK_DIR / "wire_core.png", optimize=True)
    wire_item_texture().save(ITEM_DIR / "wire.png", optimize=True)
    print("wrote HD wire block, core and item textures")

    for name in ("launch_pad_top", "launch_pad_side"):
        path = BLOCK_DIR / f"{name}.png"
        if path.exists():
            with Image.open(path) as image:
                if image.size != (SIZE, SIZE):
                    image.resize((SIZE, SIZE), Image.Resampling.NEAREST).save(path, optimize=True)

    # 其余物品图标（电路、氧气罐、工具等）统一升到 32x32。
    for path in sorted(ITEM_DIR.glob("*.png")):
        with Image.open(path) as image:
            if image.size == (16, 16):
                image.resize((SIZE, SIZE), Image.Resampling.NEAREST).save(path, optimize=True)


if __name__ == "__main__":
    main()
