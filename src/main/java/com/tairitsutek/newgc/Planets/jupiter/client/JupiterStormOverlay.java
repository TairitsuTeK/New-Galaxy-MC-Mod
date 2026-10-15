package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class JupiterStormOverlay {

    private static final float CLIENT_SMOOTH = 0.01f;
    private static final int MAX_ALPHA = 120;

    private static float clientSmoothed = 1.0f;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        // 改：从客户端缓存读
        float strength = JupiterStormClientCache.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 1.0f;

        float target = Math.max(1.0f, Math.min(2.5f, strength));

        clientSmoothed += (target - clientSmoothed) * CLIENT_SMOOTH;
        if (Float.isNaN(clientSmoothed)) clientSmoothed = 1.0f;

        if (clientSmoothed < 1.0f) return;

        float t = (clientSmoothed - 1.0f) / 1.5f;
        t = Math.max(0.0f, Math.min(1.0f, t));
        int alpha = (int) (t * MAX_ALPHA);
        alpha = Math.max(0, Math.min(MAX_ALPHA, alpha));

        if (alpha <= 0) return;

        GuiGraphics g = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        int color = (alpha << 24) | 0xCC6600;
        g.fill(0, 0, width, height, color);
    }
}