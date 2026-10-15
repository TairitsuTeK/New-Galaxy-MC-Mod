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

    // ===== 客户端平滑系数：越小变化越慢 =====
    // 0.05 = 约 20 帧追上目标
    // 0.02 = 约 50 帧追上目标（推荐）
    // 0.01 = 约 100 帧追上目标（很慢）
    private static final float CLIENT_SMOOTH = 0.02f;

    // ===== alpha 上限 =====
    private static final int MAX_ALPHA = 120;

    // ===== 客户端平滑值 =====
    private static float clientSmoothed = 1.0f;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 1.0f;

        // 目标值：低于 1.0 时目标就是 1.0（无染色）
        float target = Math.max(1.0f, Math.min(2.5f, strength));

        // 客户端平滑
        clientSmoothed += (target - clientSmoothed) * CLIENT_SMOOTH;
        if (Float.isNaN(clientSmoothed)) clientSmoothed = 1.0f;

        // 强度低于 1.0 时不染色
        if (clientSmoothed < 1.0f) return;

        // 计算 alpha
        float t = (clientSmoothed - 1.0f) / 1.5f;   // 归一化到 0~1
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