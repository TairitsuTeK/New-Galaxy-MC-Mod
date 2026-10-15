package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class JupiterCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    private static final float SMOOTH = 0.02f;
    private static final float SHAKE_THRESHOLD = 1.3f;
    private static final float SHAKE_AMPLITUDE = 2.5f;
    private static final float SHAKE_FREQUENCY = 0.15f;

    private static float smoothedStrength = 0.0f;
    private static float shakeTime = 0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        // 改：从客户端缓存读
        float strength = JupiterStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0.0f;
        strength = Math.max(0.0f, Math.min(2.5f, strength));

        smoothedStrength += (strength - smoothedStrength) * SMOOTH;
        if (Float.isNaN(smoothedStrength)) smoothedStrength = 0.0f;

        if (smoothedStrength <= SHAKE_THRESHOLD) return;

        float intensity = (smoothedStrength - SHAKE_THRESHOLD) / (2.5f - SHAKE_THRESHOLD);
        intensity = Math.max(0.0f, Math.min(1.0f, intensity));

        shakeTime += 1.0f;

        float waveX = (float) Math.sin(shakeTime * SHAKE_FREQUENCY) * intensity * SHAKE_AMPLITUDE;
        float waveY = (float) Math.cos(shakeTime * SHAKE_FREQUENCY * 1.3f) * intensity * SHAKE_AMPLITUDE * 0.6f;

        float jitterX = (RNG.nextFloat() - 0.5f) * intensity * 0.5f;
        float jitterY = (RNG.nextFloat() - 0.5f) * intensity * 0.5f;

        event.setYaw(event.getYaw() + waveX + jitterX);
        event.setPitch(event.getPitch() + waveY + jitterY);
    }
}