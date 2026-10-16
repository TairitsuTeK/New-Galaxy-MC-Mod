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

/**
 * 水星耀斑视觉（高逼真版）：
 * 1. 暗角渐晕（边缘变暗）
 * 2. 边缘淡蓝辉光
 * 3. FLARE：CRT 扫描线（双色）
 * 4. FLARE：水平撕裂条（带错位）
 * 5. FLARE：RGB 彩色偏移
 * 6. FLARE：漏液竖条
 * 7. FLARE：中心暖白光晕
 *
 * 所有效果随 intensity 平滑渐入渐出，无突变，不刺眼。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MercurySolarFlareOverlay {

    private static final RandomSource RNG = RandomSource.create();

    private static final float SMOOTH = 0.03f;

    private static float smoothed = 0f;
    private static long effectTick = 0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) return;

        float intensity = MercurySolarFlareClientCache.getIntensity();
        if (Float.isNaN(intensity) || Float.isInfinite(intensity)) intensity = 0f;
        intensity = Math.max(0f, Math.min(1.5f, intensity));

        smoothed += (intensity - smoothed) * SMOOTH;
        if (Float.isNaN(smoothed)) smoothed = 0f;
        if (smoothed < 0.02f) return;

        effectTick++;

        float t = Math.max(0f, Math.min(1f, smoothed / 1.5f));

        GuiGraphics g = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // ===== 1) 暗角（四角压暗）=====
        drawVignette(g, w, h, t);

        // ===== 2) 边缘辉光 =====
        drawEdgeGlow(g, w, h, t);

        // ===== FLARE 阶段独有 =====
        if (MercurySolarFlareClientCache.isFlaring()) {
            float flareT = Math.max(0f, Math.min(1f, (smoothed - 0.8f) / 0.7f));
            if (flareT <= 0.01f) return;

            // 3) CRT 扫描线
            drawScanlines(g, w, h, flareT);

            // 4) 撕裂条（带错位）
            drawTearBars(g, w, h, flareT);

            // 5) RGB 彩色偏移
            drawChromaticAberration(g, w, h, flareT);

            // 6) 漏液竖条
            drawLiquidLeak(g, w, h, flareT);

            // 7) 中心暖白光晕
            drawCenterGlow(g, w, h, t);
        }
    }

    // ================= 1) 暗角 =================
    private static void drawVignette(GuiGraphics g, int w, int h, float t) {
        int bands = 10;
        int maxBand = Math.min(w, h) / 2;
        for (int i = 0; i < bands; i++) {
            float band = (float) i / bands;
            int inset = (int) (maxBand * band);
            int alpha = (int) (t * 90 * (1f - band));
            if (alpha <= 0) continue;

            int color = (alpha << 24);   // 纯黑
            // 上、下、左、右四条
            g.fill(0, 0, w, inset, color);
            g.fill(0, h - inset, w, h, color);
            g.fill(0, inset, inset, h - inset, color);
            g.fill(w - inset, inset, w, h - inset, color);
        }
    }

    // ================= 2) 边缘辉光 =================
    private static void drawEdgeGlow(GuiGraphics g, int w, int h, float t) {
        int bands = 8;
        int maxInset = Math.min(w, h) / 3;
        for (int i = 0; i < bands; i++) {
            float band = (float) i / bands;
            int inset = (int) (maxInset * (1f - band));
            int alpha = (int) (t * 55 * (1f - band));
            if (alpha <= 0) continue;

            int color = (alpha << 24) | 0xC8E0FF;
            g.fill(0, inset, w, inset + 1, color);
            g.fill(0, h - inset - 1, w, h - inset, color);
            g.fill(inset, 0, inset + 1, h, color);
            g.fill(w - inset - 1, 0, w, inset + 1, color);
        }
    }

    // ================= 3) CRT 扫描线 =================
    private static void drawScanlines(GuiGraphics g, int w, int h, float t) {
        // 双色：暗线 + 稍亮的线，交替
        int darkAlpha = (int) (t * 30);
        int lightAlpha = (int) (t * 12);
        if (darkAlpha <= 0 && lightAlpha <= 0) return;

        int darkColor = (darkAlpha << 24);
        int lightColor = (lightAlpha << 24) | 0xFFFFFF;

        int gap = 3;
        for (int y = 0; y < h; y += gap) {
            g.fill(0, y, w, y + 1, darkColor);
            if (y + 1 < h && lightAlpha > 0) {
                g.fill(0, y + 1, w, y + 2, lightColor);
            }
        }
    }

    // ================= 4) 撕裂条 =================
    private static void drawTearBars(GuiGraphics g, int w, int h, float t) {
        int count = (int) (8 * t);
        count = Math.max(1, Math.min(8, count));

        for (int i = 0; i < count; i++) {
            int barY = RNG.nextInt(h);
            int barH = 1 + RNG.nextInt(6);

            // 撕裂条位置/宽度：从左边随机到右边随机
            int startX = RNG.nextInt(w / 3);
            int endX = w - RNG.nextInt(w / 3);
            if (endX <= startX) continue;

            // 随机色：红/绿/蓝/白
            float roll = RNG.nextFloat();
            int rgb;
            if (roll < 0.30f)      rgb = 0xFF3030;   // 红
            else if (roll < 0.55f) rgb = 0x30FF30;   // 绿
            else if (roll < 0.80f) rgb = 0x3030FF;   // 蓝
            else                   rgb = 0xFFFFFF;   // 白

            int alpha = (int) (t * 110);
            if (alpha <= 0) continue;

            int color = (alpha << 24) | rgb;
            g.fill(startX, barY, endX, barY + barH, color);

            // 错位：撕裂条下方 1~2 像素的偏移色
            int offsetY = barY + barH;
            int offsetH = 1 + RNG.nextInt(2);
            int offsetStartX = Math.max(0, startX - 3 + RNG.nextInt(6));
            int offsetEndX = Math.min(w, endX - 3 + RNG.nextInt(6));
            int offsetAlpha = (int) (t * 80);
            if (offsetAlpha > 0 && offsetEndX > offsetStartX) {
                int offsetColor = (offsetAlpha << 24) | 0x000000;
                g.fill(offsetStartX, offsetY, offsetEndX, offsetY + offsetH, offsetColor);
            }

            // 撕裂条上方加一条亮线
            int hiAlpha = (int) (t * 70);
            if (hiAlpha > 0) {
                int hiColor = (hiAlpha << 24) | 0xFFFFFF;
                g.fill(startX, Math.max(0, barY - 1), endX, barY, hiColor);
            }
        }
    }

    // ================= 5) RGB 彩色偏移 =================
    private static void drawChromaticAberration(GuiGraphics g, int w, int h, float t) {
        // 在屏幕上下边缘画两条半透明色条，模拟 RGB 分离
        int alpha = (int) (t * 25);
        if (alpha <= 0) return;

        int redColor = (alpha << 24) | 0xFF0000;
        int cyanColor = (alpha << 24) | 0x00FFFF;

        // 左边缘红，右边缘青
        g.fill(0, 0, 2, h, redColor);
        g.fill(w - 2, 0, w, h, cyanColor);
    }

    // ================= 6) 漏液竖条 =================
    private static void drawLiquidLeak(GuiGraphics g, int w, int h, float t) {
        int count = (int) (4 * t);
        count = Math.max(1, Math.min(5, count));

        for (int i = 0; i < count; i++) {
            int x = RNG.nextInt(w);
            int barW = 1 + RNG.nextInt(3);

            int startY = RNG.nextInt(h / 3);
            int len = h / 4 + RNG.nextInt(h / 2);

            // 蓝色/紫色渐变
            float roll = RNG.nextFloat();
            int rgb;
            if (roll < 0.5f) rgb = 0x4060C0;   // 蓝紫
            else             rgb = 0x8040A0;   // 紫

            int alpha = (int) (t * 70);
            if (alpha <= 0) continue;

            // 用多层模拟渐变
            int layers = 6;
            for (int l = 0; l < layers; l++) {
                float lr = (float) l / layers;
                int layerAlpha = (int) (alpha * (1f - lr));
                if (layerAlpha <= 0) continue;
                int color = (layerAlpha << 24) | rgb;
                int y0 = startY + (int) (len * lr);
                int y1 = startY + (int) (len * (lr + 1f / layers));
                g.fill(x, y0, x + barW, y1, color);
            }
        }
    }

    // ================= 7) 中心暖白光晕 =================
    private static void drawCenterGlow(GuiGraphics g, int w, int h, float t) {
        int cx = w / 2;
        int cy = h / 2;
        int maxR = (int) (Math.min(w, h) * 0.55f * t);

        int steps = 20;
        for (int i = steps; i > 0; i--) {
            float ratio = (float) i / steps;
            int r = (int) (maxR * ratio);
            int alpha = (int) (t * 20 * (1f - ratio) * (1f - ratio));
            if (alpha <= 0) continue;
            int color = (alpha << 24) | 0xFFF0C0;
            g.fill(cx - r, cy - r, cx + r, cy + r, color);
        }
    }
}