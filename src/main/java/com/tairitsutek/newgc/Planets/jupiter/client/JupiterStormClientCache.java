package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.client.debug.DebugState;

public final class JupiterStormClientCache {

    public enum Phase {
        CALM, RISING, PEAK, SUDDEN, FALLING
    }

    private static int phaseOrdinal = Phase.CALM.ordinal();
    private static int phaseTimer = 0;
    private static float stormStrength = 0f;

    private JupiterStormClientCache() {}

    public static void set(int ordinal, int timer, float strength) {
        // 调试模式下不覆盖网络同步的值（由 DebugState 接管）
        if (DebugState.getJupiterForcedStrength() >= 0) {
            return;
        }
        phaseOrdinal = ordinal;
        phaseTimer = timer;
        stormStrength = strength;
    }

    public static Phase getPhase() {
        // 调试模式优先
        int debugPhase = DebugState.getJupiterPhase();
        if (debugPhase >= 0) {
            Phase[] v = Phase.values();
            return v[Math.max(0, Math.min(v.length - 1, debugPhase))];
        }

        Phase[] v = Phase.values();
        int i = Math.max(0, Math.min(v.length - 1, phaseOrdinal));
        return v[i];
    }

    public static int getPhaseTimer() {
        // 调试模式不显示"剩余"（永远 0）
        if (DebugState.getJupiterForcedStrength() >= 0) return 0;
        return phaseTimer;
    }

    public static float getStormStrength() {
        // 调试模式：平滑逼近目标
        float debug = DebugState.tickJupiterSmooth();
        if (debug >= 0) return debug;
        return stormStrength;
    }
}