package com.tairitsutek.newgc.Planets.mercury.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MercurySolarFlareOverlay {

    private static final RandomSource RNG = RandomSource.create();

    private static final float CLIENT_SMOOTH = 0.05f;
    private static final int MAX_ALPHA = 150;

    private static float clientSmoothed = 0f;
    private static long flashTimer = 0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) return;

        float intensity = MercurySolarFlareClientCache.getIntensity();
        if (Float.isNaN(intensity) || Float.isInfinite(intensity)) intensity = 0f;
        intensity = Math.max(0f, Math.min(1.5f, intensity));

        // 平滑跟随
        clientSmoothed += (intensity - clientSmoothed) * CLIENT_SMOOTH;
        if (Float.isNaN(clientSmoothed)) clientSmoothed = 0f;

        if (clientSmoothed < 0.05f) return;

        float t = clientSmoothed / 1.5f;
        t = Math.max(0f, Math.min(1f, t));

        int alpha = (int) (t * MAX_ALPHA);
        if (alpha <= 0) return;

        // 白蓝色：R=0xE0, G=0xEE, B=0xFF
        int color = (alpha << 24) | 0xE0EEFF;

        GuiGraphics g = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        g.fill(0, 0, width, height, color);

        // FLARE 阶段：额外闪白脉冲
        if (MercurySolarFlareClientCache.isFlaring()) {
            flashTimer++;
            // 每 10 刻脉冲一次
            if (flashTimer % 10 < 3) {
                int flashAlpha = 60;
                g.fill(0, 0, width, height, (flashAlpha << 24) | 0xFFFFFF);
            }
        }
    }
}