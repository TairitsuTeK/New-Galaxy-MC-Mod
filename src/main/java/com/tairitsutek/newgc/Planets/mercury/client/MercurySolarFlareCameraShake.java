package com.tairitsutek.newgc.Planets.mercury.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MercurySolarFlareCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    private static final float SMOOTH = 0.04f;
    private static final float THRESHOLD = 1.0f;      // 超过 1.0 才晃
    private static final float MAX_AMPLITUDE = 0.3f;  // 很轻
    private static final float FREQUENCY = 0.1f;

    private static float smoothed = 0f;
    private static float shakeTime = 0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) return;

        float intensity = MercurySolarFlareClientCache.getIntensity();
        if (Float.isNaN(intensity) || Float.isInfinite(intensity)) intensity = 0f;
        intensity = Math.max(0f, Math.min(1.5f, intensity));

        smoothed += (intensity - smoothed) * SMOOTH;
        if (Float.isNaN(smoothed)) smoothed = 0f;

        if (smoothed <= THRESHOLD) return;

        float t = (smoothed - THRESHOLD) / (1.5f - THRESHOLD);
        t = Math.max(0f, Math.min(1f, t));

        shakeTime += 1f;

        float waveX = (float) Math.sin(shakeTime * FREQUENCY) * t * MAX_AMPLITUDE;
        float waveY = (float) Math.cos(shakeTime * FREQUENCY * 1.3f) * t * MAX_AMPLITUDE * 0.6f;

        event.setYaw(event.getYaw() + waveX);
        event.setPitch(event.getPitch() + waveY);
    }
}