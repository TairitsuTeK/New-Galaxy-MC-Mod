package com.tairitsutek.newgc.Planets.venus.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class VenusParticleHandler {

    private static final float SPAWN_CHANCE = 0.6f;

    private static final double RADIUS = 16.0;

    private static final double VERTICAL_RANGE = 12.0;

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

            if (!level.isEmptyBlock(net.minecraft.core.BlockPos.containing(x, y, z))) continue;

            double vx = (random.nextDouble() - 0.5) * 0.01;
            double vy = random.nextDouble() * 0.01 + 0.005;
            double vz = (random.nextDouble() - 0.5) * 0.01;

            level.addParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y, z,
                    vx, vy, vz
            );
        }
    }
}