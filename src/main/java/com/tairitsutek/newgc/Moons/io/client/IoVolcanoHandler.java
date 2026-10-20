package com.tairitsutek.newgc.Moons.io.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class IoVolcanoHandler {

    private static final RandomSource RNG = RandomSource.create();

    // ===== ★ 增强：爆发期和稳定期的粒子数量大幅增加 =====
    private static final int BURST_PARTICLES = 120;
    private static final int STEADY_PARTICLES = 75;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;
        if (!IoVolcanoClientCache.isErupting()) return;

        BlockPos pos = IoVolcanoClientCache.getVolcanoPos();
        if (pos == null) return;

        // 距离玩家太远就不渲染（优化性能）
        if (mc.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 128 * 128) return;

        spawnEruptionParticles(level, pos);
    }

    private static void spawnEruptionParticles(ClientLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        float intensity = IoVolcanoClientCache.getEruptionIntensity();
        int total = IoVolcanoClientCache.getEruptionTotalTicks();
        int remain = IoVolcanoClientCache.getEruptionTicks();
        float progress = total > 0 ? 1f - (float) remain / total : 0f;
        boolean burstPhase = progress < 0.2f; // 前20%时间为大爆发

        // ===== 1) 主喷发柱 (直冲云霄) =====
        int columnCount = burstPhase ? BURST_PARTICLES : (int) (STEADY_PARTICLES * (0.6f + intensity * 0.8f));
        for (int i = 0; i < columnCount; i++) {
            double heightFactor = RNG.nextDouble();
            double spread = 0.5 + heightFactor * 4.0; // ★ 扩散范围加大

            // ★ 向上喷射的速度大幅提高
            double vx = (RNG.nextDouble() - 0.5) * spread;
            double vy = 2.5 + RNG.nextDouble() * 4.0 + intensity * 3.0;
            double vz = (RNG.nextDouble() - 0.5) * spread;

            double px = cx + (RNG.nextDouble() - 0.5) * 2.0;
            double py = cy + RNG.nextDouble() * 4.0;
            double pz = cz + (RNG.nextDouble() - 0.5) * 2.0;

            if (heightFactor < 0.35) {
                // 底部：岩浆与火焰
                float r = RNG.nextFloat();
                if (r < 0.4f) {
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
                } else if (r < 0.7f) {
                    level.addParticle(ParticleTypes.LAVA, px, py, pz, vx * 0.8, vy * 0.9, vz * 0.8);
                } else {
                    // 硫磺粉尘
                    level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.YELLOW_CONCRETE.defaultBlockState()), px, py, pz, vx, vy * 0.9, vz);
                }
            } else if (heightFactor < 0.75) {
                // 中部：滚滚浓烟
                float r = RNG.nextFloat();
                if (r < 0.7f) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx * 1.5, vy * 1.2, vz * 1.5);
                } else {
                    level.addParticle(ParticleTypes.ASH, px, py, pz, vx, vy * 0.8, vz);
                }
            } else {
                // 高空：巨大的蘑菇云
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, vx * 2.0, vy * 0.6, vz * 2.0);
            }
        }

        // ===== 2) 地面岩浆喷泉 (向四周抛射) =====
        int lavaCount = burstPhase ? 60 : (int) (25 * intensity);
        for (int i = 0; i < lavaCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            // ★ 抛射距离更远
            double speed = 1.5 + RNG.nextDouble() * 3.5 * intensity;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = 3.0 + RNG.nextDouble() * 3.0; // 抛得更高

            level.addParticle(ParticleTypes.LAVA, cx, cy + 1.0, cz, vx, vy, vz);
        }

        // ===== 3) 地面冲击波 (硫磺尘埃) =====
        int ringCount = (int) (40 * intensity) + (burstPhase ? 40 : 0);
        for (int i = 0; i < ringCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double r = 2.0 + RNG.nextDouble() * 8.0; // ★ 波及范围翻倍
            double px = cx + Math.cos(angle) * r;
            double pz = cz + Math.sin(angle) * r;
            double py = cy + 0.1;

            double vx = Math.cos(angle) * (1.0 + intensity);
            double vz = Math.sin(angle) * (1.0 + intensity);
            double vy = 0.5 + RNG.nextDouble() * 1.5;

            float r2 = RNG.nextFloat();
            if (r2 < 0.5f) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.YELLOW_CONCRETE.defaultBlockState()), px, py, pz, vx, vy, vz);
            } else if (r2 < 0.8f) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BLACK_CONCRETE.defaultBlockState()), px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx * 1.5, vy, vz * 1.5);
            }
        }

        // ===== 4) 偶尔的极光/闪电感 (核心闪光) =====
        if (RNG.nextFloat() < 0.3f) {
            level.addParticle(ParticleTypes.END_ROD, cx, cy + 2.0 + RNG.nextDouble() * 4.0, cz, 0, 0.2, 0);
        }
        if (RNG.nextFloat() < 0.2f) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    cx + (RNG.nextDouble() - 0.5) * 1.5,
                    cy + 0.5 + RNG.nextDouble() * 2.0,
                    cz + (RNG.nextDouble() - 0.5) * 1.5,
                    0, 0.4, 0);
        }
    }
}