package com.tairitsutek.newgc.client.debug;

public final class DebugState {

    // ===== 木星 =====
    public static final int JUPITER_NONE   = -1;
    public static final int JUPITER_CALM   = 0;
    public static final int JUPITER_RISING = 1;
    public static final int JUPITER_PEAK   = 2;
    public static final int JUPITER_SUDDEN = 3;
    public static final int JUPITER_FALLING= 4;

    private static int jupiterTarget = JUPITER_NONE;
    private static float jupiterTargetStrength = -1f;
    private static float jupiterSmoothed = -1f;

    // ===== 火星沙尘暴 =====
    public static final int MARS_DUST_NONE    = -1;
    public static final int MARS_DUST_CALM    = 0;
    public static final int MARS_DUST_RISING  = 1;
    public static final int MARS_DUST_PEAK    = 2;
    public static final int MARS_DUST_FALLING = 3;

    private static int marsDustTarget = MARS_DUST_NONE;
    private static float marsDustTargetStrength = -1f;
    private static float marsDustSmoothed = -1f;

    // ===== 水星耀斑 =====
    public static final int MERCURY_FLARE_NONE    = -1;
    public static final int MERCURY_FLARE_CALM    = 0;
    public static final int MERCURY_FLARE_BUILDUP = 1;
    public static final int MERCURY_FLARE_FLARE   = 2;
    public static final int MERCURY_FLARE_DECAY   = 3;

    private static int mercuryFlareTarget = MERCURY_FLARE_NONE;
    private static float mercuryFlareTargetIntensity = -1f;
    private static float mercuryFlareSmoothed = -1f;

    private DebugState() {}

    // ===== 木星 =====
    public static void setJupiterPhase(int phase) {
        jupiterTarget = phase;
        jupiterTargetStrength = switch (phase) {
            case JUPITER_CALM    -> 0.05f;
            case JUPITER_RISING  -> 0.55f;
            case JUPITER_PEAK    -> 1.25f;
            case JUPITER_SUDDEN  -> 2.3f;
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
    public static float getJupiterForcedStrength() { return jupiterTargetStrength; }

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
            case MARS_DUST_CALM    -> 0.05f;
            case MARS_DUST_RISING  -> 0.5f;
            case MARS_DUST_PEAK    -> 1.3f;
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

    // ===== 水星耀斑 =====
    public static void setMercuryFlarePhase(int phase) {
        mercuryFlareTarget = phase;
        mercuryFlareTargetIntensity = switch (phase) {
            case MERCURY_FLARE_CALM    -> 0.0f;
            case MERCURY_FLARE_BUILDUP -> 0.6f;
            case MERCURY_FLARE_FLARE   -> 1.3f;
            case MERCURY_FLARE_DECAY   -> 0.4f;
            default -> -1f;
        };
        if (mercuryFlareTargetIntensity < 0) {
            mercuryFlareSmoothed = -1f;
        } else if (mercuryFlareSmoothed < 0) {
            mercuryFlareSmoothed = 0f;
        }
    }

    public static int getMercuryFlarePhase() { return mercuryFlareTarget; }
    public static float getMercuryFlareForcedIntensity() { return mercuryFlareTargetIntensity; }

    public static float tickMercuryFlareSmooth() {
        if (mercuryFlareTargetIntensity < 0) {
            mercuryFlareSmoothed = -1f;
            return -1f;
        }
        if (mercuryFlareSmoothed < 0) {
            mercuryFlareSmoothed = 0f;
        }
        mercuryFlareSmoothed += (mercuryFlareTargetIntensity - mercuryFlareSmoothed) * 0.05f;
        return mercuryFlareSmoothed;
    }

    // ===== 恢复 =====
    public static void resetAll() {
        jupiterTarget = JUPITER_NONE;
        jupiterTargetStrength = -1f;
        jupiterSmoothed = -1f;

        marsDustTarget = MARS_DUST_NONE;
        marsDustTargetStrength = -1f;
        marsDustSmoothed = -1f;

        mercuryFlareTarget = MERCURY_FLARE_NONE;
        mercuryFlareTargetIntensity = -1f;
        mercuryFlareSmoothed = -1f;
    }
}