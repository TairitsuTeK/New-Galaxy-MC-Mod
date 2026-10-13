# Minecraft Java Edition Mod: New Galaxy 新星系（Demo）

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.1+-red.svg)](https://www.neoforged.net)

**New Galaxy** 是一款基于 Minecraft Java Edition 1.21.1 与 NeoForge 的星系模组，灵感来源于 Micdoodle8 的经典模组 [Galacticraft](https://github.com/micdoodle8/Galacticraft)。

它面向较新版本的 Minecraft，主要扩展了太阳系及各大天体的维度，为玩家提供从主世界出发、探索星际的完整体验。

---

## 主要内容

### 1. 太阳系天体维度

- 为游戏添加了多个可传送前往的天体维度（使用传送器打开星图进行传送）。目前已完成 8 个维度的规划，其余维度将在后续版本中陆续开发。
- 每颗星球都拥有独立的岩石体系（表层、浅层、深层三层结构），并拥有专属矿物生成。本模组的矿物、锭、粒、块等物品**自成体系**，暂不与其他模组的同类物品通用，以避免配方冲突。
- 每颗星球拥有独立的重力系统，并结合了贴合现实星球的粒子效果、生物群系生成和天空渲染。例如：金星拥有独特的酸雨效果，地表遍布岩浆池与酸雨池；水星上太阳的视直径极大，天空深邃黑暗。

### 2. 星图

- 太阳系轨道图。太阳位于中心，周围是其他星球按椭圆轨道布局。
- 手持传送器即可打开星图，点击右上角“传送”按钮即可前往目标星球。
- 星图内的星球沿椭圆轨道缓慢公转，模拟真实太阳系黄道面的立体视角。
- 未解锁的星球显示为灰色轮廓，具体解锁机制将在后续版本中开放。

### 3. 传送系统

- 传送器（Teleporter）是本模组的核心工具，必须手持才能打开星图。
- 在星图中点击目标星球或右上角的“传送”按钮，即可传送到对应维度。

### 4. 重力系统

- 每颗星球拥有独立的重力倍率，通过 Mixin 注入 `Entity.getGravity()` 与 `LivingEntity.travel()` 实现。
- 玩家的跳跃高度、下落速度、投掷物轨迹都会随重力变化。
- 物品实体的重力同样受影响，避免“同一个星球上人和物品重量不一致”的违和感。

### 5. 环境与天气

- 每颗星球拥有独立的天空渲染（自定义 `DimensionSpecialEffects`），例如水星的纯黑星空与高亮太阳。
- 部分星球有独特天气：金星下酸雨、火星有沙尘暴、木星卫星有极光。
- 环境音效与粒子效果按星球区分，例如金星地表有上升的酸雾粒子，火星有飘散的沙尘。

### 6. 氧气含量 HUD

- 氧气含量以 **HUD 形式显示在屏幕右下角**。
- HUD 采用条状指示器，直观显示当前剩余氧气百分比。
- 氧气耗尽后，玩家会持续受到窒息伤害，直到离开无氧环境或补充氧气。

> HUD 的具体位置与样式可在配置文件中调整（是否显示、屏幕边距、颜色主题）。

- **右键使用氧气瓶**：手持已充能的氧气瓶，对着自己右键，即可为当前宇航服补充氧气。
- **机器充氧**：与氧气抽取机交互，可自动将氧气瓶充满。

---

## 开发进度

| 模块 | 状态 |
|---|---|
| 星球维度 | 🚧 开发中 |
| 星图 GUI | ✅ 已完成 |
| 传送器 | 🚧 开发中 |
| 重力系统 | ✅ 已完成 |
| 矿物体系 | 🚧 开发中 |
| 火箭系统 | ⏳ 计划中 |

---

## 注意事项

1. **世界兼容性**：模组仍处于 Demo 阶段，维度配置、方块 ID、地形生成参数可能随版本更新发生变动。**升级模组版本后，建议新建世界测试**，以免旧存档出现方块丢失、地形错位或维度加载失败。
2. **矿物独立**：本模组的矿物、锭、粒、块等物品与其它模组**不互通**。如果你同时安装了其他科技或矿物模组，它们的同类物品无法互相替换使用。
3. **客户端与服务端**：部分功能（如自定义天空、星图 GUI、按键绑定）属于**纯客户端逻辑**，但维度生成、方块注册等内容需要服务端同步。**多人游戏中，服务端与客户端的模组版本必须一致**。
4. **可选的 Stellar View 依赖**：如果你希望星空渲染更加丰富，可以额外安装 Stellar View。它不是必需依赖，但安装后能获得更细腻的星空效果。
5. **性能提示**：自定义天空渲染和维度生成会带来额外的渲染与计算开销。低配设备上如果出现卡顿，可以尝试降低渲染距离或关闭粒子效果。
6. **存档备份**：在切换模组版本或调整维度配置前，**务必备份存档**。地形生成参数的改动只对新生成的区块生效，旧区块无法回滚。

---

## 构建指南

如果你想从源码构建本模组，请按以下步骤操作。

### 环境要求

| 工具 | 版本 |
|---|---|
| JDK | 21 |
| Gradle | 随项目 Wrapper 自动下载 |
| IDE | IntelliJ IDEA（推荐）或 Eclipse |

### 构建步骤

1. **克隆仓库**

   ```bash
   git clone https://github.com/TairitsuTeK/New-Galaxy-MC-Mod
   ```

2. **导入项目**

   使用 IntelliJ IDEA 打开项目根目录，等待 Gradle 自动同步依赖。

3. **生成 IDE 运行配置**

   ```bash
   ./gradlew genIntellijRuns
   ```

   Windows 下使用：

   ```bash
   gradlew.bat genIntellijRuns
   ```

4. **运行客户端**

   ```bash
   ./gradlew runClient
   ```

   首次运行会自动下载 Minecraft、NeoForge 与相关依赖，耗时较长。

5. **构建发布 Jar**

   ```bash
   ./gradlew build
   ```

   构建完成后，产物位于：

   ```
   build/libs/newgc-1.0.0.jar
   ```

   `build/libs` 目录下通常会有两个文件，**带 `-sources` 后缀的是源码包，不带后缀的才是可发布的模组 Jar**。

### 常见构建问题

| 问题 | 原因 | 解决 |
|---|---|---|
| `Could not resolve` 依赖失败 | 网络无法访问 Maven 仓库 | 配置代理，或使用国内镜像 |
| `Unsupported class file major version` | JDK 版本不匹配 | 确认使用 JDK 21 |
| 运行时找不到 `neoforge.mods.toml` | 资源目录未正确配置 | 确认 `src/main/resources/META-INF/` 下文件存在 |
| Mixin 未生效 | `neoforge.mods.toml` 缺少 `[[mixins]]` 声明 | 在顶层添加 `[[mixins]] config = "newgc.mixins.json"` |
| 方块显示为紫黑方格 | 纹理文件缺失或路径错误 | 检查 `assets/newgc/textures/` 下对应文件 |

---

## 安装与依赖

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.1 或更高
- **Java**: 21
- **必需依赖**: 无
- **可选依赖**: [Stellar View](https://modrinth.com/mod/stellarview)（用于更丰富的星空渲染）

---

## 开源协议

本项目使用 **MIT License**。你可以自由使用、修改、分发本项目，但需保留原作者的版权声明。

---

## 致谢

- **Micdoodle8** 的 [Galacticraft](https://github.com/micdoodle8/Galacticraft)：本模组的灵感来源与参考对象
- **NeoForge** 团队：提供了强大的模组开发框架
- **Mojang**：创造了 Minecraft 这个无限可能的平台
