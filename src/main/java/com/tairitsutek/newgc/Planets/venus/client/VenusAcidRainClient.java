package com.tairitsutek.newgc.Planets.venus.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

public class VenusAcidRainClient {

    private static final RandomSource RNG = RandomSource.create();
    private static int soundCooldown = 0;

    // ===== 酸雨 =====
    private static final int RAIN_RADIUS      = 16;
    private static final int RAIN_COUNT       = 12;
    private static final int RAIN_SEG_MIN     = 5;
    private static final int RAIN_SEG_MAX     = 9;
    private static final double SEGMENT_GAP   = 0.25;
    private static final double FALL_SPEED    = -3.0;
    private static final double WIND_X        = 0.6;
    private static final double WIND_Z        = 0.15;
    private static final int RAIN_HEIGHT_MIN  = 22;
    private static final int RAIN_HEIGHT_MAX  = 38;

    // ===== 酸雨云（降低 + 变大） =====
    private static final int CLOUD_COUNT      = 3;
    private static final int CLOUD_RADIUS     = 20;
    private static final int CLOUD_HEIGHT_MIN = 16;   // 云高下限（贴近雨层）
    private static final int CLOUD_HEIGHT_MAX = 26;   // 云高上限
    private static final double CLOUD_DRIFT_X = 0.06;
    private static final double CLOUD_DRIFT_Z = 0.02;

    // ===== 地面腐蚀烟 =====
    private static final int SMOKE_COUNT      = 2;
    private static final int SMOKE_RADIUS     = 10;

    public static void tick(Player player) {
        if (!(player.level() instanceof ClientLevel level)) return;

        spawnClouds(level, player);
        spawnRain(level, player);
        spawnCorrosionSmoke(level, player);
        playCorrosionSound(level, player);
    }

    // 硫酸云：更低、更大、东向漂移
    private static void spawnClouds(ClientLevel level, Player player) {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            double dx = (RNG.nextDouble() - 0.5) * CLOUD_RADIUS * 2;
            double dz = (RNG.nextDouble() - 0.5) * CLOUD_RADIUS * 2;
            // 云高度降低：贴近酸雨层
            double cy = player.getY() + CLOUD_HEIGHT_MIN
                    + RNG.nextDouble() * (CLOUD_HEIGHT_MAX - CLOUD_HEIGHT_MIN);

            // 云更大：更多粒子 + 更广分布
            int puffs = 5 + RNG.nextInt(4);   // 5~8 个粒子
            for (int t = 0; t < puffs; t++) {
                level.addParticle(
                        ParticleTypes.CLOUD,
                        player.getX() + dx + (RNG.nextDouble() - 0.5) * 10,
                        cy + (RNG.nextDouble() - 0.5) * 4,
                        player.getZ() + dz + (RNG.nextDouble() - 0.5) * 10,
                        CLOUD_DRIFT_X, 0.0, CLOUD_DRIFT_Z
                );
            }
        }
    }

    // 斜切酸雨丝
    private static void spawnRain(ClientLevel level, Player player) {
        double sway = Math.sin(level.getGameTime() * 0.03d) * 0.2;
        double windX = WIND_X + sway;

        double mag = Math.sqrt(windX * windX + FALL_SPEED * FALL_SPEED + WIND_Z * WIND_Z);
        double ux = windX / mag;
        double uy = FALL_SPEED / mag;
        double uz = WIND_Z / mag;

        for (int i = 0; i < RAIN_COUNT; i++) {
            int dx = RNG.nextInt(RAIN_RADIUS * 2 + 1) - RAIN_RADIUS;
            int dz = RNG.nextInt(RAIN_RADIUS * 2 + 1) - RAIN_RADIUS;

            double px = player.getX() + dx + RNG.nextDouble();
            double pz = player.getZ() + dz + RNG.nextDouble();
            double startY = player.getY() + RAIN_HEIGHT_MIN
                    + RNG.nextInt(RAIN_HEIGHT_MAX - RAIN_HEIGHT_MIN + 1);

            int segments = RAIN_SEG_MIN + RNG.nextInt(RAIN_SEG_MAX - RAIN_SEG_MIN + 1);
            double speedVar = 0.85 + RNG.nextDouble() * 0.3;

            for (int s = 0; s < segments; s++) {
                double t = s * SEGMENT_GAP * speedVar;
                level.addParticle(
                        ParticleTypes.FALLING_HONEY,
                        px + ux * t,
                        startY + uy * t,
                        pz + uz * t,
                        windX * speedVar,
                        FALL_SPEED * speedVar,
                        WIND_Z * speedVar
                );
            }
        }
    }

    // 落地腐蚀烟
    private static void spawnCorrosionSmoke(ClientLevel level, Player player) {
        for (int i = 0; i < SMOKE_COUNT; i++) {
            int dx = RNG.nextInt(SMOKE_RADIUS * 2 + 1) - SMOKE_RADIUS;
            int dz = RNG.nextInt(SMOKE_RADIUS * 2 + 1) - SMOKE_RADIUS;
            int wx = (int) player.getX() + dx;
            int wz = (int) player.getZ() + dz;

            BlockPos ground = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING, new BlockPos(wx, 0, wz));
            double gx = wx + RNG.nextDouble();
            double gz = wz + RNG.nextDouble();
            double gy = ground.getY() + 0.15;

            double spreadX = (RNG.nextDouble() - 0.5) * 0.06;
            double spreadZ = (RNG.nextDouble() - 0.5) * 0.06;
            double rise = 0.02 + RNG.nextDouble() * 0.03;

            if (RNG.nextBoolean()) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, gx, gy, gz, spreadX, rise, spreadZ);
            } else {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, gx, gy, gz, spreadX, rise, spreadZ);
            }
        }
    }

    // 酸雨腐蚀嘶声
    private static void playCorrosionSound(ClientLevel level, Player player) {
        if (soundCooldown <= 0) {
            soundCooldown = 25 + RNG.nextInt(20);
            double sx = player.getX() + (RNG.nextDouble() - 0.5) * 12;
            double sy = player.getY() + RNG.nextDouble() * 2;
            double sz = player.getZ() + (RNG.nextDouble() - 0.5) * 12;

            level.playLocalSound(
                    sx, sy, sz,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.AMBIENT,
                    0.3f,
                    0.9f + RNG.nextFloat() * 0.2f,
                    false
            );
        } else {
            soundCooldown--;
        }
    }
}
