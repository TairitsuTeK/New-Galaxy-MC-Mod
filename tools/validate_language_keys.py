#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查语言文件 JSON、en/zh 键一致性和 Java 中静态翻译键是否存在。"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LANG_DIR = ROOT / "src/main/resources/assets/newgc/lang"
JAVA_DIR = ROOT / "src/main/java"
KEY_RE = re.compile(r'Component\.translatable\(\s*"([^"]+)"')


def main() -> None:
    languages = {}
    for path in sorted(LANG_DIR.glob("*.json")):
        languages[path.stem] = json.loads(path.read_text(encoding="utf-8"))
        print(f"OK {path.name}: {len(languages[path.stem])} keys")

    en = languages.get("en_us", {})
    zh = languages.get("zh_cn", {})
    print("en/zh key parity:", set(en) == set(zh))
    if set(en) != set(zh):
        print("  missing in zh:", sorted(set(en) - set(zh))[:20])
        print("  missing in en:", sorted(set(zh) - set(en))[:20])

    used = set()
    for path in JAVA_DIR.rglob("*.java"):
        used.update(KEY_RE.findall(path.read_text(encoding="utf-8")))
    missing = sorted(k for k in used if k not in en and not k.endswith("."))
    print(f"static translatable keys: {len(used)}; missing in en_us: {len(missing)}")
    for key in missing[:40]:
        print("  missing:", key)


if __name__ == "__main__":
    main()
