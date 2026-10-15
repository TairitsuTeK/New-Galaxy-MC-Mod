package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsDustStormCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    private static final float SMOOTH = 0.03f;
    private static final float SHAKE_THRESHOLD = 0.7f;   // 低于此强度不晃
    private static final float MAX_AMPLITUDE = 0.35f;    // 比火山轻
    private static final float SHAKE_FREQUENCY = 0.05f;

    private static float smoothedStrength = 0f;
    private static float shakeTime = 0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        float strength = MarsDustStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(1.5f, strength));

        smoothedStrength += (strength - smoothedStrength) * SMOOTH;
        if (Float.isNaN(smoothedStrength)) smoothedStrength = 0f;

        if (smoothedStrength <= SHAKE_THRESHOLD) return;

        float intensity = (smoothedStrength - SHAKE_THRESHOLD) / (1.5f - SHAKE_THRESHOLD);
        intensity = Math.max(0f, Math.min(1f, intensity));

        shakeTime += 1f;

        // 慢速正弦 + 轻随机
        float waveX = (float) Math.sin(shakeTime * SHAKE_FREQUENCY) * intensity * MAX_AMPLITUDE;
        float waveY = (float) Math.cos(shakeTime * SHAKE_FREQUENCY * 1.2f) * intensity * MAX_AMPLITUDE * 0.6f;

        float jitterX = (RNG.nextFloat() - 0.5f) * intensity * 0.15f;
        float jitterY = (RNG.nextFloat() - 0.5f) * intensity * 0.15f;

        event.setYaw(event.getYaw() + waveX + jitterX);
        event.setPitch(event.getPitch() + waveY + jitterY);
    }
}