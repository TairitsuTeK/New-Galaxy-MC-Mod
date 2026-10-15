package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class JupiterStormOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        float strength = JupiterStormManager.getStormStrength();
        if (strength < 1.0f) return;

        // 钳制 strength
        strength = Math.min(strength, 2.0f);

        GuiGraphics g = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // 钳制 alpha
        int alpha = (int) ((strength - 1.0f) * 150);
        alpha = Math.max(0, Math.min(180, alpha));

        int color = (alpha << 24) | 0xCC6600;
        g.fill(0, 0, width, height, color);
    }
}