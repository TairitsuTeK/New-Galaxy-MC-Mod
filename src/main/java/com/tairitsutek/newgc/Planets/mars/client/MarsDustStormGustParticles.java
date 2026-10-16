package com.tairitsutek.newgc.Planets.mars.client;

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
 * 火星沙尘暴"强风"粒子。
 * 对标 JupiterStormGustParticles，但：
 *  - 颜色改黄褐（ORANGE / YELLOW / BROWN 混合）
 *  - 风向角度不同
 *  - 阈值适配火星沙尘暴强度范围（0~1.5）
 *  - 只在沙尘暴 ≥ 0.8 时激活
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsDustStormGustParticles {

    private static final RandomSource RNG = RandomSource.create();

    // 激活阈值：火星沙尘暴最大强度 1.5
    private static final float THRESHOLD = 0.8f;

    // 生成范围
    private static final int RADIUS = 26;
    private static final int HEIGHT_MIN = -2;
    private static final int HEIGHT_MAX = 20;

    // 粒子数
    private static final int BASE_COUNT = 30;
    private static final int MAX_COUNT = 260;
    private static final int MIN_COUNT = 10;

    // 横向速度（比木星稍慢一点，火星沙尘暴更"闷"）
    private static final double BASE_SPEED = 1.8;
    private static final double SPEED_BOOST = 1.4;

    // 风向：火星沙尘暴的风向和 JupiterWindParticles 不同
    // 木星是 35°，火星取 150°（沙尘暴从另一侧刮来）
    private static final float WIND_ANGLE = (float) Math.toRadians(150.0f);
    private static final double JITTER = 0.22;

    // 垂直漂移
    private static final double VERTICAL_DRIFT = 0.03;

    // 生成间隔
    private static final int TICK_INTERVAL = 1;

    // 阵风脉冲状态
    private static long globalTick = 0;
    private static double gustPhase = 0.0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        globalTick++;

        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        spawnGust(level, player);
    }

    private static void spawnGust(ClientLevel level, Player player) {
        float strength = MarsDustStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(1.5f, strength));

        // 阈值判断
        if (strength < THRESHOLD) return;

        // 归一化：0.8 → 0，1.5 → 1
        float t = (strength - THRESHOLD) / (1.5f - THRESHOLD);
        t = Math.max(0f, Math.min(1f, t));

        // 阵风脉冲
        double pulseFreq = 0.25 + t * 1.5;
        gustPhase += pulseFreq;
        double gustEnvelope = 0.5 + 0.5 * Math.sin(gustPhase);
        double gustEnvelope2 = 0.5 + 0.5 * Math.sin(gustPhase * 2.7 + 1.3);
        double gust = gustEnvelope * 0.7 + gustEnvelope2 * 0.3;
        gust = 0.5 + 0.5 * t + gust * t * 0.8;

        // 粒子数
        int baseCount = (int) (BASE_COUNT + (MAX_COUNT - BASE_COUNT) * t);
        int gustBoost = (int) (t * t * 100 * gustEnvelope);
        int count = baseCount + gustBoost;
        count = Math.min(count, MAX_COUNT);
        count = Math.max(count, MIN_COUNT);

        // 风速
        double speed = BASE_SPEED * (0.6 + t * 0.8) * (1 + gust * SPEED_BOOST * 0.3);

        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        // 暴烈时风向缓慢摆动
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

            double vx = dirX * speed * (0.7 + RNG.nextDouble() * 0.6)
                    + (RNG.nextDouble() - 0.5) * JITTER * speed;
            double vz = dirZ * speed * (0.7 + RNG.nextDouble() * 0.6)
                    + (RNG.nextDouble() - 0.5) * JITTER * speed;

            double vy;
            if (t > 0.5f && RNG.nextFloat() < 0.3f) {
                vy = (t - 0.5f) * 0.6 + RNG.nextDouble() * 0.3;
            } else {
                vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;
            }

            // 火星沙尘暴配色：橙 / 黄 / 棕 三种混合
            float roll = RNG.nextFloat();
            if (roll < 0.45f) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.ORANGE_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else if (roll < 0.75f) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.YELLOW_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.BROWN_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            }

            // 高强度时加 ASH
            if (t > 0.4f && RNG.nextFloat() < 0.12f * t) {
                level.addParticle(ParticleTypes.ASH,
                        px, py, pz,
                        vx * 0.7, vy + 0.05, vz * 0.7);
            }
        }
    }
}