package com.tairitsutek.newgc.Moons.io.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Moons.io.network.IoQuakePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class IoQuakeManager {

    private static final RandomSource RNG = RandomSource.create();

    private static final int INTERVAL_MIN = 100;
    private static final int INTERVAL_MAX = 600;
    private static final int DURATION_MIN = 40;
    private static final int DURATION_MAX = 120;
    private static final int SYNC_INTERVAL = 5;

    private static int cooldownTimer = 0;
    private static int quakeTicks = 0;
    private static int quakeTotalTicks = 0;
    private static int syncCounter = 0;

    public static boolean isQuaking()          { return quakeTicks > 0; }
    public static int getQuakeTicks()          { return quakeTicks; }
    public static int getQuakeTotalTicks()     { return quakeTotalTicks; }
    public static int getCooldownTimer()       { return cooldownTimer; }

    public static float getQuakeIntensity() {
        if (!isQuaking() || quakeTotalTicks <= 0) return 0f;
        float progress = 1f - (float) quakeTicks / quakeTotalTicks;
        float envelope = 4f * progress * (1f - progress);
        return 0.25f + 0.75f * envelope;
    }

    public static void debugForceQuake() {
        ServerLevel ioLevel = findIoLevel();
        if (ioLevel == null) return;

        quakeTicks = DURATION_MIN + RNG.nextInt(DURATION_MAX - DURATION_MIN + 1);
        quakeTotalTicks = quakeTicks;
        cooldownTimer = 0;

        broadcastState(ioLevel);
    }

    public static void debugForceCalm() {
        ServerLevel ioLevel = findIoLevel();
        if (ioLevel == null) return;

        quakeTicks = 0;
        quakeTotalTicks = 0;
        cooldownTimer = INTERVAL_MIN + RNG.nextInt(INTERVAL_MAX - INTERVAL_MIN + 1);

        broadcastState(ioLevel);
    }

    private static ServerLevel findIoLevel() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) {
                return level;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel ioLevel = findIoLevel();
        if (ioLevel == null) {
            quakeTicks = 0;
            quakeTotalTicks = 0;
            cooldownTimer = 0;
            return;
        }

        if (quakeTicks > 0) {
            quakeTicks--;
            if (quakeTicks <= 0) {
                cooldownTimer = INTERVAL_MIN + RNG.nextInt(INTERVAL_MAX - INTERVAL_MIN + 1);
            }
        } else if (cooldownTimer > 0) {
            cooldownTimer--;
        } else {
            startQuake(ioLevel);
        }

        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            broadcastState(ioLevel);
        }
    }

    private static void startQuake(ServerLevel level) {
        if (level.players().isEmpty()) {
            cooldownTimer = 100;
            return;
        }
        quakeTicks = DURATION_MIN + RNG.nextInt(DURATION_MAX - DURATION_MIN + 1);
        quakeTotalTicks = quakeTicks;
        broadcastState(level);
    }

    private static void broadcastState(ServerLevel ioLevel) {
        boolean quaking = quakeTicks > 0;
        IoQuakePayload payload = new IoQuakePayload(
                quaking,
                quakeTicks,
                quakeTotalTicks,
                cooldownTimer
        );
        for (ServerPlayer player : ioLevel.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}