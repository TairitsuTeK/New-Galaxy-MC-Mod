package com.tairitsutek.newgc.Moons.io.client;

public class IoQuakeClientCache {
    private static boolean isQuaking = false;
    private static int quakeTicks = 0;
    private static int quakeTotalTicks = 0;
    private static int cooldownTimer = 0;

    public static void set(boolean quaking, int ticks, int totalTicks, int cooldown) {
        isQuaking = quaking;
        quakeTicks = ticks;
        quakeTotalTicks = totalTicks;
        cooldownTimer = cooldown;
    }

    public static boolean isQuaking() { return isQuaking; }
    public static int getQuakeTicks() { return quakeTicks; }
    public static int getQuakeTotalTicks() { return quakeTotalTicks; }
    public static int getCooldownTimer() { return cooldownTimer; }

    public static float getQuakeIntensity() {
        if (!isQuaking || quakeTotalTicks <= 0) return 0f;
        float progress = 1f - (float) quakeTicks / quakeTotalTicks;
        float envelope = 4f * progress * (1f - progress);
        return 0.25f + 0.75f * envelope;
    }
}