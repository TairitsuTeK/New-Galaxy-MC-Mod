"""生成主世界的太阳贴图（覆盖 assets/minecraft/textures/environment/sun.png）。

注意：原版太阳的渲染四边形固定约 30x30 世界单位，所以贴图必须是
「透明背景 + 居中圆形太阳」。之前那张 8x8 的纯色贴图会被整张拉成
一个巨大的黄色方块太阳，这个脚本用来把它换成正常大小。
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/minecraft/textures/environment/sun.png"

SIZE = 32


def main() -> None:
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    pixels = img.load()
    center = (SIZE - 1) / 2.0
    core = SIZE * 0.30
    halo = SIZE * 0.47

    for y in range(SIZE):
        for x in range(SIZE):
            dist = ((x - center) ** 2 + (y - center) ** 2) ** 0.5
            if dist <= core:
                t = dist / core
                r = 255
                g = int(255 - 18 * t ** 2)
                b = int(240 - 110 * t ** 3)
                a = 255
            elif dist <= halo:
                t = (dist - core) / (halo - core)
                fade = (1.0 - t) ** 2
                r = 255
                g = int(226 - 46 * t)
                b = int(120 - 60 * t)
                a = int(140 * fade)
            else:
                continue
            pixels[x, y] = (r, g, b, a)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT)
    print(f"wrote {OUT.relative_to(ROOT)} ({SIZE}x{SIZE})")


if __name__ == "__main__":
    main()
