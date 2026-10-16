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

    // ===== 生成范围（略小，降低不必要计算）=====
    private static final int RADIUS = 28;
    private static final int HEIGHT_MIN = -6;
    private static final int HEIGHT_MAX = 20;

    // ===== 激活阈值：强度低于此不生成任何粒子 =====
    private static final float THRESHOLD = 0.25f;

    // ===== 粒子数：从阈值到 2.5 线性放大 =====
    private static final int MIN_COUNT = 8;      // 刚激活时
    private static final int MAX_COUNT = 80;     // 硬上限（原 150，砍半）

    // ===== 风速：随强度线性 =====
    private static final double BASE_SPEED = 1.2;
    private static final double MAX_SPEED = 6.0;

    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);
    private static final double WIND_JITTER = 0.10;
    private static final double VERTICAL_DRIFT = 0.04;

    // ===== 生成间隔（刻）：每 2 刻生成一次，降低 CPU =====
    private static final int TICK_INTERVAL = 2;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        // 每 2 刻生成一次（tickCount 累加）
        if (player.tickCount % TICK_INTERVAL != 0) return;

        spawnWind(level, player);
    }

    private static void spawnWind(ClientLevel level, Player player) {
        float strength = JupiterStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(2.5f, strength));

        // ===== 阈值判断：平静期直接不生成 =====
        if (strength < THRESHOLD) return;

        // ===== 归一化：0 = 阈值，1 = 2.5 =====
        float t = (strength - THRESHOLD) / (2.5f - THRESHOLD);
        t = Math.max(0f, Math.min(1f, t));

        // ===== 粒子数：线性 =====
        int count = (int) (MIN_COUNT + (MAX_COUNT - MIN_COUNT) * t);
        count = Math.min(count, MAX_COUNT);
        count = Math.max(count, MIN_COUNT);

        // ===== 风速：线性 =====
        double windSpeed = BASE_SPEED + (MAX_SPEED - BASE_SPEED) * t;

        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);
        double perpX = -dirZ;
        double perpZ = dirX;

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        for (int i = 0; i < count; i++) {
            // 生成区域：偏上风侧
            double alongWind = -RADIUS + RNG.nextDouble() * (RADIUS * 1.4);
            double perpendicular = (RNG.nextDouble() - 0.5) * RADIUS * 2;

            double px = playerX + dirX * alongWind + perpX * perpendicular;
            double pz = playerZ + dirZ * alongWind + perpZ * perpendicular;
            double py = playerY + HEIGHT_MIN + RNG.nextDouble() * (HEIGHT_MAX - HEIGHT_MIN);

            if (!level.isEmptyBlock(BlockPos.containing(px, py, pz))) continue;

            double speed = windSpeed * (0.75 + RNG.nextDouble() * 0.5);
            double vx = dirX * speed + (RNG.nextDouble() - 0.5) * WIND_JITTER;
            double vz = dirZ * speed + (RNG.nextDouble() - 0.5) * WIND_JITTER;
            double vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;

            // ===== 主粒子：简洁配比，减少类型切换 =====
            float roll = RNG.nextFloat();
            if (roll < 0.5f) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.ORANGE_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else if (roll < 0.8f) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.BLACK_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else {
                // 少量 LARGE_SMOKE，增加"雾"感但不占太多
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        px, py, pz,
                        vx * 0.9, vy, vz * 0.9);
            }

            // ===== 火花：只在强度较高时生成，概率随 t 增长 =====
            if (t > 0.3f && RNG.nextFloat() < 0.05f * t) {
                double sparkSpeed = speed * 1.6;
                level.addParticle(
                        ParticleTypes.FLAME,
                        px, py, pz,
                        dirX * sparkSpeed, (RNG.nextDouble() - 0.5) * 0.15,
                        dirZ * sparkSpeed);
            }

            // ===== 闪电微光：只在接近暴烈时生成 =====
            if (t > 0.6f && RNG.nextFloat() < 0.02f * t) {
                double glowSpeed = speed * 2.2;
                level.addParticle(
                        ParticleTypes.END_ROD,
                        px, py, pz,
                        dirX * glowSpeed, (RNG.nextDouble() - 0.5) * 0.2,
                        dirZ * glowSpeed);
            }
        }
    }
}