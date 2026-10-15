package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterWindParticles;
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

    // 缩小范围，降低数量
    private static final int RADIUS = 16;
    private static final int HEIGHT_MIN = -4;
    private static final int HEIGHT_MAX = 8;

    private static final int BASE_COUNT = 25;
    private static final int MAX_COUNT = 60;

    private static final double BASE_WIND_SPEED = 1.5;

    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);
    private static final double WIND_JITTER = 0.08;
    private static final double VERTICAL_DRIFT = 0.04;

    // 每 2 刻生成一次
    private static final int TICK_INTERVAL = 2;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        if (player.tickCount % TICK_INTERVAL != 0) return;

        spawnWind(level, player);
    }

    private static void spawnWind(ClientLevel level, Player player) {
        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 1.0f;
        strength = Math.max(0.0f, Math.min(2.5f, strength));

        int count = (int) (BASE_COUNT * (0.5f + strength * 0.5f));
        count = Math.min(count, MAX_COUNT);
        count = Math.max(count, 5);

        double windSpeed = BASE_WIND_SPEED * (0.5f + strength * 0.5f);

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
        }
    }
}