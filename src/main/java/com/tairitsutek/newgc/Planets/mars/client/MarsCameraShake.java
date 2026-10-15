package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    private static final double MAX_DISTANCE = 64.0;
    private static final float MAX_AMPLITUDE = 1.6f;
    private static final float SMOOTH = 0.08f;

    private static float smoothedAmplitude = 0f;
    private static float shakeTime = 0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        float targetAmplitude = 0f;

        // 改：从 ClientCache 读
        BlockPos vp = MarsVolcanoClientCache.getVolcanoPos();
        float intensity = MarsVolcanoClientCache.getEruptionIntensity();

        if (vp != null && intensity > 0f) {
            Vec3 p = mc.player.position();
            double dx = p.x - (vp.getX() + 0.5);
            double dy = p.y - (vp.getY() + 0.5);
            double dz = p.z - (vp.getZ() + 0.5);
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist < MAX_DISTANCE) {
                double t = dist / MAX_DISTANCE;
                double distanceFactor = 1.0 - t * t;
                targetAmplitude = (float) (MAX_AMPLITUDE * intensity * distanceFactor);
            }
        }

        smoothedAmplitude += (targetAmplitude - smoothedAmplitude) * SMOOTH;
        if (Float.isNaN(smoothedAmplitude)) smoothedAmplitude = 0f;
        if (smoothedAmplitude < 0.01f) return;

        shakeTime += 1.0f;

        float jitterX = (RNG.nextFloat() - 0.5f) * smoothedAmplitude;
        float jitterY = (RNG.nextFloat() - 0.5f) * smoothedAmplitude;

        float waveX = (float) Math.sin(shakeTime * 0.35f) * smoothedAmplitude * 0.4f;
        float waveY = (float) Math.cos(shakeTime * 0.42f) * smoothedAmplitude * 0.3f;

        event.setYaw(event.getYaw() + jitterX + waveX);
        event.setPitch(event.getPitch() + jitterY + waveY);
    }
}