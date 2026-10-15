package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class JupiterWindParticles {

    private static final RandomSource RNG = RandomSource.create();

    // ===== 范围 =====
    private static final int RADIUS = 36;
    private static final int HEIGHT_MIN = -10;
    private static final int HEIGHT_MAX = 28;

    // ===== 密度 =====
    private static final int COUNT = 100;

    // ===== 风速 =====
    private static final double WIND_SPEED = 0.8;

    // ===== 风向（水平角，单位：弧度）=====
    // 0 = +X 方向，PI/2 = +Z 方向
    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);

    // ===== 风向随机抖动 =====
    private static final double WIND_JITTER = 0.08;

    // ===== 垂直分量（稍微上下飘）=====
    private static final double VERTICAL_DRIFT = 0.04;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        spawnWind(level, player);
    }

    private static void spawnWind(ClientLevel level, Player player) {
        // 风向的单位向量
        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        for (int i = 0; i < COUNT; i++) {
            // 在玩家周围随机位置生成
            double dx = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dz = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dy = HEIGHT_MIN + RNG.nextDouble() * (HEIGHT_MAX - HEIGHT_MIN);

            double px = player.getX() + dx;
            double py = player.getY() + dy;
            double pz = player.getZ() + dz;

            // 只在空气里生成
            if (!level.isEmptyBlock(BlockPos.containing(px, py, pz))) continue;

            // 速度：主风向 + 抖动
            double speed = WIND_SPEED * (0.6 + RNG.nextDouble() * 0.8);
            double vx = dirX * speed + (RNG.nextDouble() - 0.5) * WIND_JITTER;
            double vz = dirZ * speed + (RNG.nextDouble() - 0.5) * WIND_JITTER;
            double vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;

            // 橙 / 黑 两色
            float roll = RNG.nextFloat();
            if (roll < 0.6f) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.ORANGE_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.BLACK_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            }

            // 偶尔加一条"风线"（普通烟雾拖尾）
            if (RNG.nextFloat() < 0.15f) {
                level.addParticle(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        px, py, pz,
                        vx * 1.2, vy * 0.5, vz * 1.2);
            }
        }
    }
}