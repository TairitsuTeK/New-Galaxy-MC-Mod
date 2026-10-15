package com.tairitsutek.newgc.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormClientCache;
import com.tairitsutek.newgc.Planets.mars.client.MarsVolcanoHandler;
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

    private static final int WIDTH = 200;
    private static final int PADDING = 8;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 14;
    private static final int CARD_GAP = 6;
    private static final int MARGIN = 8;
    private static final int CARD_INNER = ROW_H * 3 + PADDING * 2;

    private static final int BG          = 0xC0101820;
    private static final int BORDER      = 0xFF2F4157;
    private static final int CARD_BG     = 0x60142030;
    private static final int TITLE       = 0xFF9BB7E0;
    private static final int LABEL       = 0xFFA0B6D0;
    private static final int VALUE       = 0xFFD0E2FF;
    private static final int JUPITER_DOT = 0xFFE09F4B;
    private static final int MARS_DOT    = 0xFFE06B4B;
    private static final int BAR_BG      = 0xFF0F1A28;

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

        int x = screenW - WIDTH - MARGIN;
        int y = MARGIN;

        int cardCount = (inJupiter ? 1 : 0) + (inMars ? 1 : 0);
        int totalH = HEADER_H + cardCount * (CARD_INNER + CARD_GAP) + PADDING;

        g.fill(x, y, x + WIDTH, y + totalH, BG);
        drawBorder(g, x, y, WIDTH, totalH, BORDER);

        String planetName = inJupiter ? "木星 · 风暴" : "火星 · 火山";
        g.drawString(mc.font, "🪐 星球状态", x + PADDING, y + 5, TITLE, false);
        int nameW = mc.font.width(planetName);
        g.drawString(mc.font, planetName, x + WIDTH - PADDING - nameW, y + 5, TITLE, false);

        int cy = y + HEADER_H;
        if (inJupiter) cy = drawJupiterCard(g, mc, x, cy);
        if (inMars)    cy = drawMarsCard(g, mc, x, cy);
    }

    // ================= 木星卡片（从客户端缓存读）=================
    private static int drawJupiterCard(GuiGraphics g, Minecraft mc, int x, int y) {
        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_INNER, CARD_BG);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_INNER, JUPITER_DOT);

        int tx = x + PADDING + 8;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, "■ 木星风暴", tx, ty, TITLE, false);

        JupiterStormClientCache.Phase phase = JupiterStormClientCache.getPhase();
        String phaseName = phaseName(phase);
        int pW = mc.font.width(phaseName);
        g.drawString(mc.font, phaseName, rightEdge - pW, ty, JUPITER_DOT, false);
        ty += ROW_H;

        int remain = JupiterStormClientCache.getPhaseTimer();
        drawRow(g, mc, tx, ty, rightEdge, "阶段剩余", formatTicks(remain), VALUE);
        ty += ROW_H;

        float strength = JupiterStormClientCache.getStormStrength();
        String sStr = String.format("%.2f", strength);
        drawRow(g, mc, tx, ty, rightEdge, "风暴强度", sStr, VALUE);

        int barW = 60;
        int barH = 8;
        int barX = rightEdge - barW;
        int barY = ty + 3;
        g.fill(barX, barY, barX + barW, barY + barH, BAR_BG);

        float t = Math.max(0f, Math.min(1f, strength / 2.5f));
        int fillW = (int) (barW * t);
        int barColor = strength > 1.5f ? 0xFFFF6B4B
                : strength > 0.8f ? 0xFFFFB86B
                  : JUPITER_DOT;
        g.fill(barX, barY, barX + fillW, barY + barH, barColor);

        return y + CARD_INNER + CARD_GAP;
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
        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_INNER, CARD_BG);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_INNER, MARS_DOT);

        int tx = x + PADDING + 8;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        int eruption = MarsVolcanoHandler.getEruptionTicks();
        int cooldown = MarsVolcanoHandler.getCooldownTimer();
        boolean erupting = eruption > 0;

        g.drawString(mc.font, "■ 火星火山", tx, ty, TITLE, false);
        String state = erupting ? "喷发中" : "冷却中";
        int sW = mc.font.width(state);
        g.drawString(mc.font, state, rightEdge - sW, ty, erupting ? MARS_DOT : LABEL, false);
        ty += ROW_H;

        if (erupting) {
            drawRow(g, mc, tx, ty, rightEdge, "喷发剩余", formatTicks(eruption), VALUE);
            ty += ROW_H;
            drawRow(g, mc, tx, ty, rightEdge, "本次总时长",
                    formatTicks(MarsVolcanoHandler.getEruptionTotalTicks()), VALUE);
        } else {
            drawRow(g, mc, tx, ty, rightEdge, "冷却剩余", formatTicks(cooldown), VALUE);
            ty += ROW_H;

            BlockPos vp = MarsVolcanoHandler.getVolcanoPos();
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
}