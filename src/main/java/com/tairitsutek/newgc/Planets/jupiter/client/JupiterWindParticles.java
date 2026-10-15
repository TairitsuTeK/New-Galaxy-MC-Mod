package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormManager;
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

    private static final int RADIUS = 36;
    private static final int HEIGHT_MIN = -10;
    private static final int HEIGHT_MAX = 28;

    private static final int BASE_COUNT = 130;
    private static final double BASE_WIND_SPEED = 2.0;

    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);
    private static final double WIND_JITTER = 0.08;
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
        float strength = JupiterStormManager.getStormStrength();
        int count = (int) (BASE_COUNT * strength);
        double windSpeed = BASE_WIND_SPEED * strength;

        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        for (int i = 0; i < count; i++) {
            double dx = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dz = (RNG.nextDouble() - 0.5) * RADIUS * 2;
            double dy = HEIGHT_MIN + RNG.nextDouble() * (HEIGHT_MAX - HEIGHT_MIN);

            double px = player.getX() + dx;
            double py = player.getY() + dy;
            double pz = player.getZ() + dz;

            if (!level.isEmptyBlock(BlockPos.containing(px, py, pz))) continue;

            double speed = windSpeed * (0.6 + RNG.nextDouble() * 0.8);
            double vx = dirX * speed + (RNG.nextDouble() - 0.5) * WIND_JITTER;
            double vz = dirZ * speed + (RNG.nextDouble() - 0.5) * WIND_JITTER;
            double vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;

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

            if (RNG.nextFloat() < 0.15f) {
                level.addParticle(
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        px, py, pz,
                        vx * 1.2, vy * 0.5, vz * 1.2);
            }

            if (RNG.nextFloat() < 0.08f) {
                double sparkVx = dirX * speed * 1.5 + (RNG.nextDouble() - 0.5) * 0.3;
                double sparkVz = dirZ * speed * 1.5 + (RNG.nextDouble() - 0.5) * 0.3;
                double sparkVy = (RNG.nextDouble() - 0.5) * 0.15;
                level.addParticle(
                        ParticleTypes.FLAME,
                        px, py, pz,
                        sparkVx, sparkVy, sparkVz);
            }

            if (RNG.nextFloat() < 0.12f) {
                double ashVx = dirX * speed * 0.5 + (RNG.nextDouble() - 0.5) * 0.05;
                double ashVz = dirZ * speed * 0.5 + (RNG.nextDouble() - 0.5) * 0.05;
                double ashVy = (RNG.nextDouble() - 0.5) * 0.02;
                level.addParticle(
                        ParticleTypes.WHITE_ASH,
                        px, py, pz,
                        ashVx, ashVy, ashVz);
            }

            if (RNG.nextFloat() < 0.03f) {
                double glowVx = dirX * speed * 2.0 + (RNG.nextDouble() - 0.5) * 0.5;
                double glowVz = dirZ * speed * 2.0 + (RNG.nextDouble() - 0.5) * 0.5;
                double glowVy = (RNG.nextDouble() - 0.5) * 0.2;
                level.addParticle(
                        ParticleTypes.END_ROD,
                        px, py, pz,
                        glowVx, glowVy, glowVz);
            }
        }
    }
}