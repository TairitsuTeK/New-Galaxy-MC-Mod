package com.tairitsutek.newgc.event.oxygen;

import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class OxygenConfig {

    /** 第一管上限（自身基础氧气） */
    public static final int BASE_MAX_OXYGEN = 100;

    /** 第二管上限（氧气管扩展）—— 初始为空 */
    public static final int TANK_MAX_OXYGEN = 0;

    /** 总上限（硬顶，未来扩展） */
    public static final int ABSOLUTE_MAX_OXYGEN = 5_000_000;

    /** 缺氧伤害间隔（tick） */
    public static final int DAMAGE_INTERVAL = 20;   // 1 秒

    /** 缺氧伤害量 = 玩家最大生命的 1/3 */
    public static final float DAMAGE_FRACTION = 1.0f / 3.0f;

    private static final Map<ResourceKey<Level>, Float> OXYGEN_USAGE = new HashMap<>();

    static {
        // 全部维度都消耗氧气
        OXYGEN_USAGE.put(Level.OVERWORLD, 0.05f);
        OXYGEN_USAGE.put(Level.NETHER, 0.05f);
        OXYGEN_USAGE.put(Level.END, 0.05f);

        OXYGEN_USAGE.put(ModDimensions.MOON_LEVEL_KEY, 0.05f);
        OXYGEN_USAGE.put(ModDimensions.MARS_LEVEL_KEY, 0.05f);
        OXYGEN_USAGE.put(ModDimensions.PHOBOS_LEVEL_KEY, 0.05f);
        OXYGEN_USAGE.put(ModDimensions.DEIMOS_LEVEL_KEY, 0.05f);
        OXYGEN_USAGE.put(ModDimensions.MERCURY_LEVEL_KEY, 0.075f);
        OXYGEN_USAGE.put(ModDimensions.VENUS_LEVEL_KEY, 0.15f);
        OXYGEN_USAGE.put(ModDimensions.CERES_LEVEL_KEY, 0.05f);
        OXYGEN_USAGE.put(ModDimensions.JUPITER_LEVEL_KEY, 0.1f);
    }

    public static float getUsage(ResourceKey<Level> dim) {
        Float usage = OXYGEN_USAGE.get(dim);
        return usage == null ? 0.05f : usage;   // 默认消耗
    }

    public static boolean hasOxygen(ResourceKey<Level> dim) {
        return getUsage(dim) <= 0.0f;
    }

    /** 永远返回 true（全维度显示） */
    public static boolean isVacuum(ResourceKey<Level> dim) {
        return true;
    }
}