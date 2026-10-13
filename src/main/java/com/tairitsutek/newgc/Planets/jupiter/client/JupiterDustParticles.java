package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
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
public class JupiterDustParticles {

    private static final RandomSource RNG = RandomSource.create();

    // ===== 尘埃参数 =====
    private static final int DUST_RADIUS     = 32;
    private static final int DUST_COUNT      = 40;
    private static final int DUST_HEIGHT_MIN = -8;
    private static final int DUST_HEIGHT_MAX = 24;

    // ===== 漂移 =====
    private static final double DRIFT_SPEED = 0.015;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        if (!(player.level() instanceof ClientLevel level)) return;

        spawnDust(level, player);
    }

    private static void spawnDust(ClientLevel level, Player player) {
        for (int i = 0; i < DUST_COUNT; i++) {
            double dx = (RNG.nextDouble() - 0.5) * DUST_RADIUS * 2;
            double dz = (RNG.nextDouble() - 0.5) * DUST_RADIUS * 2;
            double dy = DUST_HEIGHT_MIN
                    + RNG.nextDouble() * (DUST_HEIGHT_MAX - DUST_HEIGHT_MIN);

            double px = player.getX() + dx;
            double py = player.getY() + dy;
            double pz = player.getZ() + dz;

            // 缓慢随机漂移
            double vx = (RNG.nextDouble() - 0.5) * DRIFT_SPEED;
            double vy = (RNG.nextDouble() - 0.5) * DRIFT_SPEED * 0.4;
            double vz = (RNG.nextDouble() - 0.5) * DRIFT_SPEED;

            // ===== 橙黑两色分布 =====
            float roll = RNG.nextFloat();
            if (roll < 0.55f) {
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