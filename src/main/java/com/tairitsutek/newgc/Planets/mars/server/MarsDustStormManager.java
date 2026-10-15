package com.tairitsutek.newgc.Planets.mars.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.mars.network.MarsDustStormPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 火星沙尘暴状态机（服务端权威）。
 * 节奏比木星风暴慢，强度范围 0~1.5。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MarsDustStormManager {

    public enum Phase {
        CALM, RISING, PEAK, FALLING
    }

    // ===== 各阶段强度范围 =====
    private static final float CALM_MIN = 0.0f,   CALM_MAX = 0.15f;
    private static final float RISING_MIN = 0.3f, RISING_MAX = 0.8f;
    private static final float PEAK_MIN = 1.0f,   PEAK_MAX = 1.5f;
    private static final float FALLING_MIN = 0.3f, FALLING_MAX = 0.7f;

    // ===== 各阶段持续时间（刻）——比木星更长、更慢 =====
    private static final int CALM_MIN_TICKS    = 1200, CALM_MAX_TICKS    = 3000;  // 60~150s
    private static final int RISING_MIN_TICKS  = 600,  RISING_MAX_TICKS  = 1200;  // 30~60s
    private static final int PEAK_MIN_TICKS    = 1200, PEAK_MAX_TICKS    = 2400;  // 60~120s
    private static final int FALLING_MIN_TICKS = 600,  FALLING_MAX_TICKS = 1200;  // 30~60s

    private static final float SMOOTH = 0.008f;       // 比木星更平滑
    private static final float WAVE_AMPLITUDE = 0.08f;
    private static final float WAVE_PERIOD = 0.005f;

    // ===== 同步间隔 =====
    private static final int SYNC_INTERVAL = 10;

    private static Phase currentPhase = Phase.CALM;
    private static int phaseTimer = 0;
    private static int globalTick = 0;
    private static float baseTarget = CALM_MIN;
    private static float targetStrength = CALM_MIN;
    private static float stormStrength = CALM_MIN;

    private static int syncCounter = 0;

    private static final RandomSource RNG = RandomSource.create();

    // ===== Getter（服务端用）=====
    public static float getStormStrength() { return stormStrength; }
    public static Phase getCurrentPhase()  { return currentPhase; }
    public static int getPhaseTimer()      { return phaseTimer; }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel marsLevel = null;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) {
                marsLevel = level;
                break;
            }
        }
        if (marsLevel == null) {
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

        float wave = (float) Math.sin(globalTick * WAVE_PERIOD) * WAVE_AMPLITUDE;
        targetStrength = baseTarget + wave;
        stormStrength += (targetStrength - stormStrength) * SMOOTH;

        if (Float.isNaN(stormStrength) || Float.isInfinite(stormStrength)) {
            stormStrength = CALM_MIN;
        }
        stormStrength = Math.max(0.0f, Math.min(PEAK_MAX, stormStrength));

        // ===== 定期同步 =====
        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            broadcast(marsLevel);
        }
    }

    private static void broadcast(ServerLevel marsLevel) {
        MarsDustStormPayload payload = new MarsDustStormPayload(
                currentPhase.ordinal(),
                phaseTimer,
                stormStrength
        );
        for (ServerPlayer player : marsLevel.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static void switchToNextPhase() {
        Phase next;
        switch (currentPhase) {
            case CALM -> next = RNG.nextFloat() < 0.75f ? Phase.RISING : Phase.PEAK;
            case RISING -> next = RNG.nextFloat() < 0.85f ? Phase.PEAK : Phase.FALLING;
            case PEAK -> next = RNG.nextFloat() < 0.7f ? Phase.FALLING : Phase.PEAK;
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
            case FALLING -> lerp(FALLING_MIN, FALLING_MAX, RNG.nextFloat());
        };
    }

    private static int getRandomDuration(Phase phase) {
        return switch (phase) {
            case CALM -> CALM_MIN_TICKS + RNG.nextInt(CALM_MAX_TICKS - CALM_MIN_TICKS + 1);
            case RISING -> RISING_MIN_TICKS + RNG.nextInt(RISING_MAX_TICKS - RISING_MIN_TICKS + 1);
            case PEAK -> PEAK_MIN_TICKS + RNG.nextInt(PEAK_MAX_TICKS - PEAK_MIN_TICKS + 1);
            case FALLING -> FALLING_MIN_TICKS + RNG.nextInt(FALLING_MAX_TICKS - FALLING_MIN_TICKS + 1);
        };
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}