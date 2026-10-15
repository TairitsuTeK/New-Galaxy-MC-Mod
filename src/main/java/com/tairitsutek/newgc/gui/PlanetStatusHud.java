package com.tairitsutek.newgc.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormClientCache;
import com.tairitsutek.newgc.Planets.mars.client.MarsVolcanoClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class PlanetStatusHud {

    // ===== 布局 =====
    private static final int WIDTH      = 210;
    private static final int PADDING    = 10;
    private static final int HEADER_H   = 20;
    private static final int ROW_H      = 15;
    private static final int CARD_GAP   = 8;
    private static final int MARGIN_TOP = 60;
    private static final int MARGIN_RIGHT = 10;
    private static final int CARD_INNER = ROW_H * 3 + PADDING * 2;

    // ===== 主色 =====
    private static final int BG          = 0xE00A121E;
    private static final int BG_INNER    = 0x90142436;
    private static final int BORDER_OUT  = 0xFF3A5570;
    private static final int BORDER_IN   = 0xFF1A2A3C;
    private static final int TITLE       = 0xFFB8D0F0;
    private static final int LABEL       = 0xFF8898B0;
    private static final int VALUE       = 0xFFE0ECFF;
    private static final int SEPARATOR   = 0xFF2A3E54;

    // ===== 阶段色 =====
    private static final int[] PHASE_COLORS = {
            0xFF7A8FA6, // CALM
            0xFFE0A050, // RISING
            0xFFE07A3A, // PEAK
            0xFFFF5B3A, // SUDDEN
            0xFFB07040  // FALLING
    };

    // ===== 进度条 =====
    private static final int BAR_BG_TOP  = 0xFF0A1420;
    private static final int BAR_BG_BOT  = 0xFF060C14;
    private static final int BAR_TICK    = 0x60FFFFFF;

    // ===== 火星色 =====
    private static final int MARS_DOT    = 0xFFE06B4B;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.options.hideGui) return;

        boolean inJupiter = mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY);
        boolean inMars    = mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY);
        if (!inJupiter && !inMars) return;

        GuiGraphics g = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();

        int x = screenW - WIDTH - MARGIN_RIGHT;
        int y = MARGIN_TOP;

        int cardCount = (inJupiter ? 1 : 0) + (inMars ? 1 : 0);
        int totalH = HEADER_H + cardCount * (CARD_INNER + CARD_GAP) + PADDING + 2;

        // 外发光
        g.fill(x - 1, y - 1, x + WIDTH + 1, y + totalH + 1, 0x40000000);

        // 主背景
        g.fill(x, y, x + WIDTH, y + totalH, BG);
        drawBorder(g, x, y, WIDTH, totalH, BORDER_OUT);

        // 内描边
        g.fill(x + 1, y + 1, x + WIDTH - 1, y + 2, BORDER_IN);
        g.fill(x + 1, y + totalH - 2, x + WIDTH - 1, y + totalH - 1, BORDER_IN);

        // 标题栏
        String planetName = inJupiter ? "木星 · 风暴" : "火星 · 火山";
        g.drawString(mc.font, "◈ 星球状态", x + PADDING, y + 6, TITLE, false);
        int nameW = mc.font.width(planetName);
        g.drawString(mc.font, planetName,
                x + WIDTH - PADDING - nameW, y + 6, TITLE, false);

        g.fill(x + PADDING, y + HEADER_H - 3, x + WIDTH - PADDING, y + HEADER_H - 2, SEPARATOR);

        int cy = y + HEADER_H;
        if (inJupiter) cy = drawJupiterCard(g, mc, x, cy);
        if (inMars)    cy = drawMarsCard(g, mc, x, cy);
    }

    // ================= 木星卡片 =================
    private static int drawJupiterCard(GuiGraphics g, Minecraft mc, int x, int y) {
        JupiterStormClientCache.Phase phase = JupiterStormClientCache.getPhase();
        int phaseColor = PHASE_COLORS[phase.ordinal()];
        float strength = JupiterStormClientCache.getStormStrength();
        int remain     = JupiterStormClientCache.getPhaseTimer();

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_INNER, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_INNER,
                0x40FFFFFF & phaseColor | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_INNER, phaseColor);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, "■ 木星风暴", tx, ty, TITLE, false);

        String phaseName = phaseName(phase);
        int pW = mc.font.width(phaseName);
        int pillPad = 4;
        int pillX = rightEdge - pW - pillPad * 2;
        int pillY = ty - 1;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (phaseColor & 0x00FFFFFF));
        g.drawString(mc.font, phaseName, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;

        drawRow(g, mc, tx, ty, rightEdge, "阶段剩余", formatTicks(remain), VALUE);
        ty += ROW_H;

        drawRow(g, mc, tx, ty, rightEdge, "风暴强度",
                String.format("%.2f", strength), VALUE);

        drawStormBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8,
                strength, phaseColor);

        return y + CARD_INNER + CARD_GAP;
    }

    private static void drawStormBar(GuiGraphics g, int x, int y, int w,
                                     float strength, int phaseColor) {
        int h = 7;

        g.fill(x, y, x + w, y + h / 2, BAR_BG_TOP);
        g.fill(x, y + h / 2, x + w, y + h, BAR_BG_BOT);

        float t = Math.max(0f, Math.min(1f, strength / 2.5f));
        int fillW = (int) (w * t);

        int fillColor;
        if (strength > 1.8f)      fillColor = 0xFFFF4A2A;
        else if (strength > 1.0f) fillColor = 0xFFFF8A3A;
        else if (strength > 0.5f) fillColor = 0xFFE0A050;
        else                      fillColor = phaseColor;

        g.fill(x, y, x + fillW, y + h, fillColor);
        g.fill(x, y, x + fillW, y + 1, lighten(fillColor, 0x40));
        g.fill(x, y + h - 1, x + fillW, y + h, darken(fillColor, 0x40));

        for (float tick : new float[]{0.2f, 0.4f, 0.6f, 0.8f}) {
            int tx = x + (int) (w * tick);
            g.fill(tx, y, tx + 1, y + h, BAR_TICK);
        }

        drawBorder(g, x, y, w, h, 0xFF1A2A3C);

        if (fillW > 0 && fillW < w) {
            g.fill(x + fillW, y - 1, x + fillW + 1, y + h + 1, 0xFFFFFFFF);
        }
    }

    private static String phaseName(JupiterStormClientCache.Phase p) {
        return switch (p) {
            case CALM    -> "平静";
            case RISING  -> "增强";
            case PEAK    -> "峰值";
            case SUDDEN  -> "暴烈";
            case FALLING -> "衰退";
        };
    }

    // ================= 火星卡片 =================
    private static int drawMarsCard(GuiGraphics g, Minecraft mc, int x, int y) {
        int eruption     = MarsVolcanoClientCache.getEruptionTicks();
        int total        = MarsVolcanoClientCache.getEruptionTotalTicks();
        int cooldown     = MarsVolcanoClientCache.getCooldownTimer();
        BlockPos vp      = MarsVolcanoClientCache.getVolcanoPos();
        boolean erupting = MarsVolcanoClientCache.isErupting();

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_INNER, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_INNER,
                0x40FFFFFF & MARS_DOT | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_INNER, MARS_DOT);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, "■ 火星火山", tx, ty, TITLE, false);

        String state = erupting ? "喷发中" : "冷却中";
        int sW = mc.font.width(state);
        int pillPad = 4;
        int pillX = rightEdge - sW - pillPad * 2;
        int pillY = ty - 1;
        int pillColor = erupting ? MARS_DOT : 0xFF4A5A6E;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (pillColor & 0x00FFFFFF));
        g.drawString(mc.font, state, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;

        if (erupting) {
            drawRow(g, mc, tx, ty, rightEdge, "喷发剩余", formatTicks(eruption), VALUE);
            ty += ROW_H;
            drawRow(g, mc, tx, ty, rightEdge, "本次总时长", formatTicks(total), VALUE);

            float progress = total > 0 ? (float) eruption / total : 0f;
            drawProgressBar(g, x + PADDING + 9, ty + ROW_H - 5,
                    WIDTH - PADDING * 2 - 17 - 8, progress, MARS_DOT);
        } else {
            // 冷却剩余：现在可以从 ClientCache 读到
            drawRow(g, mc, tx, ty, rightEdge, "冷却剩余", formatTicks(cooldown), VALUE);
            ty += ROW_H;

            String dist = "—";
            if (vp != null) {
                Vec3 p = mc.player.position();
                double d = Math.sqrt(p.distanceToSqr(
                        vp.getX() + 0.5, vp.getY() + 0.5, vp.getZ() + 0.5));
                dist = String.format("%.1f m", d);
            }
            drawRow(g, mc, tx, ty, rightEdge, "喷发点距离", dist, VALUE);
        }

        return y + CARD_INNER + CARD_GAP;
    }

    private static void drawProgressBar(GuiGraphics g, int x, int y, int w,
                                        float progress, int color) {
        int h = 7;
        progress = Math.max(0f, Math.min(1f, progress));

        g.fill(x, y, x + w, y + h / 2, BAR_BG_TOP);
        g.fill(x, y + h / 2, x + w, y + h, BAR_BG_BOT);

        int fillW = (int) (w * progress);
        g.fill(x, y, x + fillW, y + h, color);
        g.fill(x, y, x + fillW, y + 1, lighten(color, 0x40));
        g.fill(x, y + h - 1, x + fillW, y + h, darken(color, 0x40));

        drawBorder(g, x, y, w, h, 0xFF1A2A3C);
    }

    // ================= 工具 =================
    private static void drawRow(GuiGraphics g, Minecraft mc, int x, int y, int rightEdge,
                                String label, String value, int valueColor) {
        g.drawString(mc.font, label, x, y, LABEL, false);
        int vW = mc.font.width(value);
        g.drawString(mc.font, value, rightEdge - vW, y, valueColor, false);
    }

    private static String formatTicks(int ticks) {
        if (ticks <= 0) return "0s";
        float sec = ticks / 20f;
        if (sec >= 60f) {
            int m = (int) (sec / 60);
            int s = (int) (sec % 60);
            return m + "m " + s + "s";
        }
        return String.format("%.1fs", sec);
    }

    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int lighten(int color, int amount) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, ((color >>> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >>> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int darken(int color, int amount) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.max(0, ((color >>> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >>> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}