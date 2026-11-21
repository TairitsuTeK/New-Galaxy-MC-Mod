#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成火箭与发射平台的确定性贴图。

输出：
  textures/block/launch_pad_top.png   16x16
  textures/block/launch_pad_side.png  16x16
  textures/item/rocket.png            16x16
  textures/item/tier2_rocket.png      16x16
  textures/item/tier3_rocket.png      16x16
  textures/entity/rocket.png          256x128 实体模型 UV 图集
  textures/entity/tier2_rocket.png    256x128 二阶实体图集
  textures/entity/tier3_rocket.png    256x128 三阶实体图集

实体图集的 UV 布局必须与 RocketModel 中的 texOffs 保持一致。
"""

from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/newgc/textures"
BLOCK_DIR = ASSETS / "block"
ITEM_DIR = ASSETS / "item"
ENTITY_DIR = ASSETS / "entity"


def rng_for(name: str) -> random.Random:
    return random.Random(f"newgc-rocket::{name}")


def add_noise(img: Image.Image, rng: random.Random, amount: int = 8) -> None:
    pixels = img.load()
    width, height = img.size
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            delta = rng.randint(-amount, amount)
            pixels[x, y] = (
                max(0, min(255, r + delta)),
                max(0, min(255, g + delta)),
                max(0, min(255, b + delta)),
                a,
            )


def launch_pad_top() -> Image.Image:
    rng = rng_for("launch_pad_top")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    draw.rectangle([0, 0, 15, 15], fill=(38, 42, 48, 255))
    draw.rectangle([1, 1, 14, 14], fill=(60, 65, 74, 255))
    draw.rectangle([2, 2, 13, 13], fill=(48, 52, 60, 255))

    # 危险色警示环
    draw.rectangle([2, 2, 13, 3], fill=(214, 172, 54, 255))
    draw.rectangle([2, 12, 13, 13], fill=(214, 172, 54, 255))
    draw.rectangle([2, 2, 3, 13], fill=(214, 172, 54, 255))
    draw.rectangle([12, 2, 13, 13], fill=(214, 172, 54, 255))

    # 中央喷口
    draw.rectangle([5, 5, 10, 10], fill=(24, 26, 30, 255))
    draw.rectangle([6, 6, 9, 9], fill=(12, 13, 15, 255))
    draw.point((7, 6), fill=(96, 102, 112, 255))
    draw.point((8, 9), fill=(96, 102, 112, 255))

    # 固定螺栓
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        draw.point((x, y), fill=(126, 134, 146, 255))

    add_noise(img, rng, 6)
    return img


def launch_pad_side() -> Image.Image:
    rng = rng_for("launch_pad_side")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    draw.rectangle([0, 0, 15, 15], fill=(40, 44, 50, 255))
    draw.rectangle([1, 1, 14, 14], fill=(58, 62, 70, 255))
    draw.rectangle([1, 1, 14, 2], fill=(88, 94, 104, 255))
    draw.rectangle([1, 13, 14, 14], fill=(30, 33, 38, 255))

    # 底部危险条纹
    for x in range(2, 14, 4):
        draw.rectangle([x, 10, x + 1, 12], fill=(214, 172, 54, 255))
    draw.rectangle([2, 9, 13, 9], fill=(28, 30, 34, 255))

    # 侧面散热格栅
    for y in (4, 6, 8):
        draw.line([(3, y), (12, y)], fill=(46, 50, 58, 255))

    for x, y in ((2, 2), (13, 2), (2, 12), (13, 12)):
        draw.point((x, y), fill=(126, 134, 146, 255))

    add_noise(img, rng, 6)
    return img


def rocket_item() -> Image.Image:
    rng = rng_for("rocket_item")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    body = (206, 212, 220, 255)
    body_dark = (150, 158, 168, 255)
    accent = (200, 58, 46, 255)
    glass = (110, 220, 240, 255)
    dark = (44, 48, 56, 255)
    flame = (255, 168, 46, 255)

    # 整流罩
    draw.point((7, 1), fill=accent)
    draw.point((8, 1), fill=accent)
    draw.rectangle([6, 2, 9, 3], fill=accent)
    draw.rectangle([5, 4, 10, 4], fill=accent)

    # 机身
    draw.rectangle([5, 5, 10, 11], fill=body)
    draw.rectangle([5, 5, 5, 11], fill=body_dark)
    draw.rectangle([10, 5, 10, 11], fill=body_dark)

    # 舷窗
    draw.rectangle([7, 6, 8, 7], fill=glass)
    draw.point((7, 6), fill=(232, 252, 255, 255))

    # 尾翼
    draw.rectangle([3, 9, 4, 12], fill=accent)
    draw.rectangle([11, 9, 12, 12], fill=accent)
    draw.point((3, 12), fill=body_dark)
    draw.point((12, 12), fill=body_dark)

    # 喷口与尾焰
    draw.rectangle([6, 12, 9, 12], fill=dark)
    draw.rectangle([6, 13, 9, 13], fill=(90, 96, 106, 255))
    draw.rectangle([7, 14, 8, 14], fill=flame)
    draw.point((7, 15), fill=(255, 232, 140, 255))

    add_noise(img, rng, 4)
    return img


def tier2_rocket_item() -> Image.Image:
    """16x16 二阶火箭物品图：白色主体、青色识别环、两侧外置助推器。"""
    rng = rng_for("tier2_rocket_item")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    body = (224, 235, 244, 255)
    body_dark = (146, 172, 190, 255)
    accent = (32, 196, 224, 255)
    dark = (38, 50, 64, 255)
    glass = (182, 245, 255, 255)
    flame = (255, 172, 48, 255)
    booster = (112, 132, 150, 255)

    # 两侧外置助推器
    draw.rectangle([2, 8, 4, 13], fill=booster, outline=dark)
    draw.rectangle([11, 8, 13, 13], fill=booster, outline=dark)
    draw.point((3, 7), fill=accent)
    draw.point((12, 7), fill=accent)

    # 中央锥体与机身
    draw.point((7, 1), fill=accent)
    draw.point((8, 1), fill=accent)
    draw.rectangle([6, 2, 9, 3], fill=(244, 249, 252, 255))
    draw.rectangle([5, 4, 10, 12], fill=body)
    draw.rectangle([5, 4, 5, 12], fill=body_dark)
    draw.rectangle([10, 4, 10, 12], fill=body_dark)

    # 舷窗、识别环与喷口
    draw.rectangle([7, 5, 8, 6], fill=glass)
    draw.point((7, 5), fill=(255, 255, 255, 255))
    draw.rectangle([5, 8, 10, 8], fill=accent)
    draw.rectangle([6, 12, 9, 13], fill=dark)
    draw.rectangle([7, 14, 8, 14], fill=flame)
    draw.point((8, 15), fill=(255, 230, 130, 255))

    add_noise(img, rng, 3)
    return img


def tier3_rocket_item() -> Image.Image:
    """16x16 三阶火箭物品图：白金主体、金色识别环与四组外置助推器。"""
    rng = rng_for("tier3_rocket_item")
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    body = (242, 244, 247, 255)
    body_dark = (184, 177, 165, 255)
    accent = (239, 181, 56, 255)
    dark = (36, 40, 47, 255)
    glass = (186, 246, 255, 255)
    flame = (255, 176, 44, 255)
    booster = (132, 133, 134, 255)

    # 前后两组助推器（正面以更亮的金色喷口表示）
    for x in (1, 3, 12, 14):
        draw.rectangle([x, 7, x + 1, 13], fill=booster, outline=dark)
        draw.point((x, 6), fill=accent)
        draw.point((x + 1, 14), fill=flame)

    # 中央箭体与白金鼻锥
    draw.polygon([(7, 0), (5, 3), (10, 3)], fill=(250, 252, 255, 255))
    draw.rectangle([5, 3, 10, 12], fill=body)
    draw.rectangle([5, 3, 5, 12], fill=body_dark)
    draw.rectangle([10, 3, 10, 12], fill=body_dark)
    draw.rectangle([5, 7, 10, 8], fill=accent)
    draw.rectangle([6, 10, 9, 11], fill=dark)

    # 舷窗与中央喷口
    draw.rectangle([7, 4, 8, 5], fill=glass)
    draw.point((7, 4), fill=(255, 255, 255, 255))
    draw.rectangle([6, 12, 9, 13], fill=dark)
    draw.rectangle([7, 14, 8, 14], fill=flame)
    draw.point((8, 15), fill=(255, 235, 145, 255))

    add_noise(img, rng, 3)
    return img


TEX_SIZE = 128
TEX_W = 256
TEX_H = 128


def faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    """返回 addBox 展开后各面在贴图中的 (x, y, w, h) 区域。"""
    return {
        "down": (u + d, v, w, d),
        "up": (u + d + w, v, w, d),
        "east": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "west": (u + d + w, v + d, d, h),
        "south": (u + d + w + d, v + d, w, h),
    }


def paint_hull(draw: ImageDraw.ImageDraw, u: int, v: int, w: int, h: int, d: int,
               base: tuple[int, int, int, int], light: tuple[int, int, int, int],
               dark: tuple[int, int, int, int], band: tuple[int, int, int, int] | None = None,
               band_ratio: float = 0.45) -> None:
    """绘制箱体的四个侧面与顶底面：高光、阴影、板缝和可选的识别带。"""
    regions = faces(u, v, w, h, d)

    for name in ("north", "south", "east", "west"):
        x, y, rw, rh = regions[name]
        draw.rectangle([x, y, x + rw - 1, y + rh - 1], fill=base)
        draw.line([(x, y), (x + rw - 1, y)], fill=light)
        draw.line([(x, y + rh - 1), (x + rw - 1, y + rh - 1)], fill=dark)
        for offset in range(4, rh - 1, 5):
            draw.line([(x, y + offset), (x + rw - 1, y + offset)], fill=dark)
        if band is not None:
            by = y + int(rh * band_ratio)
            draw.rectangle([x, by, x + rw - 1, by + 1], fill=band)

    for name in ("up", "down"):
        x, y, rw, rh = regions[name]
        draw.rectangle([x, y, x + rw - 1, y + rh - 1], fill=dark if name == "down" else light)
        if rw > 2 and rh > 2:
            draw.rectangle([x + 1, y + 1, x + rw - 2, y + rh - 2], fill=base)


def solid_block(draw: ImageDraw.ImageDraw, x0: int, y0: int,
                color: tuple[int, int, int], shade: float) -> None:
    """绘制一块 64x64 的纯色材质区域，带很弱的竖向明暗过渡，避免完全死板。"""
    size = TEX_SIZE // 2
    for row in range(size):
        t = row / (size - 1)
        factor = 1.0 + shade * (1.0 - t) - shade * 0.8 * t
        px = tuple(min(255, max(0, int(c * factor))) for c in color) + (255,)
        draw.line([(x0, y0 + row), (x0 + size - 1, y0 + row)], fill=px)


def rocket_entity() -> Image.Image:
    """128x128 实体 UV 图集：黑、白、灰、黄四个纯色材质块。

    四块的左上角坐标必须与 RocketModel 中的 UV_*_U / UV_*_V 常量保持一致。
    """
    rng = rng_for("rocket_entity")
    img = Image.new("RGBA", (TEX_SIZE, TEX_SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    half = TEX_SIZE // 2

    solid_block(draw, 0, 0, (236, 238, 242), 0.05)        # 白色机体
    solid_block(draw, half, 0, (32, 34, 40), 0.10)        # 黑色整流罩 / 识别环 / 尾翼
    solid_block(draw, 0, half, (126, 130, 138), 0.14)     # 灰色尾喷口下段
    solid_block(draw, half, half, (234, 188, 60), 0.10)   # 黄色推进器上段

    add_noise(img, rng, 3)
    return img


def _gradient_region(draw: ImageDraw.ImageDraw, x0: int, y0: int,
                     top: tuple[int, int, int], bottom: tuple[int, int, int],
                     seam: bool = True) -> None:
    """绘制一个 64x64 的纵向金属渐变区域。"""
    for row in range(64):
        t = row / 63.0
        color = tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)) + (255,)
        draw.line([(x0, y0 + row), (x0 + 63, y0 + row)], fill=color)

    # 横向面板接缝和高光，避免大面积纯色显得卡通
    if seam:
        for yy in range(10, 64, 14):
            draw.line([(x0, y0 + yy), (x0 + 63, y0 + yy)], fill=(70, 78, 88, 255))
            draw.line([(x0, y0 + yy + 1), (x0 + 63, y0 + yy + 1)], fill=(220, 228, 236, 55))
            for xx in range(5, 64, 18):
                draw.point((x0 + xx, y0 + yy), fill=(225, 232, 240, 180))


def _region_noise(img: Image.Image, x0: int, y0: int, seed: str,
                  amount: int = 3) -> None:
    rng = random.Random(f"newgc-rocket-v2::{seed}")
    pixels = img.load()
    for y in range(y0, y0 + 64):
        for x in range(x0, x0 + 64):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            d = rng.randint(-amount, amount)
            pixels[x, y] = (max(0, min(255, r + d)),
                            max(0, min(255, g + d)),
                            max(0, min(255, b + d)), a)


def rocket_entity_v2() -> Image.Image:
    """256x128 实体图集：三段白灰渐变、金属喷口、深色结构、橙色识别色、玻璃与舱门。"""
    img = Image.new("RGBA", (TEX_W, TEX_H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 0,0 / 64,0 / 128,0：三段机身。越往上越亮，形成从发动机到载人舱的纵向渐变
    _gradient_region(draw, 0, 0, (188, 194, 204), (142, 150, 162))
    _gradient_region(draw, 64, 0, (222, 228, 236), (174, 182, 193))
    _gradient_region(draw, 128, 0, (248, 250, 252), (211, 218, 227))

    # 192,0：深蓝舷窗玻璃，带斜向反光
    _gradient_region(draw, 192, 0, (20, 62, 98), (4, 16, 34), seam=False)
    draw.line([(198, 0), (198, 63)], fill=(80, 210, 255, 150), width=2)
    draw.line([(200, 0), (224, 24)], fill=(180, 245, 255, 130), width=1)
    draw.line([(208, 0), (247, 39)], fill=(110, 210, 250, 85), width=1)

    # 0,64：发动机喷口，深色金属和热痕
    _gradient_region(draw, 0, 64, (126, 132, 142), (44, 48, 56))
    for x in range(6, 64, 9):
        draw.line([(x, 64), (x + 2, 127)], fill=(36, 39, 44, 120), width=1)

    # 64,64：黑色结构/尾翼
    _gradient_region(draw, 64, 64, (58, 63, 72), (20, 22, 27))

    # 128,64：橙色识别色与警示条纹
    _gradient_region(draw, 128, 64, (240, 118, 52), (154, 46, 32), seam=False)
    for x in range(128, 192, 16):
        draw.polygon([(x, 64), (x + 6, 64), (x + 24, 127), (x + 18, 127)],
                     fill=(255, 190, 70, 120))

    # 192,64：舱门面板与铆钉
    _gradient_region(draw, 192, 64, (104, 110, 120), (42, 46, 53))
    draw.rectangle([198, 69, 249, 120], outline=(25, 28, 33, 255), width=2)
    draw.rectangle([203, 74, 244, 115], outline=(170, 180, 192, 75), width=1)
    draw.rectangle([231, 91, 235, 104], fill=(235, 150, 42, 255))
    for xx, yy in ((200, 70), (246, 70), (200, 117), (246, 117)):
        draw.point((xx, yy), fill=(215, 222, 230, 220))

    for x0, y0, seed in ((0, 0, "hull_low"), (64, 0, "hull_mid"),
                         (128, 0, "hull_top"), (192, 0, "glass"),
                         (0, 64, "engine"), (64, 64, "dark"),
                         (128, 64, "accent"), (192, 64, "hatch")):
        _region_noise(img, x0, y0, seed)

    return img


def rocket_entity_tier2() -> Image.Image:
    """二阶火箭图集：白色箭体染上冷蓝，橙色识别带改为青蓝。"""
    img = rocket_entity_v2().copy()
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            if r > g * 1.15 and g > b * 1.20 and r > 100:
                # 橙色/黄色识别色 → 青蓝色能量色
                glow = min(255, int((r + g) * 0.55))
                pixels[x, y] = (24, min(245, 120 + glow // 2), min(255, 190 + glow // 3), a)
            elif r > 150 and g > 150 and b > 150:
                # 白色箭体加入轻微冷蓝高光
                pixels[x, y] = (max(0, r - 16), min(255, g + 3), min(255, b + 12), a)
    return img


def rocket_entity_tier3() -> Image.Image:
    """三阶火箭图集：白金箭体、金色识别带，与二阶青色涂装区分。"""
    img = rocket_entity_v2().copy()
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            # 仅修改 128,64 的识别色区域，保持舷窗玻璃仍是蓝色。
            if 128 <= x < 192 and 64 <= y < 128:
                if r > g * 1.15 and g > b * 1.20 and r > 100:
                    glow = min(255, int((r + g) * 0.55))
                    pixels[x, y] = (min(255, 235 + glow // 8),
                                    min(255, 150 + glow // 3),
                                    min(255, 42 + glow // 7), a)
            elif r > 150 and g > 150 and b > 150:
                # 白色箭体加入暖金色高光。
                pixels[x, y] = (min(255, r + 10), min(255, g + 2),
                                max(0, b - 18), a)
    return img


def main() -> None:
    for directory in (BLOCK_DIR, ITEM_DIR, ENTITY_DIR):
        directory.mkdir(parents=True, exist_ok=True)

    outputs = [
        (BLOCK_DIR / "launch_pad_top.png", launch_pad_top()),
        (BLOCK_DIR / "launch_pad_side.png", launch_pad_side()),
        (ITEM_DIR / "rocket.png", rocket_item()),
        (ITEM_DIR / "tier2_rocket.png", tier2_rocket_item()),
        (ITEM_DIR / "tier3_rocket.png", tier3_rocket_item()),
        (ENTITY_DIR / "rocket.png", rocket_entity_v2()),
        (ENTITY_DIR / "tier2_rocket.png", rocket_entity_tier2()),
        (ENTITY_DIR / "tier3_rocket.png", rocket_entity_tier3()),
    ]
    for path, image in outputs:
        image.save(path, optimize=True)
        print(f"wrote {path.relative_to(ROOT)} ({image.width}x{image.height})")

    print(f"Generated {len(outputs)} rocket textures.")


if __name__ == "__main__":
    main()
