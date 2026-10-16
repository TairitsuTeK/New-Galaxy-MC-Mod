package com.tairitsutek.newgc.Planets.mercury.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.mercury.network.MercurySolarFlarePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 水星太阳耀斑状态机。
 * 平静期 5~15 分钟，让耀斑成为"罕见事件"。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MercurySolarFlareManager {

    public enum Phase {
        CALM, BUILDUP, FLARE, DECAY
    }

    // ===== 各阶段持续时间（刻）=====
    // 平静：5~15 分钟
    private static final int CALM_MIN_TICKS    = 6000, CALM_MAX_TICKS    = 18000;
    // 预警：3~8 秒
    private static final int BUILDUP_MIN_TICKS = 60,   BUILDUP_MAX_TICKS = 160;
    // 爆发：2~5 秒
    private static final int FLARE_MIN_TICKS   = 40,   FLARE_MAX_TICKS   = 100;
    // 消退：5~15 秒
    private static final int DECAY_MIN_TICKS   = 100,  DECAY_MAX_TICKS   = 300;

    private static final int SYNC_INTERVAL = 5;

    private static Phase currentPhase = Phase.CALM;
    private static int phaseTimer = 0;
    private static int globalTick = 0;

    private static int syncCounter = 0;

    private static final RandomSource RNG = RandomSource.create();

    public static Phase getCurrentPhase() { return currentPhase; }
    public static int getPhaseTimer()     { return phaseTimer; }

    public static float getFlareIntensity() {
        return switch (currentPhase) {
            case CALM -> 0f;
            case BUILDUP -> {
                int total = Math.max(1, BUILDUP_MAX_TICKS);
                float progress = 1f - (float) phaseTimer / total;
                yield Math.max(0f, Math.min(1f, progress));
            }
            case FLARE -> {
                float wave = (float) Math.sin(globalTick * 0.3) * 0.15f;
                yield 1.0f + wave;
            }
            case DECAY -> {
                int total = Math.max(1, DECAY_MAX_TICKS);
                float progress = (float) phaseTimer / total;
                yield Math.max(0f, Math.min(1f, progress));
            }
        };
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel mercuryLevel = null;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) {
                mercuryLevel = level;
                break;
            }
        }
        if (mercuryLevel == null) {
            currentPhase = Phase.CALM;
            phaseTimer = 0;
            return;
        }

        globalTick++;
        phaseTimer--;

        if (phaseTimer <= 0) {
            switchToNextPhase();
        }

        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            MercurySolarFlarePayload payload = new MercurySolarFlarePayload(
                    currentPhase.ordinal(),
                    phaseTimer,
                    getFlareIntensity()
            );
            for (ServerPlayer player : mercuryLevel.players()) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private static void switchToNextPhase() {
        currentPhase = switch (currentPhase) {
            case CALM -> Phase.BUILDUP;
            case BUILDUP -> Phase.FLARE;
            case FLARE -> Phase.DECAY;
            case DECAY -> Phase.CALM;
        };
        phaseTimer = getRandomDuration(currentPhase);
    }

    private static int getRandomDuration(Phase phase) {
        return switch (phase) {
            case CALM -> CALM_MIN_TICKS + RNG.nextInt(CALM_MAX_TICKS - CALM_MIN_TICKS + 1);
            case BUILDUP -> BUILDUP_MIN_TICKS + RNG.nextInt(BUILDUP_MAX_TICKS - BUILDUP_MIN_TICKS + 1);
            case FLARE -> FLARE_MIN_TICKS + RNG.nextInt(FLARE_MAX_TICKS - FLARE_MIN_TICKS + 1);
            case DECAY -> DECAY_MIN_TICKS + RNG.nextInt(DECAY_MAX_TICKS - DECAY_MIN_TICKS + 1);
        };
    }
}