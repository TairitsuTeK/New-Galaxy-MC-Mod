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

/**
 * 木星风暴"强风"粒子。
 * 只在风暴强度 ≥ THRESHOLD 时激活，强度越高越猛。
 * 与 JupiterWindParticles 不同：
 *  - 横向速度极快，粒子几乎"横着刮过去"
 *  - 粒子数量指数级增长
 *  - 有"阵风"脉冲
 *  - 只在高强度时激活，避免平静期浪费性能
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class JupiterStormGustParticles {

    private static final RandomSource RNG = RandomSource.create();

    // 激活阈值：低于这个强度不生成任何粒子
    private static final float THRESHOLD = 1.2f;

    // 生成范围
    private static final int RADIUS = 26;
    private static final int HEIGHT_MIN = -2;
    private static final int HEIGHT_MAX = 20;

    // 粒子数：从阈值开始到 2.5 之间，线性放大
    private static final int BASE_COUNT = 30;
    private static final int MAX_COUNT = 260;
    private static final int MIN_COUNT = 10;

    // 横向速度
    private static final double BASE_SPEED = 2.2;      // 非常快
    private static final double SPEED_BOOST = 1.6;     // 阵风额外加速

    // 风向
    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);
    private static final double JITTER = 0.22;         // 方向抖动

    // 垂直漂移（基本水平，只有一点点上下）
    private static final double VERTICAL_DRIFT = 0.03;

    // 生成间隔（刻）
    private static final int TICK_INTERVAL = 1;        // 每刻生成，非常密

    // 阵风脉冲状态
    private static long globalTick = 0;
    private static double gustPhase = 0.0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        globalTick++;

        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        spawnGust(level, player);
    }

    private static void spawnGust(ClientLevel level, Player player) {
        float strength = JupiterStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(2.5f, strength));

        // ===== 阈值判断：低于阈值不生成 =====
        if (strength < THRESHOLD) return;

        // ===== 归一化：阈值 → 0，2.5 → 1 =====
        float t = (strength - THRESHOLD) / (2.5f - THRESHOLD);
        t = Math.max(0f, Math.min(1f, t));

        // ===== 阵风脉冲：t 越高频率越快 =====
        double pulseFreq = 0.25 + t * 1.5;
        gustPhase += pulseFreq;
        double gustEnvelope = 0.5 + 0.5 * Math.sin(gustPhase);
        double gustEnvelope2 = 0.5 + 0.5 * Math.sin(gustPhase * 2.7 + 1.3);
        double gust = gustEnvelope * 0.7 + gustEnvelope2 * 0.3;
        // t 越低，脉冲越弱（保持最小风感）
        gust = 0.5 + 0.5 * t + (1 - t) * 0.0 + gust * t * 0.8;

        // ===== 粒子数 =====
        int baseCount = (int) (BASE_COUNT + (MAX_COUNT - BASE_COUNT) * t);
        // 阵风时额外加
        int gustBoost = (int) (t * t * 100 * gustEnvelope);
        int count = baseCount + gustBoost;
        count = Math.min(count, MAX_COUNT);
        count = Math.max(count, MIN_COUNT);

        // ===== 风速 =====
        double speed = BASE_SPEED * (0.6 + t * 0.8) * (1 + gust * SPEED_BOOST * 0.3);

        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        // 暴烈时风向缓慢摆动（形成"沙尘墙翻滚"）
        if (t > 0.3f) {
            double wobble = Math.sin(globalTick * 0.07) * (t - 0.3f) * 0.5;
            double cos = Math.cos(wobble);
            double sin = Math.sin(wobble);
            double nx = dirX * cos - dirZ * sin;
            double nz = dirX * sin + dirZ * cos;
            dirX = nx;
            dirZ = nz;
        }

        for (int i = 0; i < count; i++) {
            double dx = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dz = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dy = HEIGHT_MIN + RNG.nextDouble() * (HEIGHT_MAX - HEIGHT_MIN);

            double px = player.getX() + dx;
            double py = player.getY() + dy;
            double pz = player.getZ() + dz;

            if (!level.isEmptyBlock(BlockPos.containing(px, py, pz))) continue;

            // 横向速度
            double vx = dirX * speed * (0.7 + RNG.nextDouble() * 0.6)
                    + (RNG.nextDouble() - 0.5) * JITTER * speed;
            double vz = dirZ * speed * (0.7 + RNG.nextDouble() * 0.6)
                    + (RNG.nextDouble() - 0.5) * JITTER * speed;

            // 垂直速度：高 t 时粒子被"卷起"
            double vy;
            if (t > 0.5f && RNG.nextFloat() < 0.3f) {
                vy = (t - 0.5f) * 0.6 + RNG.nextDouble() * 0.3;
            } else {
                vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;
            }

            // 颜色：暴烈时更多深色
            float roll = RNG.nextFloat();
            boolean useDark = t > 0.5f ? roll < 0.7f : roll < 0.4f;

            if (!useDark) {
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

            // 偶尔加 ASH 增加颗粒感
            if (t > 0.4f && RNG.nextFloat() < 0.12f * t) {
                level.addParticle(ParticleTypes.ASH,
                        px, py, pz,
                        vx * 0.7, vy + 0.05, vz * 0.7);
            }
        }
    }
}