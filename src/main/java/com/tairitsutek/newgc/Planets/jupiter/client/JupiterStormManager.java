package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterStormManager {

    // ===== 风暴阶段 =====
    public enum Phase {
        CALM,       // 平静
        RISING,     // 渐强
        PEAK,       // 顶峰
        SUDDEN,     // 突然爆发
        FALLING     // 渐弱
    }

    // ===== 每个阶段的强度范围 =====
    private static final float CALM_MIN = 0.0f;
    private static final float CALM_MAX = 0.15f;

    private static final float RISING_MIN = 0.2f;
    private static final float RISING_MAX = 0.7f;

    private static final float PEAK_MIN = 1.0f;
    private static final float PEAK_MAX = 1.5f;

    private static final float SUDDEN_MIN = 1.8f;
    private static final float SUDDEN_MAX = 2.5f;

    private static final float FALLING_MIN = 0.3f;
    private static final float FALLING_MAX = 0.8f;

    // ===== 每个阶段的持续时间范围（刻）=====
    private static final int CALM_MIN_TICKS = 400;    // 20 秒
    private static final int CALM_MAX_TICKS = 1200;   // 60 秒

    private static final int RISING_MIN_TICKS = 100;  // 5 秒
    private static final int RISING_MAX_TICKS = 300;  // 15 秒

    private static final int PEAK_MIN_TICKS = 100;    // 5 秒
    private static final int PEAK_MAX_TICKS = 400;    // 20 秒

    private static final int SUDDEN_MIN_TICKS = 40;   // 2 秒
    private static final int SUDDEN_MAX_TICKS = 120;  // 6 秒

    private static final int FALLING_MIN_TICKS = 100; // 5 秒
    private static final int FALLING_MAX_TICKS = 300; // 15 秒

    // ===== 当前状态 =====
    private static Phase currentPhase = Phase.CALM;
    private static int phaseTimer = 0;
    private static float stormStrength = CALM_MIN;

    private static final RandomSource RNG = RandomSource.create();

    public static float getStormStrength() {
        return stormStrength;
    }

    public static Phase getCurrentPhase() {
        return currentPhase;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        boolean jupiterLoaded = false;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) {
                jupiterLoaded = true;
                break;
            }
        }
        if (!jupiterLoaded) {
            stormStrength = CALM_MIN;
            currentPhase = Phase.CALM;
            phaseTimer = 0;
            return;
        }

        phaseTimer--;

        // 当前阶段结束，切换到下一个阶段
        if (phaseTimer <= 0) {
            switchToNextPhase();
        }

        // 根据当前阶段计算强度
        updateStrength();
    }

    // ==================== 阶段切换 ====================
    private static void switchToNextPhase() {
        Phase next;
        switch (currentPhase) {
            case CALM -> {
                // 平静后：70% 渐强，30% 突然爆发
                next = RNG.nextFloat() < 0.7f ? Phase.RISING : Phase.SUDDEN;
            }
            case RISING -> {
                // 渐强后：80% 进入顶峰，20% 直接渐弱
                next = RNG.nextFloat() < 0.8f ? Phase.PEAK : Phase.FALLING;
            }
            case PEAK -> {
                // 顶峰后：50% 再来一次突然爆发，50% 渐弱
                next = RNG.nextFloat() < 0.5f ? Phase.SUDDEN : Phase.FALLING;
            }
            case SUDDEN -> {
                // 突然爆发后：60% 进入顶峰，40% 直接渐弱
                next = RNG.nextFloat() < 0.6f ? Phase.PEAK : Phase.FALLING;
            }
            case FALLING -> {
                // 渐弱后：回到平静
                next = Phase.CALM;
            }
            default -> next = Phase.CALM;
        }

        currentPhase = next;
        phaseTimer = getRandomDuration(next);
    }

    // ==================== 强度计算 ====================
    private static void updateStrength() {
        switch (currentPhase) {
            case CALM -> stormStrength = lerp(CALM_MIN, CALM_MAX, RNG.nextFloat());
            case RISING -> stormStrength = lerp(RISING_MIN, RISING_MAX, RNG.nextFloat());
            case PEAK -> stormStrength = lerp(PEAK_MIN, PEAK_MAX, RNG.nextFloat());
            case SUDDEN -> stormStrength = lerp(SUDDEN_MIN, SUDDEN_MAX, RNG.nextFloat());
            case FALLING -> stormStrength = lerp(FALLING_MIN, FALLING_MAX, RNG.nextFloat());
        }

        // 兜底钳制
        if (Float.isNaN(stormStrength) || Float.isInfinite(stormStrength)) {
            stormStrength = CALM_MIN;
        }
        stormStrength = Math.max(0.0f, Math.min(SUDDEN_MAX, stormStrength));
    }

    // ==================== 工具 ====================
    private static int getRandomDuration(Phase phase) {
        return switch (phase) {
            case CALM -> CALM_MIN_TICKS + RNG.nextInt(CALM_MAX_TICKS - CALM_MIN_TICKS + 1);
            case RISING -> RISING_MIN_TICKS + RNG.nextInt(RISING_MAX_TICKS - RISING_MIN_TICKS + 1);
            case PEAK -> PEAK_MIN_TICKS + RNG.nextInt(PEAK_MAX_TICKS - PEAK_MIN_TICKS + 1);
            case SUDDEN -> SUDDEN_MIN_TICKS + RNG.nextInt(SUDDEN_MAX_TICKS - SUDDEN_MIN_TICKS + 1);
            case FALLING -> FALLING_MIN_TICKS + RNG.nextInt(FALLING_MAX_TICKS - FALLING_MIN_TICKS + 1);
        };
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}