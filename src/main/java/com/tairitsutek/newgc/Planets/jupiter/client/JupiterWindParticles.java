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

    private static final int RADIUS = 20;
    private static final int HEIGHT_MIN = 0;
    private static final int HEIGHT_MAX = 12;

    // 基础粒子数（CALM 时约 15，PEAK 时约 70，SUDDEN 时可达 150+）
    private static final int BASE_COUNT = 40;
    private static final int MAX_COUNT = 220;   // 上限拉高
    private static final int MIN_COUNT = 15;

    // 基础风速（1 刻移动方块数）
    private static final double BASE_WIND_SPEED = 1.5;

    // 风向
    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);
    private static final double WIND_JITTER = 0.08;
    private static final double VERTICAL_DRIFT = 0.04;

    // 每 2 刻生成一批
    private static final int TICK_INTERVAL = 2;

    // ===== 风暴脉冲（阵风）=====
    // 全局 tick 计数，用于生成脉冲包络
    private static long globalTick = 0;
    // 每个脉冲持续 tick 数（越爆裂越短、越频繁）
    private static double gustPhase = 0.0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        globalTick++;

        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        spawnWind(level, player);
    }

    private static void spawnWind(ClientLevel level, Player player) {
        float strength = JupiterStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 1.0f;
        strength = Math.max(0.0f, Math.min(2.5f, strength));

        // ===== 风暴等级：0.0 ~ 1.0 =====
        // 0.0 = 平静, 1.0 = 暴烈顶点
        float storm = Math.max(0f, Math.min(1f, strength / 2.5f));

        // ===== 阵风脉冲 =====
        // 脉冲频率随 storm 提高，SUDDEN 时可达 ~8Hz 的视觉效果
        double pulseFreq = 0.15 + storm * 1.2;   // 每 tick 推进量
        gustPhase += pulseFreq;
        // 主包络：0~1 正弦
        double gustEnvelope = 0.5 + 0.5 * Math.sin(gustPhase);
        // 二次小脉冲（不规则感）
        double gustEnvelope2 = 0.5 + 0.5 * Math.sin(gustPhase * 2.7 + 1.3);
        // 混合，得到 0~1 之间的包络，低 storm 时几乎平坦
        double gust = gustEnvelope * 0.7 + gustEnvelope2 * 0.3;
        gust = 1.0 - storm + storm * gust;   // storm 越低，gust 越接近 1（无脉冲感）

        // ===== 粒子数 =====
        // 平静 ≈ 15，峰值 ≈ 70，暴烈 ≈ 150~220（取决于阵风瞬时值）
        int baseCount = (int) (BASE_COUNT * (0.4f + storm * 1.2f));
        int gustBoost = (int) (storm * storm * 100 * gust);   // 暴烈时阵风额外加粒子
        int count = baseCount + gustBoost;
        count = Math.min(count, MAX_COUNT);
        count = Math.max(count, MIN_COUNT);

        // ===== 风速 =====
        // 平静 ≈ 0.75，峰值 ≈ 2.4，暴烈 ≈ 4.5~6（阵风瞬时）
        double windSpeed = BASE_WIND_SPEED
                * (0.5 + storm * 1.6)
                * (0.7 + 0.6 * gust);   // 阵风时再乘最多 1.3 倍

        // ===== 抖动 =====
        // 暴烈时粒子方向更乱
        double jitter = WIND_JITTER * (1.0 + storm * 2.5);

        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        // 暴烈时增加一点方向扰动（风向整体偏移）
        if (storm > 0.5f) {
            double wobble = Math.sin(globalTick * 0.05) * (storm - 0.5f) * 0.4;
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
            // 高度：暴烈时把粒子范围往上扩，形成"扬尘墙"
            double heightMax = HEIGHT_MAX + storm * 10;
            double dy = HEIGHT_MIN + RNG.nextDouble() * (heightMax - HEIGHT_MIN);

            double px = player.getX() + dx;
            double py = player.getY() + dy;
            double pz = player.getZ() + dz;

            if (!level.isEmptyBlock(BlockPos.containing(px, py, pz))) continue;

            // 单个粒子的速度：主方向 * 风速，加随机抖动
            double speed = windSpeed * (0.6 + RNG.nextDouble() * 0.8);

            // 水平抖动
            double vx = dirX * speed + (RNG.nextDouble() - 0.5) * jitter * speed;
            double vz = dirZ * speed + (RNG.nextDouble() - 0.5) * jitter * speed;

            // 垂直：平静时轻微上下，暴烈时被"卷起"向上
            double vy;
            if (storm < 0.4f) {
                vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;
            } else {
                // 暴烈时：一部分粒子被卷起（向上），一部分下压
                double liftBias = (storm - 0.4f) / 0.6f;   // 0~1
                double r = RNG.nextDouble();
                if (r < 0.5) {
                    vy = liftBias * (0.3 + RNG.nextDouble() * 0.5);   // 向上卷
                } else {
                    vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT * (1 + storm * 3); // 乱
                }
            }

            // 粒子类型：暴烈时烟尘比例增加
            float roll = RNG.nextFloat();
            boolean useDark;
            if (storm > 0.7f) {
                // 暴烈：更多深色烟尘，少量亮色
                useDark = roll < 0.75f;
            } else {
                useDark = roll < 0.4f;
            }

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

            // 暴烈时偶尔加沙尘粒子（原版 ASH）营造"颗粒感"
            if (storm > 0.6f && RNG.nextFloat() < 0.15f * storm) {
                level.addParticle(
                        ParticleTypes.ASH,
                        px, py, pz,
                        vx * 0.6, vy + 0.1, vz * 0.6);
            }
        }
    }
}