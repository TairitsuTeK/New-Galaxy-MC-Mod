package com.tairitsutek.newgc.client.magnetometer;

import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormClientCache;
import com.tairitsutek.newgc.Planets.mars.client.MarsDustStormClientCache;
import com.tairitsutek.newgc.Planets.mars.client.MarsVolcanoClientCache;
import com.tairitsutek.newgc.Planets.mercury.client.MercurySolarFlareClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * 磁场计客户端数据缓存。每帧计算一次，供 HUD / Screen 读取。
 */
public final class MagnetometerClientCache {

    private static final RandomSource RNG = RandomSource.create();

    private static float strength = 0f;
    private static float angle = 0f;      // 0~360，指向磁北极
    private static float noise = 0f;
    private static String status = "正常";
    private static int statusColor = 0xFF9BB7E0;
    private static float jitterAmount = 0f;

    // 用于"矿脉感应"
    private static boolean ironNearby = false;

    private static long lastUpdateTick = -1;

    private MagnetometerClientCache() {}

    /** 每帧调用一次 */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;

        long tick = level.getGameTime();
        if (tick == lastUpdateTick) return;
        lastUpdateTick = tick;

        ResourceKey<Level> dim = level.dimension();
        float base = MagnetometerData.getBaseStrength(dim);
        float n = MagnetometerData.getNoise(dim);

        // 事件干扰
        String evt = null;
        float evtBoost = 0f;
        int evtColor = 0xFF9BB7E0;

        // 木星风暴
        float jupiterStrength = JupiterStormClientCache.getStormStrength();
        if (jupiterStrength > 0.5f && dim.equals(com.tairitsutek.newgc.Planets.ModDimensions.JUPITER_LEVEL_KEY)) {
            evt = "⚡ 风暴干扰";
            evtBoost = jupiterStrength * 0.8f;
            evtColor = 0xFFFF8A3A;
        }

        // 火星火山
        if (MarsVolcanoClientCache.isErupting() && dim.equals(com.tairitsutek.newgc.Planets.ModDimensions.MARS_LEVEL_KEY)) {
            evt = "🌋 局部磁畸变";
            evtBoost = MarsVolcanoClientCache.getEruptionIntensity() * 0.5f;
            evtColor = 0xFFE06B4B;
        }

        // 火星沙尘暴
        float dust = MarsDustStormClientCache.getStormStrength();
        if (dust > 0.5f && dim.equals(com.tairitsutek.newgc.Planets.ModDimensions.MARS_LEVEL_KEY)) {
            evt = "🌪 沙尘噪声";
            evtBoost = dust * 0.3f;
            evtColor = 0xFFC89650;
        }

        // 水星耀斑
        if (MercurySolarFlareClientCache.isFlaring() && dim.equals(com.tairitsutek.newgc.Planets.ModDimensions.MERCURY_LEVEL_KEY)) {
            evt = "☀ 耀斑脉冲";
            evtBoost = MercurySolarFlareClientCache.getIntensity() * 1.0f;
            evtColor = 0xFFFFE0A0;
        }

        strength = base + evtBoost;
        noise = n;

        // 角度：指向磁北
        double northX = MagnetometerData.getNorthX(dim);
        double northZ = MagnetometerData.getNorthZ(dim);
        double dx = northX - player.getX();
        double dz = northZ - player.getZ();

        double targetAngle = Math.toDegrees(Math.atan2(-dx, dz));
        if (targetAngle < 0) targetAngle += 360;

        // 噪声：指针抖动
        float jitter = n * (1f + evtBoost * 0.5f);
        jitterAmount = jitter;
        angle = (float) (targetAngle + (RNG.nextDouble() - 0.5) * jitter * 60);

        // 矿脉感应：附近 16 格有没有铁矿
        ironNearby = checkIronNearby(level, player.blockPosition());

        // 状态文字
        if (evt != null) {
            status = evt;
            statusColor = evtColor;
        } else if (base < 0.05f) {
            status = "磁场失效";
            statusColor = 0xFF8898B0;
        } else if (n > 0.5f) {
            status = "磁场紊乱";
            statusColor = 0xFFFFC060;
        } else {
            status = "正常";
            statusColor = 0xFF9BB7E0;
        }
    }

    private static boolean checkIronNearby(Level level, BlockPos center) {
        // 简单检查：范围内扫描铁矿石
        for (int dx = -8; dx <= 8; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    var block = level.getBlockState(p).getBlock();
                    if (block == net.minecraft.world.level.block.Blocks.IRON_ORE
                            || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ===== Getter =====
    public static float getStrength()      { return strength; }
    public static float getAngle()         { return angle; }
    public static float getNoise()         { return noise; }
    public static String getStatus()       { return status; }
    public static int getStatusColor()     { return statusColor; }
    public static float getJitterAmount()  { return jitterAmount; }
    public static boolean isIronNearby()   { return ironNearby; }
}