#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Minecraft NeoForge 资源文件生成器（快捷版）
用法:
  python gen_assets.py                          # 交互模式（逐项问答）
  python gen_assets.py --batch                  # 批量模式
  python gen_assets.py --batch --force          # 批量 + 覆盖
  python gen_assets.py block mercury_stone      # 单个方块
  python gen_assets.py block tin_ore --ore tin --tool pickaxe
  python gen_assets.py item raw_tin             # 单个物品
"""

import argparse
import json
import os
import sys
from pathlib import Path

# ==================== 配置 ====================
MOD_ID      = "newgc"
ASSETS_ROOT = "src/main/resources/assets"
DATA_ROOT   = "src/main/resources/data"

ORE_TYPES = {
    "iron":   "minecraft:raw_iron",
    "copper": "minecraft:raw_copper",
    "tin":      "newgc:raw_tin",
    "aluminum": "newgc:raw_aluminum",
    "lead":     "newgc:raw_lead",
    "silver":   "newgc:raw_silver",
    "nickel":   "newgc:raw_nickel",
    "zinc":     "newgc:raw_zinc",
    "platinum": "newgc:raw_platinum",
    "titanium": "newgc:raw_titanium",
    "uranium":  "newgc:raw_uranium",
}

ORE_LABELS = {
    "iron": "铁矿石", "copper": "铜矿石", "tin": "锡矿石",
    "aluminum": "铝矿石", "lead": "铅矿石", "silver": "银矿石",
    "nickel": "镍矿石", "zinc": "锌矿石", "platinum": "铂矿石",
    "titanium": "钛矿石", "uranium": "铀矿石",
}

TOOL_TAGS = {
    "pickaxe": "mineable/pickaxe",
    "shovel":  "mineable/shovel",
    "axe":     "mineable/axe",
    "hoe":     "mineable/hoe",
}

# 合成表类型（菜单顺序）
RECIPE_MENU = [
    ("crafting",           "普通合成表（有序/无序）"),
    ("smelting",           "熔炉"),
    ("blasting",           "高炉"),
    ("smoking",            "烟熏炉"),
    ("campfire_cooking",   "营火"),
    ("stonecutting",       "切石机"),
    ("smithing_transform", "锻造台"),
]

# 烧制类配方的默认时长（tick）
COOKING_TIME = {
    "smelting":         200,
    "blasting":         100,
    "smoking":          100,
    "campfire_cooking": 600,
}

# ==================== 批量清单（只改这里）====================
BATCH_BLOCKS = [
    ("moon_surface", None),
    ("moon_sub_surface", None),
    ("moon_stone", None),
    ("moon_ore_iron", "iron"),
    ("moon_ore_copper", "copper"),
    ("tin_ore", "tin"),
    ("aluminum_ore", "aluminum"),
]

BATCH_ITEMS = [
    # "example_item",
    # ("raw_tin", "锡石", "Raw Tin"),
    # ("tin_ingot", "锡锭", "Tin Ingot"),
]
# ========================================================


# ---------- 通用工具 ----------
def write_json(path: Path, data: dict, force=False):
    if path.exists() and not force:
        print(f"  [跳过] {path}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"  [创建] {path}")


def load_json(path: Path):
    if not path.exists():
        return {}
    try:
        with open(path, "r", encoding="utf-8") as f:
            content = f.read().strip()
        if not content:
            return {}
        if content.startswith("\ufeff"):
            content = content[1:]
        return json.loads(content)
    except json.JSONDecodeError as e:
        print(f"  [警告] {path} 非法 JSON: {e}，跳过该文件。")
        return None


def merge_lang(path: Path, entries: dict):
    data = load_json(path)
    if data is None:
        return
    changed = False
    for k, v in entries.items():
        if k not in data:
            data[k] = v
            changed = True
    if changed:
        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(dict(sorted(data.items())), f, indent=2, ensure_ascii=False)
            f.write("\n")
        print(f"  [更新] {path}")
    else:
        print(f"  [未变] {path}")


def validate_id(ident: str) -> bool:
    return len(ident) > 0 and all(c.islower() or c.isdigit() or c == "_" for c in ident)


def full_id(item: str, default_ns: str = MOD_ID) -> str:
    return item if ":" in item else f"{default_ns}:{item}"


# ---------- 交互工具 ----------
def ask_yes_no(prompt, default=False):
    suffix = " [Y/n]: " if default else " [y/N]: "
    ans = input(prompt + suffix).strip().lower()
    if ans == "":
        return default
    return ans in ("y", "yes")


def ask_choice(prompt, choices):
    print(prompt)
    for i, (key, label) in enumerate(choices, 1):
        print(f"  {i}. {label}")
    while True:
        s = input("选择编号: ").strip()
        if s.isdigit() and 1 <= int(s) <= len(choices):
            return choices[int(s) - 1][0]
        print("编号无效。")


def ask_int(prompt, default=None, min_value=None):
    while True:
        s = input(prompt).strip()
        if s == "" and default is not None:
            return default
        try:
            v = int(s)
        except ValueError:
            print("请输入整数。")
            continue
        if min_value is not None and v < min_value:
            print(f"数值不能小于 {min_value}。")
            continue
        return v


# ---------- 标签 ----------
def add_to_tag(tag_name: str, block_id: str):
    path = Path(DATA_ROOT) / "minecraft" / "tags" / "block" / f"{tag_name}.json"
    data = load_json(path)
    if data is None:
        return
    data.setdefault("replace", False)
    values = data.setdefault("values", [])
    entry = f"{MOD_ID}:{block_id}"
    if entry in values:
        print(f"  [已在标签] {entry} → minecraft:{tag_name}")
        return
    values.append(entry)
    write_json(path, data, force=True)
    print(f"  [加入标签] {entry} → minecraft:{tag_name}")


# ---------- 掉落表 ----------
def silk_touch_condition():
    return {
        "condition": "minecraft:match_tool",
        "predicate": {
            "predicates": {
                "minecraft:enchantments": [{
                    "enchantments": "minecraft:silk_touch",
                    "levels": {"min": 1},
                }]
            }
        },
    }


def normal_loot(block_id):
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1, "bonus_rolls": 0,
            "entries": [{
                "type": "minecraft:alternatives",
                "children": [
                    {"type": "minecraft:item", "name": f"{MOD_ID}:{block_id}",
                     "conditions": [silk_touch_condition()]},
                    {"type": "minecraft:item", "name": f"{MOD_ID}:{block_id}",
                     "functions": [{"function": "minecraft:explosion_decay"}]},
                ],
            }],
        }],
    }


def ore_loot(block_id, raw_item, drop_min=1, drop_max=1):
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1, "bonus_rolls": 0,
            "entries": [{
                "type": "minecraft:alternatives",
                "children": [
                    {"type": "minecraft:item", "name": f"{MOD_ID}:{block_id}",
                     "conditions": [silk_touch_condition()]},
                    {"type": "minecraft:item", "name": raw_item,
                     "functions": [
                         {"function": "minecraft:set_count",
                          "count": {"type": "minecraft:uniform",
                                    "min": float(drop_min), "max": float(drop_max)},
                          "add": False},
                         {"function": "minecraft:apply_bonus",
                          "enchantment": "minecraft:fortune",
                          "formula": "minecraft:ore_drops"},
                         {"function": "minecraft:explosion_decay"},
                     ]},
                ],
            }],
        }],
    }


# ---------- 合成表构建 ----------
def build_cooking_recipe(rtype, item, result_full, result_count):
    """熔炉/高炉/烟熏炉/营火 共用的烧制配方。"""
    return {"type": f"minecraft:{rtype}",
            "category": "misc",
            "ingredient": {"item": full_id(item, MOD_ID)},
            "result": {"id": result_full, "count": result_count},
            "experience": 0.7,
            "cookingtime": COOKING_TIME[rtype]}


def build_crafting_recipe(shape, result_full, result_count):
    """普通合成表：有序 / 无序。"""
    if shape == "shaped":
        rows = []
        print("  输入 3 行，每行 3 字符（. 空，字母/数字=材料）：")
        for i in range(3):
            while True:
                r = input(f"  第 {i+1} 行: ").strip()
                if len(r) == 3 and all(c.isalnum() or c == "." for c in r):
                    rows.append(r)
                    break
                print("  必须 3 字符，只能字母/数字/'.'")
        used = sorted({c for r in rows for c in r if c != "."})
        if not used:
            print("  没有材料。")
            return None
        key = {}
        for ch in used:
            item = input(f"  符号 '{ch}' 对应物品 id: ").strip()
            key[ch] = {"item": full_id(item, MOD_ID)}
        return {"type": "minecraft:crafting_shaped", "category": "misc",
                "pattern": rows, "key": key,
                "result": {"id": result_full, "count": result_count}}
    else:
        ingredients = []
        while True:
            item = input(f"  材料 {len(ingredients)+1} id (回车结束): ").strip()
            if not item:
                break
            full = full_id(item, MOD_ID)
            n = ask_int(f"    {full} 的数量 (默认 1): ", default=1, min_value=1)
            for _ in range(n):
                ingredients.append({"item": full})
        if not ingredients:
            print("  没有材料。")
            return None
        return {"type": "minecraft:crafting_shapeless", "category": "misc",
                "ingredients": ingredients,
                "result": {"id": result_full, "count": result_count}}


# ---------- 合成表（方块/物品附带，可选）----------
def build_recipe(result_item):
    rtype = ask_choice("合成类型：", [
        ("none", "不生成"), ("shaped", "有序 3x3"),
        ("shapeless", "无序"), ("smelting", "熔炉"),
        ("blasting", "高炉"), ("stonecutting", "切石机"),
    ])
    if rtype == "none":
        return None
    if rtype == "shaped":
        rows = []
        print("输入 3 行，每行 3 字符（. 空，字母=材料）：")
        for i in range(3):
            while True:
                r = input(f"第 {i+1} 行: ").strip()
                if len(r) == 3 and all(c.isalnum() or c == "." for c in r):
                    rows.append(r)
                    break
                print("必须 3 字符，只能字母/数字/'.'")
        used = sorted({c for r in rows for c in r if c != "."})
        key = {}
        for ch in used:
            item = input(f"符号 '{ch}' 对应物品 id: ").strip()
            if ":" not in item:
                item = f"minecraft:{item}"
            key[ch] = {"item": item}
        return {"type": "minecraft:crafting_shaped", "category": "misc", "pattern": rows,
                "key": key, "result": {"id": result_item, "count": 1}}
    if rtype == "shapeless":
        ingredients = []
        while True:
            item = input(f"材料 {len(ingredients)+1} (回车结束): ").strip()
            if not item:
                break
            if ":" not in item:
                item = f"minecraft:{item}"
            ingredients.append({"item": item})
        if not ingredients:
            return None
        return {"type": "minecraft:crafting_shapeless", "category": "misc",
                "ingredients": ingredients, "result": {"id": result_item, "count": 1}}
    if rtype in ("smelting", "blasting"):
        item = input("原料 id: ").strip()
        if ":" not in item:
            item = f"minecraft:{item}"
        t = 200 if rtype == "smelting" else 100
        return {"type": f"minecraft:{rtype}", "category": "misc",
                "ingredient": {"item": item},
                "result": {"id": result_item, "count": 1},
                "experience": 0.7, "cookingtime": t}
    if rtype == "stonecutting":
        item = input("原料 id: ").strip()
        if ":" not in item:
            item = f"minecraft:{item}"
        return {"type": "minecraft:stonecutting", "ingredient": {"item": item},
                "result": {"id": result_item, "count": 1}}
    return None


# ---------- 核心生成 ----------
def gen_block(block_id, ore_type=None, cn=None, en=None, tool=None, recipe=None, force=False):
    print(f"\n===== 方块: {block_id} =====")
    ore_raw = ORE_TYPES.get(ore_type) if ore_type else None

    if cn is None:
        cn = input("  中文名: ").strip() or block_id
    if en is None:
        en = input("  英文名: ").strip() or block_id

    if tool is None:
        tool = ask_choice("  需要什么工具挖掘？", [
            ("pickaxe", "镐 (pickaxe)"),
            ("shovel",  "锹 (shovel)"),
            ("axe",     "斧 (axe)"),
            ("hoe",     "锄 (hoe)"),
            ("none",    "无（徒手/任意工具）"),
        ])

    drop_min = drop_max = 1
    if ore_raw:
        drop_min = ask_int("  掉落最小值 (默认 1): ", default=1, min_value=0)
        drop_max = ask_int("  掉落最大值 (默认 1): ", default=1, min_value=0)
        if drop_max < drop_min:
            drop_min, drop_max = drop_max, drop_min
            print(f"  [提示] 最大值小于最小值，已交换为 {drop_min}~{drop_max}")

    write_json(Path(ASSETS_ROOT) / MOD_ID / "blockstates" / f"{block_id}.json",
               {"variants": {"": {"model": f"{MOD_ID}:block/{block_id}"}}}, force)
    write_json(Path(ASSETS_ROOT) / MOD_ID / "models" / "block" / f"{block_id}.json",
               {"parent": "minecraft:block/cube_all",
                "textures": {"all": f"{MOD_ID}:block/{block_id}"}}, force)
    write_json(Path(ASSETS_ROOT) / MOD_ID / "models" / "item" / f"{block_id}.json",
               {"parent": f"{MOD_ID}:block/{block_id}"}, force)
    write_json(Path(DATA_ROOT) / MOD_ID / "loot_table" / "blocks" / f"{block_id}.json",
               ore_loot(block_id, ore_raw, drop_min, drop_max) if ore_raw else normal_loot(block_id), force)
    if recipe:
        write_json(Path(DATA_ROOT) / MOD_ID / "recipe" / f"{block_id}.json", recipe, force)

    if tool in TOOL_TAGS:
        add_to_tag(TOOL_TAGS[tool], block_id)

    merge_lang(Path(ASSETS_ROOT) / MOD_ID / "lang" / "zh_cn.json",
               {f"block.{MOD_ID}.{block_id}": cn})
    merge_lang(Path(ASSETS_ROOT) / MOD_ID / "lang" / "en_us.json",
               {f"block.{MOD_ID}.{block_id}": en})

    print(f"  [提示] 贴图: assets/{MOD_ID}/textures/block/{block_id}.png（需自行提供）")


def gen_item(item_id, cn=None, en=None, recipe=None, force=False):
    print(f"\n===== 物品: {item_id} =====")

    if cn is None:
        cn = input("  中文名: ").strip() or item_id
    if en is None:
        en = input("  英文名: ").strip() or item_id

    write_json(Path(ASSETS_ROOT) / MOD_ID / "models" / "item" / f"{item_id}.json",
               {"parent": "minecraft:item/generated",
                "textures": {"layer0": f"{MOD_ID}:item/{item_id}"}}, force)
    if recipe:
        write_json(Path(DATA_ROOT) / MOD_ID / "recipe" / f"{item_id}.json", recipe, force)

    merge_lang(Path(ASSETS_ROOT) / MOD_ID / "lang" / "zh_cn.json",
               {f"item.{MOD_ID}.{item_id}": cn})
    merge_lang(Path(ASSETS_ROOT) / MOD_ID / "lang" / "en_us.json",
               {f"item.{MOD_ID}.{item_id}": en})

    print(f"  [提示] 贴图: assets/{MOD_ID}/textures/item/{item_id}.png（需自行提供）")


# ---------- 合成表菜单 ----------
def run_recipe_menu():
    while True:
        rtype = ask_choice("添加哪种合成表？", RECIPE_MENU + [("exit", "返回上一级")])
        if rtype == "exit":
            return

        result = input("  输出物品 id（无命名空间默认 newgc）: ").strip()
        if not result:
            print("  输出物品不能为空。")
            continue
        result_full = full_id(result, MOD_ID)
        result_count = ask_int("  输出数量 (默认 1): ", default=1, min_value=1)

        recipe = None
        type_id = rtype  # 默认文件名用的类型英文 id

        if rtype == "crafting":
            shape = ask_choice("  有序还是无序？", [
                ("shaped", "有序 3x3"),
                ("shapeless", "无序"),
            ])
            type_id = f"crafting_{shape}"  # crafting_shaped 或 crafting_shapeless
            recipe = build_crafting_recipe(shape, result_full, result_count)
        elif rtype in COOKING_TIME:
            item = input("  原料 id: ").strip()
            if item:
                recipe = build_cooking_recipe(rtype, item, result_full, result_count)
        elif rtype == "stonecutting":
            item = input("  原料 id: ").strip()
            if item:
                recipe = {"type": "minecraft:stonecutting",
                          "ingredient": {"item": full_id(item, MOD_ID)},
                          "result": {"id": result_full, "count": result_count}}
        elif rtype == "smithing_transform":
            template = input("  锻造模板 id: ").strip()
            base = input("  基底物品 id: ").strip()
            addition = input("  添加材料 id: ").strip()
            if template and base and addition:
                recipe = {"type": "minecraft:smithing_transform",
                          "template": {"item": full_id(template, MOD_ID)},
                          "base": {"item": full_id(base, MOD_ID)},
                          "addition": {"item": full_id(addition, MOD_ID)},
                          "result": {"id": result_full, "count": result_count}}

        if recipe is None:
            print("  配方未生成（输入不完整）。")
            continue

        # 默认文件名：<工具英文id>_<产物>.json
        default_name = f"{type_id}_{result.split(':')[-1]}"
        fname = input(f"  合成表文件名 (默认 {default_name}): ").strip() or default_name
        if not validate_id(fname):
            print(f"  [提示] 文件名含非法字符，改用 {default_name}")
            fname = default_name
        write_json(Path(DATA_ROOT) / MOD_ID / "recipe" / f"{fname}.json", recipe)
        print("  完成。")

        if not ask_yes_no("继续添加其它合成表？", default=True):
            return


# ---------- 模式 ----------
def run_batch(force):
    for entry in BATCH_BLOCKS:
        block_id = entry[0]
        ore_type = entry[1] if len(entry) > 1 else None
        cn = entry[2] if len(entry) > 2 else None
        en = entry[3] if len(entry) > 3 else None
        tool = entry[4] if len(entry) > 4 else None
        gen_block(block_id, ore_type, cn, en, tool, force=force)
    for entry in BATCH_ITEMS:
        if isinstance(entry, (tuple, list)):
            item_id = entry[0]
            cn = entry[1] if len(entry) > 1 else None
            en = entry[2] if len(entry) > 2 else None
        else:
            item_id, cn, en = entry, None, None
        gen_item(item_id, cn, en, force=force)
    print("\n批量生成完成。")


def run_interactive():
    while True:
        kind = ask_choice("你要添加的是？", [
            ("block", "方块"), ("item", "物品"), ("recipe", "合成表"), ("exit", "退出"),
        ])
        if kind == "exit":
            return

        if kind == "recipe":
            run_recipe_menu()
        else:
            while True:
                ident = input("id（如 tin_ore）: ").strip()
                if validate_id(ident):
                    break
                print("id 只能含小写字母、数字、下划线。")

            if kind == "block":
                is_ore = ask_yes_no("是否矿石？", default=False)
                ore_type = None
                if is_ore:
                    ore_type = ask_choice("矿石类型：",
                        [(k, ORE_LABELS.get(k, k)) for k in ORE_TYPES])
                recipe = build_recipe(f"{MOD_ID}:{ident}") if ask_yes_no("生成合成表？", default=False) else None
                gen_block(ident, ore_type, recipe=recipe)
            else:
                recipe = build_recipe(f"{MOD_ID}:{ident}") if ask_yes_no("生成合成表？", default=False) else None
                gen_item(ident, recipe=recipe)

        if not ask_yes_no("继续添加下一个？", default=True):
            return


def main():
    p = argparse.ArgumentParser(description="NeoForge 资源生成器（快捷版）")
    p.add_argument("--batch", action="store_true", help="批量模式")
    p.add_argument("--force", action="store_true", help="覆盖已存在文件")
    p.add_argument("kind", nargs="?", choices=["block", "item"], help="block 或 item")
    p.add_argument("ident", nargs="?", help="方块/物品 id")
    p.add_argument("--ore", choices=list(ORE_TYPES.keys()), help="矿石类型")
    p.add_argument("--tool", choices=["pickaxe", "shovel", "axe", "hoe", "none"], help="挖掘工具")
    args = p.parse_args()

    os.chdir(Path(__file__).resolve().parent)

    if args.batch:
        run_batch(args.force)
    elif args.kind == "block" and args.ident:
        if not validate_id(args.ident):
            sys.exit("id 只能含小写字母、数字、下划线。")
        gen_block(args.ident, args.ore, tool=args.tool, force=args.force)
    elif args.kind == "item" and args.ident:
        if not validate_id(args.ident):
            sys.exit("id 只能含小写字母、数字、下划线。")
        gen_item(args.ident, force=args.force)
    else:
        run_interactive()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n用户中断。")
        sys.exit(0)
