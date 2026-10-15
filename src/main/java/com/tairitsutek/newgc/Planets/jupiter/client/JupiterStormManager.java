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

    public enum Phase {
        CALM, RISING, PEAK, SUDDEN, FALLING
    }

    private static final float CALM_MIN = 0.0f, CALM_MAX = 0.15f;
    private static final float RISING_MIN = 0.2f, RISING_MAX = 0.7f;
    private static final float PEAK_MIN = 1.0f, PEAK_MAX = 1.5f;
    private static final float SUDDEN_MIN = 1.8f, SUDDEN_MAX = 2.5f;
    private static final float FALLING_MIN = 0.3f, FALLING_MAX = 0.8f;

    // 阶段持续时间：适中，不太长
    private static final int CALM_MIN_TICKS = 600, CALM_MAX_TICKS = 1200;
    private static final int RISING_MIN_TICKS = 300, RISING_MAX_TICKS = 600;
    private static final int PEAK_MIN_TICKS = 300, PEAK_MAX_TICKS = 600;
    private static final int SUDDEN_MIN_TICKS = 200, SUDDEN_MAX_TICKS = 400;
    private static final int FALLING_MIN_TICKS = 300, FALLING_MAX_TICKS = 600;

    // 平滑系数：5 秒追上
    private static final float SMOOTH = 0.01f;

    // 阶段内正弦波幅度和周期
    private static final float WAVE_AMPLITUDE = 0.1f;
    private static final float WAVE_PERIOD = 0.01f;

    private static Phase currentPhase = Phase.CALM;
    private static int phaseTimer = 0;
    private static int globalTick = 0;
    private static float baseTarget = CALM_MIN;
    private static float targetStrength = CALM_MIN;
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
            targetStrength = CALM_MIN;
            baseTarget = CALM_MIN;
            currentPhase = Phase.CALM;
            phaseTimer = 0;
            return;
        }

        globalTick++;

        phaseTimer--;
        if (phaseTimer <= 0) {
            switchToNextPhase();
            baseTarget = getRandomStrengthForPhase(currentPhase);
        }

        // 阶段内叠加正弦波，避免完全静止
        float wave = (float) Math.sin(globalTick * WAVE_PERIOD) * WAVE_AMPLITUDE;
        targetStrength = baseTarget + wave;

        // 平滑跟随
        stormStrength += (targetStrength - stormStrength) * SMOOTH;

        if (Float.isNaN(stormStrength) || Float.isInfinite(stormStrength)) {
            stormStrength = CALM_MIN;
        }
        stormStrength = Math.max(0.0f, Math.min(SUDDEN_MAX, stormStrength));
    }

    private static void switchToNextPhase() {
        Phase next;
        switch (currentPhase) {
            case CALM -> next = RNG.nextFloat() < 0.7f ? Phase.RISING : Phase.SUDDEN;
            case RISING -> next = RNG.nextFloat() < 0.8f ? Phase.PEAK : Phase.FALLING;
            case PEAK -> next = RNG.nextFloat() < 0.5f ? Phase.SUDDEN : Phase.FALLING;
            case SUDDEN -> next = RNG.nextFloat() < 0.6f ? Phase.PEAK : Phase.FALLING;
            case FALLING -> next = Phase.CALM;
            default -> next = Phase.CALM;
        }
        currentPhase = next;
        phaseTimer = getRandomDuration(next);
    }

    private static float getRandomStrengthForPhase(Phase phase) {
        return switch (phase) {
            case CALM -> lerp(CALM_MIN, CALM_MAX, RNG.nextFloat());
            case RISING -> lerp(RISING_MIN, RISING_MAX, RNG.nextFloat());
            case PEAK -> lerp(PEAK_MIN, PEAK_MAX, RNG.nextFloat());
            case SUDDEN -> lerp(SUDDEN_MIN, SUDDEN_MAX, RNG.nextFloat());
            case FALLING -> lerp(FALLING_MIN, FALLING_MAX, RNG.nextFloat());
        };
    }

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