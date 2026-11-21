package com.tairitsutek.newgc.client.magnetometer;

import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * 磁场参数表。每个维度一组参数。
 */
public final class MagnetometerData {

    public static final float STRENGTH_MAX = 3.0f;

    private MagnetometerData() {}

    /**
     * 基础磁场强度（0~3）。
     * 无事件、无干扰时的"本底磁场"。
     */
    public static float getBaseStrength(ResourceKey<Level> dim) {
        if (dim.equals(Level.OVERWORLD))                       return 1.0f;
        if (dim.equals(ModDimensions.MOON_LEVEL_KEY))          return 0.05f;
        if (dim.equals(ModDimensions.MARS_LEVEL_KEY))          return 0.10f;
        if (dim.equals(ModDimensions.JUPITER_LEVEL_KEY))       return 3.00f;
        if (dim.equals(ModDimensions.MERCURY_LEVEL_KEY))       return 0.40f;
        if (dim.equals(ModDimensions.VENUS_LEVEL_KEY))         return 0.02f;
        if (dim.equals(ModDimensions.PHOBOS_LEVEL_KEY))        return 0.03f;
        if (dim.equals(ModDimensions.DEIMOS_LEVEL_KEY))        return 0.03f;
        if (dim.equals(ModDimensions.CERES_LEVEL_KEY))         return 0.06f;
        if (dim.equals(ModDimensions.PROXIMA_B_LEVEL_KEY))     return 0.55f;
        if (dim.equals(ModDimensions.SPACE_LEVEL_KEY))         return 0.0f;
        return 0.5f;
    }

    /**
     * 磁场噪声系数（0~1）。越高指针越不稳。
     */
    public static float getNoise(ResourceKey<Level> dim) {
        if (dim.equals(Level.OVERWORLD))                       return 0.05f;
        if (dim.equals(ModDimensions.MOON_LEVEL_KEY))          return 0.50f;
        if (dim.equals(ModDimensions.MARS_LEVEL_KEY))          return 0.30f;
        if (dim.equals(ModDimensions.JUPITER_LEVEL_KEY))       return 1.00f;
        if (dim.equals(ModDimensions.MERCURY_LEVEL_KEY))       return 0.15f;
        if (dim.equals(ModDimensions.VENUS_LEVEL_KEY))         return 0.80f;
        if (dim.equals(ModDimensions.PROXIMA_B_LEVEL_KEY))     return 0.18f;
        if (dim.equals(ModDimensions.SPACE_LEVEL_KEY))         return 0.0f;
        return 0.30f;
    }

    /**
     * 磁北极方向（世界坐标里的一个固定点）。
     * 目前所有维度都设成 (0, 0)，玩家朝磁北 = 朝世界原点。
     */
    public static double getNorthX(ResourceKey<Level> dim) { return 0.0; }
    public static double getNorthZ(ResourceKey<Level> dim) { return 0.0; }

    /** 归一化到 0~1 用于进度条 */
    public static float normalize(float strength) {
        return Math.max(0f, Math.min(1f, strength / STRENGTH_MAX));
    }

    /** 磁场等级描述（从语言文件读取） */
    public static String levelText(float strength) {
        String key;
        if (strength < 0.05f) key = "very_weak";
        else if (strength < 0.3f) key = "weak";
        else if (strength < 0.8f) key = "medium";
        else if (strength < 1.5f) key = "normal";
        else if (strength < 2.2f) key = "strong";
        else key = "extreme";
        return Component.translatable("gui.newgc.magnetometer.level." + key).getString();
    }
}