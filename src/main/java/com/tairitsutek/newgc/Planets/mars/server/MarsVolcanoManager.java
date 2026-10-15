package com.tairitsutek.newgc.Planets.mars.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.mars.network.MarsVolcanoPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MarsVolcanoManager {

    private static final RandomSource RNG = RandomSource.create();

    private static final int INTERVAL_MIN = 400;
    private static final int INTERVAL_MAX = 1800;
    private static final int DURATION_MIN = 100;
    private static final int DURATION_MAX = 280;

    private static final int SEARCH_RADIUS = 48;
    private static final int SYNC_INTERVAL = 5;

    // 服务端状态
    private static int cooldownTimer = 0;
    private static int eruptionTicks = 0;
    private static int eruptionTotalTicks = 0;
    private static BlockPos volcanoPos = null;
    private static int syncCounter = 0;

    // ===== 供伤害处理器读取 =====
    public static BlockPos getVolcanoPos()     { return volcanoPos; }
    public static boolean isErupting()         { return volcanoPos != null && eruptionTicks > 0; }
    public static int getEruptionTicks()       { return eruptionTicks; }
    public static int getEruptionTotalTicks()  { return eruptionTotalTicks; }
    public static int getCooldownTimer()       { return cooldownTimer; }

    public static float getEruptionIntensity() {
        if (!isErupting() || eruptionTotalTicks <= 0) return 0f;
        float progress = 1f - (float) eruptionTicks / eruptionTotalTicks;
        float envelope = 4f * progress * (1f - progress);
        return 0.25f + 0.75f * envelope;
    }

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
            volcanoPos = null;
            eruptionTicks = 0;
            eruptionTotalTicks = 0;
            cooldownTimer = 0;
            return;
        }

        // ---- 状态推进 ----
        if (eruptionTicks > 0 && volcanoPos != null) {
            eruptionTicks--;
            if (eruptionTicks <= 0) {
                volcanoPos = null;
                cooldownTimer = INTERVAL_MIN + RNG.nextInt(INTERVAL_MAX - INTERVAL_MIN + 1);
            }
        } else if (cooldownTimer > 0) {
            cooldownTimer--;
        } else {
            startEruption(marsLevel);
        }

        // ---- 定期同步 ----
        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            broadcastState(marsLevel);
        }
    }

    private static void broadcastState(ServerLevel marsLevel) {
        MarsVolcanoPayload payload = new MarsVolcanoPayload(
                volcanoPos,
                eruptionTicks,
                eruptionTotalTicks,
                cooldownTimer
        );
        for (ServerPlayer player : marsLevel.players()) {
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

        // 立即广播一次，避免客户端等 5 刻
        broadcastState(level);
    }
}