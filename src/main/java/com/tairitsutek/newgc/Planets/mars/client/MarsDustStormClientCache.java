package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.client.debug.DebugState;

public final class MarsDustStormClientCache {

    public enum Phase {
        CALM, RISING, PEAK, FALLING
    }

    private static int phaseOrdinal = Phase.CALM.ordinal();
    private static int phaseTimer = 0;
    private static float stormStrength = 0f;

    private MarsDustStormClientCache() {}

    public static void set(int ordinal, int timer, float strength) {
        if (DebugState.getMarsDustForcedStrength() >= 0) {
            return;
        }
        phaseOrdinal = ordinal;
        phaseTimer = timer;
        stormStrength = strength;
    }

    public static Phase getPhase() {
        int debugPhase = DebugState.getMarsDustPhase();
        if (debugPhase >= 0) {
            Phase[] v = Phase.values();
            return v[Math.max(0, Math.min(v.length - 1, debugPhase))];
        }
        Phase[] v = Phase.values();
        int i = Math.max(0, Math.min(v.length - 1, phaseOrdinal));
        return v[i];
    }

    public static int getPhaseTimer() {
        if (DebugState.getMarsDustForcedStrength() >= 0) return 0;
        return phaseTimer;
    }

    public static float getStormStrength() {
        float debug = DebugState.tickMarsDustSmooth();
        if (debug >= 0) return debug;
        return stormStrength;
    }
}