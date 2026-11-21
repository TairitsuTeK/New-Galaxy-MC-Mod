#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成与太阳系一致的 16px Proxima b 方块贴图和 GUI 图标。"""

from __future__ import annotations

import json
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / "tools"))

from generate_textures import PLANETS, ore_block, terrain_base  # noqa: E402

BLOCK_DIR = ROOT / "src/main/resources/assets/newgc/textures/block"
GUI_DIR = ROOT / "src/main/resources/assets/newgc/textures/gui"

# 与太阳系 GUI 图标一致：关闭线性模糊，保留 16px 像素边缘。
GUI_META = json.dumps({"texture": {"blur": False, "clamp": True}},
                      ensure_ascii=False, indent=2) + "\n"


def star_icon(name: str, core: tuple[int, int, int], glow: tuple[int, int, int]) -> None:
    """8x8 opaque star icon, matching the existing sun_icon pixel scale."""
    img = Image.new("RGBA", (8, 8), glow + (255,))
    draw = ImageDraw.Draw(img)
    for y in range(8):
        for x in range(8):
            dx, dy = x - 3.5, y - 3.5
            distance = (dx * dx + dy * dy) ** 0.5
            if distance <= 2.2:
                color = core
            else:
                t = min(1.0, (distance - 2.2) / 3.8)
                color = tuple(int(core[i] + (glow[i] - core[i]) * t) for i in range(3))
            draw.point((x, y), fill=color + (255,))
    img.save(GUI_DIR / name, optimize=True)
    (GUI_DIR / f"{name}.mcmeta").write_text(GUI_META, encoding="utf-8")


def main() -> None:
    BLOCK_DIR.mkdir(parents=True, exist_ok=True)
    GUI_DIR.mkdir(parents=True, exist_ok=True)
    palette = PLANETS["proxima_b"]

    surface = terrain_base("proxima_b_surface", palette, "surface")
    sub = terrain_base("proxima_b_sub_surface", palette, "sub")
    stone = terrain_base("proxima_b_stone", palette, "stone")
    outputs = {
        "proxima_b_surface.png": surface,
        "proxima_b_sub_surface.png": sub,
        "proxima_b_stone.png": stone,
        "proxima_b_ore_iron.png": ore_block("proxima_b_ore_iron"),
        "proxima_b_ore_copper.png": ore_block("proxima_b_ore_copper"),
    }
    for name, image in outputs.items():
        image.save(BLOCK_DIR / name, optimize=True)
        print("wrote 16px block/" + name)

    # 使用与太阳系天体图标相同的 16x16 方形像素贴图，而不是透明圆形图标。
    icon = terrain_base("proxima_b_icon", palette, "surface")
    icon.save(GUI_DIR / "proxima_b_icon.png", optimize=True)
    (GUI_DIR / "proxima_b_icon.png.mcmeta").write_text(GUI_META, encoding="utf-8")

    star_icon("alpha_centauri_a_star.png", (255, 240, 198), (228, 178, 94))
    star_icon("alpha_centauri_b_star.png", (255, 210, 156), (220, 128, 70))
    star_icon("proxima_star.png", (255, 190, 152), (181, 48, 30))
    print("wrote 16px Proxima b icon and 8px star icons")


if __name__ == "__main__":
    main()
