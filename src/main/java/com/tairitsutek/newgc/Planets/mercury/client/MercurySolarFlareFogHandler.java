package com.tairitsutek.newgc.Planets.mercury.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MercurySolarFlareFogHandler {

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) return;

        float intensity = MercurySolarFlareClientCache.getIntensity();
        if (Float.isNaN(intensity) || Float.isInfinite(intensity)) intensity = 0f;
        intensity = Math.max(0f, Math.min(1.5f, intensity));

        if (intensity < 0.2f) return;

        float t = Math.max(0f, Math.min(1f, (intensity - 0.2f) / 1.3f));

        // 天空染成淡白蓝色
        float r = event.getRed()   + (0.95f - event.getRed())   * t * 0.7f;
        float g = event.getGreen() + (0.97f - event.getGreen()) * t * 0.7f;
        float b = event.getBlue()  + (1.00f - event.getBlue())  * t * 0.7f;

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }
}