package com.tairitsutek.newgc.client.debug;

/**
 * 客户端调试状态：
 * 记录"强制目标强度"，每刻由各个 ClientCache 平滑逼近。
 * 由 HUD 下方的调试按钮写入。
 */
public final class DebugState {

    // ===== 木星 =====
    public static final int JUPITER_NONE = -1;
    public static final int JUPITER_CALM = 0;
    public static final int JUPITER_RISING = 1;
    public static final int JUPITER_PEAK = 2;
    public static final int JUPITER_SUDDEN = 3;
    public static final int JUPITER_FALLING = 4;

    private static int jupiterTarget = JUPITER_NONE;
    private static float jupiterTargetStrength = -1f;
    private static float jupiterSmoothed = -1f;

    // ===== 火星沙尘暴 =====
    public static final int MARS_DUST_NONE = -1;
    public static final int MARS_DUST_CALM = 0;
    public static final int MARS_DUST_RISING = 1;
    public static final int MARS_DUST_PEAK = 2;
    public static final int MARS_DUST_FALLING = 3;

    private static int marsDustTarget = MARS_DUST_NONE;
    private static float marsDustTargetStrength = -1f;
    private static float marsDustSmoothed = -1f;

    private DebugState() {}

    // ===== 木星 =====
    public static void setJupiterPhase(int phase) {
        jupiterTarget = phase;
        jupiterTargetStrength = switch (phase) {
            case JUPITER_CALM -> 0.05f;
            case JUPITER_RISING -> 0.55f;
            case JUPITER_PEAK -> 1.25f;
            case JUPITER_SUDDEN -> 2.3f;
            case JUPITER_FALLING -> 0.55f;
            default -> -1f;
        };
        if (jupiterTargetStrength < 0) {
            jupiterSmoothed = -1f;
        } else if (jupiterSmoothed < 0) {
            jupiterSmoothed = 0f;
        }
    }

    public static int getJupiterPhase() { return jupiterTarget; }

    /** 返回 -1 表示没有强制目标 */
    public static float getJupiterForcedStrength() { return jupiterTargetStrength; }

    /** 每刻调用，平滑逼近目标，返回当前平滑值 */
    public static float tickJupiterSmooth() {
        if (jupiterTargetStrength < 0) {
            jupiterSmoothed = -1f;
            return -1f;
        }
        if (jupiterSmoothed < 0) {
            jupiterSmoothed = 0f;
        }
        jupiterSmoothed += (jupiterTargetStrength - jupiterSmoothed) * 0.03f;
        return jupiterSmoothed;
    }

    // ===== 火星沙尘暴 =====
    public static void setMarsDustPhase(int phase) {
        marsDustTarget = phase;
        marsDustTargetStrength = switch (phase) {
            case MARS_DUST_CALM -> 0.05f;
            case MARS_DUST_RISING -> 0.5f;
            case MARS_DUST_PEAK -> 1.3f;
            case MARS_DUST_FALLING -> 0.5f;
            default -> -1f;
        };
        if (marsDustTargetStrength < 0) {
            marsDustSmoothed = -1f;
        } else if (marsDustSmoothed < 0) {
            marsDustSmoothed = 0f;
        }
    }

    public static int getMarsDustPhase() { return marsDustTarget; }

    public static float getMarsDustForcedStrength() { return marsDustTargetStrength; }

    public static float tickMarsDustSmooth() {
        if (marsDustTargetStrength < 0) {
            marsDustSmoothed = -1f;
            return -1f;
        }
        if (marsDustSmoothed < 0) {
            marsDustSmoothed = 0f;
        }
        marsDustSmoothed += (marsDustTargetStrength - marsDustSmoothed) * 0.03f;
        return marsDustSmoothed;
    }
}