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
    private static final int BURST_PARTICLES = 60;
    private static final int STEADY_PARTICLES = 35;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;
        if (!IoVolcanoClientCache.isErupting()) return;

        BlockPos pos = IoVolcanoClientCache.getVolcanoPos();
        if (pos == null) return;

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
        boolean burstPhase = progress < 0.15f;

        // 1) 主喷发柱 (火焰 + 硫磺烟尘)
        int columnCount = burstPhase ? BURST_PARTICLES : (int) (STEADY_PARTICLES * (0.5f + intensity));
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
                    // 硫磺色烟雾
                    level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.YELLOW_CONCRETE.defaultBlockState()), px, py, pz, vx, vy * 0.8, vz);
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
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, vx * 1.5, vy * 0.4, vz * 1.5);
            }
        }

        // 2) 地面冲击波 (硫磺粉尘)
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
            if (r2 < 0.6f) {
                // 大量硫磺黄
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.YELLOW_CONCRETE.defaultBlockState()), px, py, pz, vx, vy, vz);
            } else if (r2 < 0.9f) {
                // 黑曜石/黑尘
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BLACK_CONCRETE.defaultBlockState()), px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx * 1.5, vy, vz * 1.5);
            }
        }
    }
}