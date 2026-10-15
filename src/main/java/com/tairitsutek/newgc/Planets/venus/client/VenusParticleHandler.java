package com.tairitsutek.newgc.Planets.venus.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class VenusParticleHandler {

    private static final float SPAWN_CHANCE = 0.6f;
    private static final double RADIUS = 16.0;
    private static final double VERTICAL_RANGE = 12.0;

    private static final Vector3f COLOR_ORANGE = new Vector3f(1.0f, 0.65f, 0.15f);
    private static final Vector3f COLOR_GRAY   = new Vector3f(0.55f, 0.55f, 0.55f);
    private static final Vector3f COLOR_BLACK  = new Vector3f(0.15f, 0.15f, 0.15f);

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) return;

        RandomSource random = level.random;
        Vec3 playerPos = mc.player.position();

        int attempts = 6;
        for (int i = 0; i < attempts; i++) {
            if (random.nextFloat() > SPAWN_CHANCE) continue;

            double x = playerPos.x + (random.nextDouble() - 0.5) * 2 * RADIUS;
            double y = playerPos.y + (random.nextDouble() - 0.5) * 2 * VERTICAL_RANGE;
            double z = playerPos.z + (random.nextDouble() - 0.5) * 2 * RADIUS;

            if (!level.isEmptyBlock(BlockPos.containing(x, y, z))) continue;

            double vx = (random.nextDouble() - 0.5) * 0.02;
            double vy = random.nextDouble() * 0.015 + 0.005;
            double vz = (random.nextDouble() - 0.5) * 0.02;

            int colorChoice = random.nextInt(3);
            Vector3f color = switch (colorChoice) {
                case 0 -> COLOR_ORANGE;
                case 1 -> COLOR_GRAY;
                default -> COLOR_BLACK;
            };

            float size = 1.0f + random.nextFloat() * 1.5f;

            level.addParticle(
                    new DustParticleOptions(color, size),
                    x, y, z,
                    vx, vy, vz
            );

            if (random.nextFloat() < 0.3f) {
                level.addParticle(
                        ParticleTypes.SMOKE,
                        x, y, z,
                        vx * 0.5, vy * 0.5, vz * 0.5
                );
            }
        }
    }
}