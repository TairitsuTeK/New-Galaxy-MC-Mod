# Minecraft Java Edition Mod: New Galaxy新星系(Demo)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.1+-red.svg)](https://www.neoforged.net)

**New Galaxy** 是基于 Minecraft Java Edition 1.21.1 NeoForge 的一款有关星系，并参考 Micdoodle8 的星系mod：[Galacticraft](https://github.com/micdoodle8/Galacticraft) 的模组。

它在较新版本的Minecraft下主要添加了太阳系以及各个主要天体的维度。

---

## 主要内容
1. **太阳系天体维度**
   - 为游戏添加了多个可以传送前往的天体维度（使用传送器打开星图传送。目前只开发了8个维度，剩下的请关注后续开发）
   - 各个星球拥有自己的岩石（表层，浅层，深层3种），同时拥有矿物生成（模组矿物暂时不兼容其他模组，同时安装后矿物/锭/粒/块等物品会独立）
   - 拥有独立的重力系统，模拟现实星球环境的粒子效果及群系生成和天空效果（例如金星独特酸雨效果以及遍地的岩浆池和酸雨池；水星上太阳视直径很大等效果）
     
2. **星图**
   - 太阳系轨道图。太阳放在中心，周围是其他星球的椭圆轨道布局
   - 手持传送器打开星图，点击右上角“传送”按钮即可前往目标星球
   - 星图内的星球沿轨道缓慢公转，轨道为椭圆，模拟真实太阳系的黄道面视角
   - 未解锁的星球显示为灰色轮廓，需等待后续开发

3. **传送系统**
   - 传送器（Teleporter）是本模组的核心工具，必须手持才能打开星图
   - 点击星图中的星球或右上角按钮即可传送到对应维度

4. **重力系统**
   - 每个星球拥有独立的重力倍率，通过 Mixin 注入 Entity.getGravity() 与 LivingEntity.travel() 实现
   - 玩家的跳跃高度、下落速度、投掷物轨迹都会随重力变化
   - 物品实体的重力同样受影响，避免“同一个星球上人和物品重量不一致”的违和感

5. **环境与天气**
   - 各星球拥有独立的天空渲染（自定义 DimensionSpecialEffects），例如水星的纯黑星空 + 高亮太阳
   - 部分星球有独特天气：金星下酸雨、火星有沙尘暴、木星卫星有极光
   - 环境音效与粒子效果按星球区分，例如金星地表有上升的酸雾粒子，火星有飘散的沙尘
