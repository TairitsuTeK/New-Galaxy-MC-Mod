package com.tairitsutek.newgc.Moons.io.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Moons.io.network.IoVolcanoPayload;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class IoVolcanoManager {

    private static final RandomSource RNG = RandomSource.create();

    // ===== ★ 增强：冷却时间大幅缩短 (3秒 ~ 15秒) =====
    private static final int INTERVAL_MIN = 60;
    private static final int INTERVAL_MAX = 300;

    // ===== ★ 增强：喷发时长大幅增加 (15秒 ~ 40秒) =====
    private static final int DURATION_MIN = 300;
    private static final int DURATION_MAX = 800;

    private static final int SEARCH_RADIUS = 48;

    // ===== ★ 增强：同步频率加快 (每 2 tick 同步一次，让震动更连贯) =====
    private static final int SYNC_INTERVAL = 2;

    private static int cooldownTimer = 0;
    private static int eruptionTicks = 0;
    private static int eruptionTotalTicks = 0;
    private static BlockPos volcanoPos = null;
    private static int syncCounter = 0;

    public static BlockPos getVolcanoPos()     { return volcanoPos; }
    public static boolean isErupting()         { return volcanoPos != null && eruptionTicks > 0; }
    public static int getEruptionTicks()       { return eruptionTicks; }
    public static int getEruptionTotalTicks()  { return eruptionTotalTicks; }
    public static int getCooldownTimer()       { return cooldownTimer; }

    public static float getEruptionIntensity() {
        if (!isErupting() || eruptionTotalTicks <= 0) return 0f;
        float progress = 1f - (float) eruptionTicks / eruptionTotalTicks;
        float envelope = 4f * progress * (1f - progress);
        // ★ 增强：基础强度从 0.25 提高到 0.4，让爆发更加剧烈
        return 0.4f + 0.6f * envelope;
    }

    public static void debugForceErupt() {
        ServerLevel ioLevel = findIoLevel();
        if (ioLevel == null || ioLevel.players().isEmpty()) return;

        ServerPlayer anchor = ioLevel.players().get(RNG.nextInt(ioLevel.players().size()));
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist = 16 + RNG.nextDouble() * SEARCH_RADIUS;

        int x = (int) (anchor.getX() + Math.cos(angle) * dist);
        int z = (int) (anchor.getZ() + Math.sin(angle) * dist);
        int y = ioLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        volcanoPos = new BlockPos(x, y, z);
        eruptionTicks = DURATION_MIN + RNG.nextInt(DURATION_MAX - DURATION_MIN + 1);
        eruptionTotalTicks = eruptionTicks;
        cooldownTimer = 0;

        broadcastState(ioLevel);
    }

    public static void debugForceCooldown() {
        ServerLevel ioLevel = findIoLevel();
        if (ioLevel == null) return;

        volcanoPos = null;
        eruptionTicks = 0;
        eruptionTotalTicks = 0;
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
            volcanoPos = null;
            eruptionTicks = 0;
            eruptionTotalTicks = 0;
            cooldownTimer = 0;
            return;
        }

        if (eruptionTicks > 0 && volcanoPos != null) {
            eruptionTicks--;
            if (eruptionTicks <= 0) {
                volcanoPos = null;
                cooldownTimer = INTERVAL_MIN + RNG.nextInt(INTERVAL_MAX - INTERVAL_MIN + 1);
            }
        } else if (cooldownTimer > 0) {
            cooldownTimer--;
        } else {
            startEruption(ioLevel);
        }

        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            broadcastState(ioLevel);
        }
    }

    private static void broadcastState(ServerLevel ioLevel) {
        boolean erupting = volcanoPos != null && eruptionTicks > 0;
        IoVolcanoPayload payload = new IoVolcanoPayload(
                erupting,
                erupting ? volcanoPos : BlockPos.ZERO,
                eruptionTicks,
                eruptionTotalTicks,
                cooldownTimer
        );
        for (ServerPlayer player : ioLevel.players()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static void startEruption(ServerLevel level) {
        if (level.players().isEmpty()) return;

        ServerPlayer anchor = level.players().get(RNG.nextInt(level.players().size()));
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist = 16 + RNG.nextDouble() * SEARCH_RADIUS;

        int x = (int) (anchor.getX() + Math.cos(angle) * dist);
        int z = (int) (anchor.getZ() + Math.sin(angle) * dist);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        volcanoPos = new BlockPos(x, y, z);
        eruptionTicks = DURATION_MIN + RNG.nextInt(DURATION_MAX - DURATION_MIN + 1);
        eruptionTotalTicks = eruptionTicks;

        broadcastState(level);
    }
}