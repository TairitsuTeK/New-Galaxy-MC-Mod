package com.tairitsutek.newgc.Planets.mars.client;

public final class MarsDustStormClientCache {

    public enum Phase {
        CALM, RISING, PEAK, FALLING
    }

    private static int phaseOrdinal = Phase.CALM.ordinal();
    private static int phaseTimer = 0;
    private static float stormStrength = 0f;

    private MarsDustStormClientCache() {}

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

    public static int getPhaseTimer()      { return phaseTimer; }
    public static float getStormStrength() { return stormStrength; }
}