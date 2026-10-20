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
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class IoVolcanoHandler {

    private static final RandomSource RNG = RandomSource.create();

    private static final int BURST_PARTICLES = 80;
    private static final int STEADY_PARTICLES = 25;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;
        if (!IoVolcanoClientCache.isErupting()) return;

        BlockPos pos = IoVolcanoClientCache.getVolcanoPos();
        if (pos == null) return;

        Vec3 playerPos = mc.player.position();
        double distSqr = playerPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // 距离剔除：96格外不生成
        if (distSqr > 96 * 96) {
            return;
        }

        float distanceMultiplier = 1.0f;
        if (distSqr > 48 * 48) {
            distanceMultiplier = (float) (1.0 - (Math.sqrt(distSqr) - 48) / 48.0);
            distanceMultiplier = Math.max(0.1f, distanceMultiplier);
        }

        spawnEruptionParticles(level, pos, distanceMultiplier);
    }

    private static void spawnEruptionParticles(ClientLevel level, BlockPos pos, float distanceMul) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        float intensity = IoVolcanoClientCache.getEruptionIntensity();
        int total = IoVolcanoClientCache.getEruptionTotalTicks();
        int remain = IoVolcanoClientCache.getEruptionTicks();
        float progress = total > 0 ? 1f - (float) remain / total : 0f;
        boolean burstPhase = progress < 0.2f;

        int baseColumn = burstPhase ? BURST_PARTICLES : STEADY_PARTICLES;
        int columnCount = (int) (baseColumn * distanceMul * (0.6f + intensity * 0.6f));

        for (int i = 0; i < columnCount; i++) {
            double heightFactor = RNG.nextDouble();
            double spread = 0.5 + heightFactor * 3.5;

            double vx = (RNG.nextDouble() - 0.5) * spread;
            double vy = 2.0 + RNG.nextDouble() * 3.5 + intensity * 2.0;
            double vz = (RNG.nextDouble() - 0.5) * spread;

            double px = cx + (RNG.nextDouble() - 0.5) * 1.5;
            double py = cy + RNG.nextDouble() * 3.0;
            double pz = cz + (RNG.nextDouble() - 0.5) * 1.5;

            if (heightFactor < 0.35) {
                // 恢复根部的方块粒子和原版粒子混合
                float roll = RNG.nextFloat();
                if (roll < 0.2f) {
                    level.addParticle(ParticleTypes.LAVA, px, py, pz, vx * 0.6, vy * 0.7, vz * 0.6);
                } else if (roll < 0.5f) {
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
                } else if (roll < 0.7f) {
                    // 硫磺粉尘方块
                    level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.YELLOW_CONCRETE.defaultBlockState()), px, py, pz, vx, vy * 0.8, vz);
                } else {
                    // 黑曜石碎屑
                    level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.BLACK_CONCRETE.defaultBlockState()), px, py, pz, vx, vy * 0.8, vz);
                }
            } else if (heightFactor < 0.75) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, vx * 1.2, vy * 0.8, vz * 1.2);
                if (RNG.nextFloat() < 0.3f) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx, vy * 0.7, vz);
                }
            } else {
                if (RNG.nextFloat() < 0.3f) {
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz, vx * 1.5, vy * 0.5, vz * 1.5);
                }
            }
        }

        int lavaCount = burstPhase ? (int)(20 * distanceMul) : (int) (8 * intensity * distanceMul);
        for (int i = 0; i < lavaCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double speed = 1.5 + RNG.nextDouble() * 2.5 * intensity;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = 2.5 + RNG.nextDouble() * 2.0;

            level.addParticle(ParticleTypes.LAVA, cx, cy + 1.0, cz, vx, vy, vz);
        }

        int ringCount = (int) (15 * intensity * distanceMul) + (burstPhase ? 15 : 0);
        for (int i = 0; i < ringCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double r = 2.0 + RNG.nextDouble() * 6.0;
            double px = cx + Math.cos(angle) * r;
            double pz = cz + Math.sin(angle) * r;
            double py = cy + 0.1;

            double vx = Math.cos(angle) * (0.8 + intensity);
            double vz = Math.sin(angle) * (0.8 + intensity);
            double vy = 0.5 + RNG.nextDouble() * 1.0;

            float roll = RNG.nextFloat();
            if (roll < 0.5f) {
                level.addParticle(ParticleTypes.ASH, px, py, pz, vx, vy, vz);
            } else if (roll < 0.8f) {
                level.addParticle(ParticleTypes.SMOKE, px, py, pz, vx * 1.5, vy, vz * 1.5);
            } else {
                // 冲击波带出的岩浆星点
                level.addParticle(ParticleTypes.LAVA, px, py, pz, vx * 0.5, vy * 0.5, vz * 0.5);
            }
        }
    }
}