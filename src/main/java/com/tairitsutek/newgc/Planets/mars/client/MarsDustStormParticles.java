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

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsDustStormParticles {

    private static final RandomSource RNG = RandomSource.create();

    private static final int RADIUS = 24;
    private static final int HEIGHT_MIN = -4;
    private static final int HEIGHT_MAX = 16;

    private static final int BASE_COUNT = 20;
    private static final int MAX_COUNT = 120;
    private static final int MIN_COUNT = 0;

    // ===== 横向速度大幅提高 =====
    private static final double BASE_SPEED = 1.2;   // 原 0.35 → 1.2
    private static final float WIND_ANGLE = (float) Math.toRadians(120.0f);
    private static final double JITTER = 0.15;      // 原 0.06 → 0.15

    // 垂直抖动保持很小（沙尘基本水平吹）
    private static final double VERTICAL_DRIFT = 0.02;

    private static final int TICK_INTERVAL = 3;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (!player.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;
        if (!(player.level() instanceof ClientLevel level)) return;

        if (level.getGameTime() % TICK_INTERVAL != 0) return;

        spawnDust(level, player);
    }

    private static void spawnDust(ClientLevel level, Player player) {
        float strength = MarsDustStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(1.5f, strength));

        if (strength < 0.1f) return;

        int count = (int) (BASE_COUNT * strength);
        count = Math.min(count, MAX_COUNT);
        count = Math.max(count, MIN_COUNT);
        if (count <= 0) return;

        // 风速：强度越高越快，最高约 1.2 * 1.25 = 1.5
        double speed = BASE_SPEED * (0.5 + strength * 0.5);

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

            // 横向速度：主风向 * 风速 + 随机抖动
            double vx = dirX * speed * (0.7 + RNG.nextDouble() * 0.6)
                    + (RNG.nextDouble() - 0.5) * JITTER;
            double vz = dirZ * speed * (0.7 + RNG.nextDouble() * 0.6)
                    + (RNG.nextDouble() - 0.5) * JITTER;
            // 垂直速度很小
            double vy = (RNG.nextDouble() - 0.5) * VERTICAL_DRIFT;

            float roll = RNG.nextFloat();
            if (roll < 0.5f) {
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.ORANGE_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else if (roll < 0.85f) {
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

            // 高强度时加一点 ASH 颗粒感
            if (strength > 0.9f && RNG.nextFloat() < 0.1f * strength) {
                level.addParticle(ParticleTypes.ASH, px, py, pz, vx * 0.6, vy, vz * 0.6);
            }
        }
    }
}