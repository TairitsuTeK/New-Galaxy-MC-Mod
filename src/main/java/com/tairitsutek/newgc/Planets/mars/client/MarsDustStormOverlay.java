package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsDustStormOverlay {

    private static final float CLIENT_SMOOTH = 0.01f;
    private static final int MAX_ALPHA = 140;

    private static float clientSmoothed = 0f;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        float strength = MarsDustStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(1.5f, strength));

        float target = strength;
        clientSmoothed += (target - clientSmoothed) * CLIENT_SMOOTH;
        if (Float.isNaN(clientSmoothed)) clientSmoothed = 0f;

        if (clientSmoothed < 0.05f) return;

        float t = clientSmoothed / 1.5f;
        t = Math.max(0f, Math.min(1f, t));
        int alpha = (int) (t * MAX_ALPHA);
        if (alpha <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // 黄褐色：R=0xC8, G=0x96, B=0x50
        int color = (alpha << 24) | 0xC89650;
        g.fill(0, 0, width, height, color);
    }
}