package com.tairitsutek.newgc.Planets.mars.client;

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
public class MarsVolcanoHandler {

    private static final RandomSource RNG = RandomSource.create();

    // 爆发期 / 稳定期每刻粒子数
    private static final int BURST_PARTICLES = 60;
    private static final int STEADY_PARTICLES = 35;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        if (!MarsVolcanoClientCache.isErupting()) return;

        BlockPos pos = MarsVolcanoClientCache.getVolcanoPos();
        if (pos == null) return;

        spawnEruptionParticles(level, pos);
    }

    private static void spawnEruptionParticles(ClientLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        float intensity = MarsVolcanoClientCache.getEruptionIntensity();
        int total = MarsVolcanoClientCache.getEruptionTotalTicks();
        int remain = MarsVolcanoClientCache.getEruptionTicks();
        float progress = total > 0 ? 1f - (float) remain / total : 0f;
        boolean burstPhase = progress < 0.15f;

        // ===== 1) 主喷发柱 =====
        int columnCount = burstPhase
                ? BURST_PARTICLES
                : (int) (STEADY_PARTICLES * (0.5f + intensity));

        for (int i = 0; i < columnCount; i++) {
            double heightFactor = RNG.nextDouble();
            double spread = 0.4 + heightFactor * 2.5;

            double vx = (RNG.nextDouble() - 0.5) * spread;
            double vy = 1.5 + RNG.nextDouble() * 2.5 + intensity * 1.5;
            double vz = (RNG.nextDouble() - 0.5) * spread;

            double px = cx + (RNG.nextDouble() - 0.5) * 1.0;
            double py = cy + RNG.nextDouble() * 2.5;
            double pz = cz + (RNG.nextDouble() - 0.5) * 1.0;

            if (heightFactor < 0.3) {
                float r = RNG.nextFloat();
                if (r < 0.5f) {
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
                } else if (r < 0.8f) {
                    level.addParticle(ParticleTypes.LAVA, px, py, pz, vx * 0.6, vy * 0.7, vz * 0.6);
                } else {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx, vy * 0.8, vz);
                }
            } else if (heightFactor < 0.7) {
                float r = RNG.nextFloat();
                if (r < 0.6f) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx * 1.2, vy * 0.9, vz * 1.2);
                } else if (r < 0.85f) {
                    level.addParticle(ParticleTypes.ASH, px, py, pz, vx, vy * 0.6, vz);
                } else {
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, vx * 0.5, vy * 0.5, vz * 0.5);
                }
            } else {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz,
                        vx * 1.5, vy * 0.4, vz * 1.5);
            }
        }

        // ===== 2) 熔岩喷泉 =====
        int lavaCount = burstPhase ? 25 : (int) (8 * intensity);
        for (int i = 0; i < lavaCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double speed = 0.6 + RNG.nextDouble() * 1.8 * intensity;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = 2.0 + RNG.nextDouble() * 2.0;

            level.addParticle(ParticleTypes.LAVA, cx, cy + 1.0, cz, vx, vy, vz);
        }

        // ===== 3) 地面冲击波 =====
        int ringCount = (int) (20 * intensity) + (burstPhase ? 20 : 0);
        for (int i = 0; i < ringCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double r = 1.0 + RNG.nextDouble() * 4.0;
            double px = cx + Math.cos(angle) * r;
            double pz = cz + Math.sin(angle) * r;
            double py = cy + 0.1;

            double vx = Math.cos(angle) * (0.5 + intensity);
            double vz = Math.sin(angle) * (0.5 + intensity);
            double vy = 0.3 + RNG.nextDouble() * 0.6;

            float r2 = RNG.nextFloat();
            if (r2 < 0.5f) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.ORANGE_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else if (r2 < 0.8f) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.BLACK_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz,
                        vx * 1.5, vy, vz * 1.5);
            }
        }

        // ===== 4) 核心闪光 =====
        int coreCount = burstPhase ? 15 : (int) (5 * intensity);
        for (int i = 0; i < coreCount; i++) {
            level.addParticle(ParticleTypes.FLAME,
                    cx + (RNG.nextDouble() - 0.5) * 1.5,
                    cy + RNG.nextDouble() * 2.0,
                    cz + (RNG.nextDouble() - 0.5) * 1.5,
                    (RNG.nextDouble() - 0.5) * 0.4,
                    0.3 + RNG.nextDouble() * 0.6,
                    (RNG.nextDouble() - 0.5) * 0.4);
        }
        if (burstPhase || RNG.nextFloat() < 0.4f) {
            level.addParticle(ParticleTypes.END_ROD, cx, cy + 1.0 + RNG.nextDouble() * 1.5, cz, 0, 0.1, 0);
        }
        if (burstPhase || RNG.nextFloat() < 0.25f) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    cx + (RNG.nextDouble() - 0.5) * 0.8,
                    cy + 0.5 + RNG.nextDouble() * 1.2,
                    cz + (RNG.nextDouble() - 0.5) * 0.8,
                    0, 0.2, 0);
        }

        // ===== 5) 高空烟柱 =====
        int plumeCount = burstPhase ? 12 : (int) (4 * intensity);
        for (int i = 0; i < plumeCount; i++) {
            double topY = cy + 6.0 + RNG.nextDouble() * 6.0;
            double spread = 2.0 + RNG.nextDouble() * 3.0;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx + (RNG.nextDouble() - 0.5) * spread,
                    topY,
                    cz + (RNG.nextDouble() - 0.5) * spread,
                    (RNG.nextDouble() - 0.5) * 0.3,
                    0.05,
                    (RNG.nextDouble() - 0.5) * 0.3);
        }
    }
}