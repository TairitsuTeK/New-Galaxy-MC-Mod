package com.tairitsutek.newgc.Planets.jupiter.client;

/**
 * 纯客户端镜像缓存：由 JupiterStormPayload 写入，所有客户端 HUD/特效读取。
 * 不依赖任何服务端状态，避免 Dist 隔离问题。
 */
public final class JupiterStormClientCache {

    public enum Phase {
        CALM, RISING, PEAK, SUDDEN, FALLING
    }

    private static int phaseOrdinal = Phase.CALM.ordinal();
    private static int phaseTimer = 0;
    private static float stormStrength = 0f;

    private JupiterStormClientCache() {}

    public static void set(int ordinal, int timer, float strength) {
        phaseOrdinal = ordinal;
        phaseTimer = timer;
        stormStrength = strength;
    }

    public static Phase getPhase() {
        Phase[] v = Phase.values();
        int i = Math.max(0, Math.min(v.length - 1, phaseOrdinal));
        return v[i];
    }

    public static int getPhaseTimer() { return phaseTimer; }
    public static float getStormStrength() { return stormStrength; }
}