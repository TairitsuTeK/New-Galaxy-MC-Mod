package com.tairitsutek.newgc.Planets.mars.client;

import net.minecraft.core.BlockPos;

/**
 * 客户端火山状态镜像：由 MarsVolcanoPayload 写入，HUD/相机/粒子读取。
 */
public final class MarsVolcanoClientCache {

    private static BlockPos volcanoPos = null;
    private static int eruptionTicks = 0;
    private static int eruptionTotalTicks = 0;
    private static int cooldownTimer = 0;

    private MarsVolcanoClientCache() {}

    public static void set(BlockPos pos, int remain, int total, int cooldown) {
        volcanoPos = pos;
        eruptionTicks = remain;
        eruptionTotalTicks = total;
        cooldownTimer = cooldown;
    }

    public static BlockPos getVolcanoPos()      { return volcanoPos; }
    public static int getEruptionTicks()        { return eruptionTicks; }
    public static int getEruptionTotalTicks()   { return eruptionTotalTicks; }
    public static int getCooldownTimer()        { return cooldownTimer; }

    public static boolean isErupting() {
        return volcanoPos != null && eruptionTicks > 0;
    }

    /**
     * 喷发强度 0~1，中间最强（抛物线包络）
     */
    public static float getEruptionIntensity() {
        if (!isErupting() || eruptionTotalTicks <= 0) return 0f;
        float progress = 1f - (float) eruptionTicks / eruptionTotalTicks;
        float envelope = 4f * progress * (1f - progress);
        return 0.25f + 0.75f * envelope;
    }
}