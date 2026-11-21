#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""列出 Java 源码中仍包含中文的字符串字面量（用于迁移语言键）。"""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
STRING_RE = re.compile(r'"[^"\n]*[\u4e00-\u9fff][^"\n]*"')


def main() -> None:
    for path in sorted((ROOT / "src/main/java").rglob("*.java")):
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if line.lstrip().startswith(("//", "*", "/*")):
                continue
            if STRING_RE.search(line):
                print(f"{path.relative_to(ROOT)}:{number}: {line.strip()}")


if __name__ == "__main__":
    main()
