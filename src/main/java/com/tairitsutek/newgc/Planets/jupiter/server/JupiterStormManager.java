package com.tairitsutek.newgc.Planets.jupiter.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.network.JupiterStormPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterStormManager {

    public enum Phase {
        CALM, RISING, PEAK, SUDDEN, FALLING
    }

    // ===== 各阶段强度范围 =====
    private static final float CALM_MIN = 0.0f,   CALM_MAX = 0.15f;
    private static final float RISING_MIN = 0.2f, RISING_MAX = 0.7f;
    private static final float PEAK_MIN = 1.0f,   PEAK_MAX = 1.5f;
    private static final float SUDDEN_MIN = 2.0f, SUDDEN_MAX = 3.0f;
    private static final float FALLING_MIN = 0.3f, FALLING_MAX = 0.8f;

    // ===== 对外暴露的峰值常量（供 JupiterThunderHandler 归一化使用） =====
    /** PEAK 阶段理论上限，也是“有效闪电强度”的参考峰值 */
    public static final float PEAK_REFERENCE = PEAK_MAX;
    /** SUDDEN 阶段理论上限 */
    public static final float SUDDEN_REFERENCE = SUDDEN_MAX;

    // ===== 各阶段持续时间（tick） =====
    private static final int CALM_MIN_TICKS = 300, CALM_MAX_TICKS = 600;
    private static final int RISING_MIN_TICKS = 200, RISING_MAX_TICKS = 400;
    private static final int PEAK_MIN_TICKS = 300, PEAK_MAX_TICKS = 600;
    private static final int SUDDEN_MIN_TICKS = 600, SUDDEN_MAX_TICKS = 1200;
    private static final int FALLING_MIN_TICKS = 300, FALLING_MAX_TICKS = 600;

    // ===== 平滑与波动参数 =====
    private static final float SMOOTH = 0.20f;
    /** 阶段切换时直接 snap 到新目标的比例（0.7 = 一步到位 70%，剩余靠 SMOOTH 补） */
    private static final float PHASE_SNAP = 0.7f;
    /** 阶段内 baseTarget 重随机间隔（tick），让阶段内也有起伏 */
    private static final int BASE_TARGET_REROLL_INTERVAL = 100;
    /** 叠加在 baseTarget 上的正弦波动幅度与周期 */
    private static final float WAVE_AMPLITUDE = 0.1f;
    private static final float WAVE_PERIOD = 0.01f;

    /** 网络同步间隔（tick） */
    private static final int SYNC_INTERVAL = 10;

    private static Phase currentPhase = Phase.CALM;
    private static int phaseTimer = 0;
    private static int globalTick = 0;
    private static float baseTarget = CALM_MIN;
    private static float targetStrength = CALM_MIN;
    private static float stormStrength = CALM_MIN;

    private static int syncCounter = 0;

    private static final RandomSource RNG = RandomSource.create();

    // ===== 强制劈雷调试开关 =====
    private static boolean forceThunder = false;

    public static boolean isForceThunder() { return forceThunder; }
    public static void setForceThunder(boolean value) { forceThunder = value; }

    public static float getStormStrength() { return stormStrength; }
    public static Phase getCurrentPhase()  { return currentPhase; }
    public static int getPhaseTimer()      { return phaseTimer; }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel jupiterLevel = null;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) {
                jupiterLevel = level;
                break;
            }
        }

        if (jupiterLevel == null) {
            stormStrength = CALM_MIN;
            targetStrength = CALM_MIN;
            baseTarget = CALM_MIN;
            currentPhase = Phase.CALM;
            phaseTimer = 0;
            return;
        }

        globalTick++;

        // ===== 阶段推进 =====
        phaseTimer--;
        if (phaseTimer <= 0) {
            switchToNextPhase();
            baseTarget = getRandomStrengthForPhase(currentPhase);
            // 阶段突变：一步到位 snap 到新目标的 70%，剩余 30% 交给 SMOOTH
            stormStrength += (baseTarget - stormStrength) * PHASE_SNAP;
        } else if (globalTick % BASE_TARGET_REROLL_INTERVAL == 0) {
            // 阶段内每 100 tick 重随机一次 baseTarget，让阶段内也有起伏
            baseTarget = getRandomStrengthForPhase(currentPhase);
        }

        // ===== 正弦波动 + 平滑 =====
        float wave = (float) Math.sin(globalTick * WAVE_PERIOD) * WAVE_AMPLITUDE;
        targetStrength = baseTarget + wave;
        stormStrength += (targetStrength - stormStrength) * SMOOTH;

        // ===== 安全钳制 =====
        if (Float.isNaN(stormStrength) || Float.isInfinite(stormStrength)) {
            stormStrength = CALM_MIN;
        }
        stormStrength = Math.max(0.0f, Math.min(SUDDEN_MAX, stormStrength));

        // ===== 调试日志（每 20 tick 一次） =====
        if (globalTick % 20 == 0) {
            System.out.println("[Storm] phase=" + currentPhase
                    + " strength=" + String.format("%.2f", stormStrength)
                    + " target=" + String.format("%.2f", targetStrength));
        }

        // ===== 网络同步 =====
        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            JupiterStormPayload payload = new JupiterStormPayload(
                    currentPhase.ordinal(),
                    phaseTimer,
                    stormStrength
            );
            for (ServerPlayer player : jupiterLevel.players()) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private static void switchToNextPhase() {
        Phase next;
        switch (currentPhase) {
            case CALM -> {
                float r = RNG.nextFloat();
                if (r < 0.4f)      next = Phase.SUDDEN;
                else if (r < 0.9f) next = Phase.RISING;
                else               next = Phase.PEAK;
            }
            case RISING -> next = RNG.nextFloat() < 0.9f ? Phase.PEAK : Phase.SUDDEN;
            case PEAK -> next = RNG.nextFloat() < 0.7f ? Phase.SUDDEN : Phase.FALLING;
            case SUDDEN -> next = RNG.nextFloat() < 0.4f ? Phase.PEAK : Phase.FALLING;
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