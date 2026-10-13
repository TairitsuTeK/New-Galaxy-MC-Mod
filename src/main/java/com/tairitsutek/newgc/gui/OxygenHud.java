package com.tairitsutek.newgc.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.event.oxygen.ModAttachments;
import com.tairitsutek.newgc.event.oxygen.OxygenData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class OxygenHud {

    private static final int BAR_WIDTH = 26;
    private static final int BAR_HEIGHT = 110;
    private static final int BAR_GAP = 6;
    private static final int MARGIN_RIGHT = 30;
    private static final int MARGIN_BOTTOM = 70;

    private static final int COLOR_BG_OUTER   = 0x40001824;
    private static final int COLOR_BG_INNER   = 0xB3000D14;
    private static final int COLOR_BORDER     = 0xB300B8D0;
    private static final int COLOR_CORNER     = 0xB300D0E0;
    private static final int COLOR_TICK_MAJOR = 0x5000B8D0;
    private static final int COLOR_GRID       = 0x18FFFFFF;

    private static final int COLOR_FILL_HIGH  = 0xB300B8D0;
    private static final int COLOR_FILL_MID   = 0xB3CC9000;
    private static final int COLOR_FILL_LOW   = 0xB3CC1030;
    private static final int COLOR_GLOW_HIGH  = 0x5000B8D0;
    private static final int COLOR_GLOW_MID   = 0x50CC9000;
    private static final int COLOR_GLOW_LOW   = 0x50CC1030;

    private static final int COLOR_TEXT_MAIN  = 0xCC00D8E8;
    private static final int COLOR_TEXT_VALUE = 0x9980D0E0;
    private static final int COLOR_TEXT_HINT  = 0x8060A0B0;

    private static final long PULSE_PERIOD_MS = 1500;
    private static final long SCAN_PERIOD_MS = 2000;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;

        OxygenData data = mc.player.getData(ModAttachments.OXYGEN.get());

        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int totalWidth = BAR_WIDTH * 2 + BAR_GAP;
        int baseX = screenWidth - MARGIN_RIGHT - totalWidth;
        int baseY = screenHeight - MARGIN_BOTTOM - BAR_HEIGHT;

        drawTitle(g, mc, baseX, baseY, totalWidth);

        int x1 = baseX;
        drawBar(g, mc, x1, baseY, data.getOxygen(), data.getBaseMax(), "BASE", true, false);

        int x2 = baseX + BAR_WIDTH + BAR_GAP;
        boolean hasTank = data.getTankMax() > 0;
        drawBar(g, mc, x2, baseY, data.getTankOxygen(), data.getTankMax(), "TANK", false, true);

        drawStatusBar(g, mc, baseX, baseY + BAR_HEIGHT + 4, totalWidth, data, hasTank);
    }

    private static void drawTitle(GuiGraphics g, Minecraft mc, int x, int y, int width) {
        String title = "◤ OXYGEN ◢";
        int tw = mc.font.width(title);
        int tx = x + width / 2 - tw / 2;
        int ty = y - 34;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                g.drawString(mc.font, title, tx + dx, ty + dy, 0x3000B8D0, false);
            }
        }
        g.drawString(mc.font, title, tx, ty, COLOR_TEXT_MAIN, true);

        int lineY = ty + 12;
        g.fill(x, lineY, x + width, lineY + 1, 0x6000B8D0);
        g.fill(x - 1, lineY - 1, x + 3, lineY + 2, COLOR_CORNER);
        g.fill(x + width - 3, lineY - 1, x + width + 1, lineY + 2, COLOR_CORNER);
    }

    private static void drawBar(GuiGraphics g, Minecraft mc, int x, int y,
                                int current, int max, String label,
                                boolean pulse, boolean showTicks) {
        float pct = max <= 0 ? 0.0f : (float) current / max;

        if (pulse && pct > 0) {
            long time = System.currentTimeMillis();
            double phase = (time % PULSE_PERIOD_MS) / (double) PULSE_PERIOD_MS;
            float pulseFactor = (float) (0.5 + 0.5 * Math.sin(phase * Math.PI * 2));

            int glowColor;
            if (pct > 0.5f) glowColor = applyPulse(COLOR_GLOW_HIGH, pulseFactor);
            else if (pct > 0.25f) glowColor = applyPulse(COLOR_GLOW_MID, pulseFactor);
            else glowColor = applyPulse(COLOR_GLOW_LOW, pulseFactor);

            g.fill(x - 3, y - 3, x + BAR_WIDTH + 3, y + BAR_HEIGHT + 3, glowColor);
        }

        g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BG_OUTER);
        g.fill(x + 1, y + 1, x + BAR_WIDTH - 1, y + BAR_HEIGHT - 1, COLOR_BG_INNER);

        for (int i = 1; i < 8; i++) {
            int gridY = y + (BAR_HEIGHT * i / 8);
            g.fill(x + 1, gridY, x + BAR_WIDTH - 1, gridY + 1, COLOR_GRID);
        }
        g.fill(x + BAR_WIDTH / 2, y + 1, x + BAR_WIDTH / 2 + 1, y + BAR_HEIGHT - 1, COLOR_GRID);

        int fillHeight = (int) (BAR_HEIGHT * pct);
        if (fillHeight > 0) {
            int fillTop = y + BAR_HEIGHT - fillHeight;

            int fillColor;
            if (pct > 0.5f) fillColor = COLOR_FILL_HIGH;
            else if (pct > 0.25f) fillColor = COLOR_FILL_MID;
            else fillColor = COLOR_FILL_LOW;

            g.fill(x + 2, fillTop, x + BAR_WIDTH - 2, y + BAR_HEIGHT - 2, fillColor);
            g.fill(x + 2, fillTop, x + BAR_WIDTH - 2, fillTop + 1, 0x80FFFFFF);

            for (int i = fillTop + 3; i < y + BAR_HEIGHT - 2; i += 4) {
                g.fill(x + 3, i, x + BAR_WIDTH - 3, i + 1, 0x18000000);
            }

            long time = System.currentTimeMillis();
            double scanPhase = (time % SCAN_PERIOD_MS) / (double) SCAN_PERIOD_MS;
            int scanY = fillTop + (int) ((1.0 - scanPhase) * fillHeight);
            if (scanY > fillTop && scanY < y + BAR_HEIGHT - 2) {
                g.fill(x + 2, scanY, x + BAR_WIDTH - 2, scanY + 1, 0x80FFFFFF);
            }
        }

        g.fill(x, y, x + BAR_WIDTH, y + 1, COLOR_BORDER);
        g.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BORDER);
        g.fill(x, y, x + 1, y + BAR_HEIGHT, COLOR_BORDER);
        g.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_BORDER);

        int corner = 3;
        g.fill(x, y, x + corner, y + 2, COLOR_CORNER);
        g.fill(x, y, x + 2, y + corner, COLOR_CORNER);
        g.fill(x + BAR_WIDTH - corner, y, x + BAR_WIDTH, y + 2, COLOR_CORNER);
        g.fill(x + BAR_WIDTH - 2, y, x + BAR_WIDTH, y + corner, COLOR_CORNER);
        g.fill(x, y + BAR_HEIGHT - 2, x + corner, y + BAR_HEIGHT, COLOR_CORNER);
        g.fill(x, y + BAR_HEIGHT - corner, x + 2, y + BAR_HEIGHT, COLOR_CORNER);
        g.fill(x + BAR_WIDTH - corner, y + BAR_HEIGHT - 2, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_CORNER);
        g.fill(x + BAR_WIDTH - 2, y + BAR_HEIGHT - corner, x + BAR_WIDTH, y + BAR_HEIGHT, COLOR_CORNER);

        for (int i = 0; i <= 4; i++) {
            int tickY = y + (BAR_HEIGHT * i / 4);
            int tickLen = (i == 0 || i == 4) ? BAR_WIDTH - 2 : 5;
            g.fill(x + 1, tickY, x + 1 + tickLen, tickY + 1, COLOR_TICK_MAJOR);
            g.fill(x + BAR_WIDTH - 1 - tickLen, tickY, x + BAR_WIDTH - 1, tickY + 1, COLOR_TICK_MAJOR);
        }

        if (showTicks) {
            for (int i = 0; i <= 4; i++) {
                int tickY = y + (BAR_HEIGHT * i / 4);
                int percent = (4 - i) * 25;
                String pctText = percent + "%";
                g.drawString(mc.font, pctText,
                        x + BAR_WIDTH + 5, tickY - 4,
                        0x6000B8D0, false);
            }
        }

        int bw = mc.font.width(label);
        int bx = x + BAR_WIDTH / 2 - bw / 2;
        int by = y - 18;

        g.fill(bx - 2, by - 1, bx + bw + 2, by + 9, 0xB3000D14);
        g.fill(bx - 2, by - 1, bx + bw + 2, by, COLOR_BORDER);
        g.fill(bx - 2, by + 8, bx + bw + 2, by + 9, COLOR_BORDER);

        g.drawString(mc.font, label, bx, by, COLOR_TEXT_MAIN, true);

        String valueText = String.valueOf(current);
        int vw = mc.font.width(valueText);
        int vx = x + BAR_WIDTH / 2 - vw / 2;
        int vy = y + BAR_HEIGHT + 5;

        g.fill(vx - 2, vy - 1, vx + vw + 2, vy + 9, 0xB3000D14);
        g.fill(vx - 2, vy - 1, vx + vw + 2, vy, COLOR_BORDER);
        g.fill(vx - 2, vy + 8, vx + vw + 2, vy + 9, COLOR_BORDER);

        g.drawString(mc.font, valueText, vx, vy, COLOR_TEXT_VALUE, true);
    }

    private static void drawStatusBar(GuiGraphics g, Minecraft mc, int x, int y, int width, OxygenData data, boolean hasTank) {
        int statusY = y + 20;

        g.fill(x, statusY, x + width, statusY + 1, 0x6000B8D0);

        String symbol;
        int statusColor;

        int total = data.getOxygen() + data.getTankOxygen();
        int totalMax = data.getBaseMax() + data.getTankMax();
        float pct = totalMax <= 0 ? 0.0f : (float) total / totalMax;

        if (pct > 0.5f) {
            symbol = "●";
            statusColor = 0xCC00CC70;
        } else if (pct > 0.25f) {
            symbol = "▲";
            statusColor = 0xCCCC9000;
        } else if (pct > 0) {
            symbol = "◆";
            statusColor = 0xCCCC5000;
        } else {
            symbol = "✖";
            statusColor = 0xCCCC1030;
        }

        int totalPct = (int) (pct * 100);
        String status = symbol + " " + totalPct + "%";

        g.drawString(mc.font, status, x + width / 2 - mc.font.width(status) / 2, statusY + 4, statusColor, true);

        if (!hasTank) {
            String hint = "← 使用氧气罐扩展";
            int hw = mc.font.width(hint);
            g.drawString(mc.font, hint,
                    x + width / 2 - hw / 2, statusY + 16,
                    COLOR_TEXT_HINT, false);
        }
    }

    private static int applyPulse(int argb, float pulseFactor) {
        int a = (argb >> 24) & 0xFF;
        int newA = (int) (a * (0.5f + 0.5f * pulseFactor));
        return (newA << 24) | (argb & 0x00FFFFFF);
    }
}