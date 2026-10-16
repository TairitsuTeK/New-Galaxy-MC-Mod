package com.tairitsutek.newgc.Planets.mercury.client;

import com.tairitsutek.newgc.client.debug.DebugState;

public final class MercurySolarFlareClientCache {

    public enum Phase {
        CALM, BUILDUP, FLARE, DECAY
    }

    private static int phaseOrdinal = Phase.CALM.ordinal();
    private static int phaseTimer = 0;
    private static float intensity = 0f;

    private MercurySolarFlareClientCache() {}

    public static void set(int ordinal, int timer, float value) {
        // 调试模式下不覆盖网络同步的值
        if (DebugState.getMercuryFlareForcedIntensity() >= 0) {
            return;
        }
        phaseOrdinal = ordinal;
        phaseTimer = timer;
        intensity = value;
    }

    public static Phase getPhase() {
        // 调试模式优先
        int debugPhase = DebugState.getMercuryFlarePhase();
        if (debugPhase >= 0) {
            Phase[] v = Phase.values();
            return v[Math.max(0, Math.min(v.length - 1, debugPhase))];
        }
        Phase[] v = Phase.values();
        int i = Math.max(0, Math.min(v.length - 1, phaseOrdinal));
        return v[i];
    }

    public static int getPhaseTimer() {
        if (DebugState.getMercuryFlareForcedIntensity() >= 0) return 0;
        return phaseTimer;
    }

    public static float getIntensity() {
        // 调试模式：平滑逼近目标
        float debug = DebugState.tickMercuryFlareSmooth();
        if (debug >= 0) return debug;
        return intensity;
    }

    public static boolean isFlaring() {
        return getPhase() == Phase.FLARE;
    }
}