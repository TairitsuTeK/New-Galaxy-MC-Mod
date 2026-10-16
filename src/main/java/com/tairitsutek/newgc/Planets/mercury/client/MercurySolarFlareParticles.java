package com.tairitsutek.newgc.Planets.mercury.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 水星太阳耀斑粒子：
 * - 白色/淡蓝粒子从天空竖直落下
 * - 模拟"太阳风粒子流"
 * - 只在 BUILDUP 后期 / FLARE / DECAY 前段生成
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MercurySolarFlareParticles {

    private static final RandomSource RNG = RandomSource.create();

    private static final int RADIUS = 32;
    private static final int HEIGHT_ABOVE = 24;      // 玩家上方生成高度

    private static final int BASE_COUNT = 15;
    private static final int MAX_COUNT = 80;

    // 向下速度：不是自由落体，是"雨"的感觉
    private static final double FALL_SPEED = 1.2;
    private static final double HORIZONTAL_JITTER = 0.05;

    private static final int TICK_INTERVAL = 2;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        spawnFlare(level, player);
    }

    private static void spawnFlare(ClientLevel level, Player player) {
        float intensity = MercurySolarFlareClientCache.getIntensity();
        if (Float.isNaN(intensity) || Float.isInfinite(intensity)) intensity = 0f;
        intensity = Math.max(0f, Math.min(1.5f, intensity));

        // 强度低于 0.3 不生成
        if (intensity < 0.3f) return;

        float t = (intensity - 0.3f) / 1.2f;
        t = Math.max(0f, Math.min(1f, t));

        int count = (int) (BASE_COUNT + (MAX_COUNT - BASE_COUNT) * t);
        count = Math.min(count, MAX_COUNT);

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        for (int i = 0; i < count; i++) {
            double dx = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dz = (RNG.nextDouble() - 0.5) * RADIUS * 2;

            double px = playerX + dx;
            double pz = playerZ + dz;

            // 在地表上方生成，高度随机
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) px, (int) pz);
            double py = Math.max(playerY + HEIGHT_ABOVE, groundY + 8);

            // 竖直向下 + 小抖动
            double vx = (RNG.nextDouble() - 0.5) * HORIZONTAL_JITTER;
            double vz = (RNG.nextDouble() - 0.5) * HORIZONTAL_JITTER;
            double vy = -FALL_SPEED * (0.8 + RNG.nextDouble() * 0.4);

            // 颜色：白色 + 淡蓝
            float roll = RNG.nextFloat();
            if (roll < 0.7f) {
                level.addParticle(ParticleTypes.END_ROD, px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
            }

            // 偶尔加个 ASH 做颗粒感
            if (RNG.nextFloat() < 0.1f) {
                level.addParticle(ParticleTypes.ASH, px, py, pz, vx * 2, vy * 0.5, vz * 2);
            }
        }
    }
}