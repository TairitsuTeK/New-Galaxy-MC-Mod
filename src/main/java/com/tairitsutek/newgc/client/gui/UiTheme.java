package com.tairitsutek.newgc.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 模组统一的深空界面样式。
 * <p>
 * 深蓝黑玻璃面板、细冰蓝描边、低对比度分段渐变；
 * 不使用小格网和满屏文字，减少像素感和文字/槽位遮挡。
 */
public final class UiTheme {

    public static final int BG_TOP = 0xFF08111C;
    public static final int BG_BOTTOM = 0xFF03070C;
    public static final int PANEL_TOP = 0xE00C1724;
    public static final int PANEL_BOTTOM = 0xE0060D15;
    public static final int PANEL_BORDER = 0x805D7E93;
    public static final int PANEL_GLOW = 0x3078D8F0;
    public static final int ACCENT = 0xFF78D8F0;
    public static final int ACCENT_SOFT = 0x6078D8F0;
    public static final int ACCENT_DIM = 0x2878D8F0;
    public static final int TEXT = 0xFFE7F4FA;
    public static final int MUTED = 0xFF8199AB;
    public static final int SLOT_BG = 0xFF071019;
    public static final int SLOT_BORDER = 0xFF263B4B;
    public static final int SLOT_HIGHLIGHT = 0x405E8FA8;
    public static final int GOOD = 0xFF66D79B;
    public static final int WARN = 0xFFE2AB62;

    private UiTheme() {
    }

    /** 整个机器界面的背景。 */
    public static void screen(GuiGraphics g, int x, int y, int w, int h) {
        g.fillGradient(x, y, x + w, y + h, BG_TOP, BG_BOTTOM);
        g.fillGradient(x, y, x + w, y + Math.min(44, h / 3), 0x281F4C62, 0x001F4C62);
        g.fillGradient(x, y + h - Math.min(44, h / 3), x + w, y + h, 0x00000000, 0x40101824);
        g.fill(x, y, x + w, y + 1, ACCENT_SOFT);
        g.fill(x, y + h - 1, x + w, y + h, 0x405D7E93);
        g.fill(x, y, x + 1, y + h, 0x305D7E93);
        g.fill(x + w - 1, y, x + w, y + h, 0x305D7E93);
        cornerBrackets(g, x, y, x + w, y + h, ACCENT_DIM);
    }

    /** 星图/信息面板使用的玻璃卡片。 */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, PANEL_GLOW);
        g.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOTTOM);
        g.fill(x, y, x + w, y + 1, PANEL_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, 0x405D7E93);
        g.fill(x, y, x + 1, y + h, 0x405D7E93);
        g.fill(x + w - 1, y, x + w, y + h, 0x405D7E93);
        g.fillGradient(x + 1, y + 1, x + w - 1, Math.min(y + h - 1, y + MIN_HEADER_H),
                0x301D4560, 0x001D4560);
        cornerBrackets(g, x, y, x + w, y + h, ACCENT);
    }

    private static final int MIN_HEADER_H = 24;

    /**
     * 绘制原版 18x18 槽位底框。
     * <p>{@code sx/sy} 是物品图标的左上角，和 {@code Slot} 的坐标完全一致；
     * 底框必须从 {@code sx-1/sy-1} 开始，否则会和真实槽位错开 1 像素。
     */
    public static void slot(GuiGraphics g, int sx, int sy) {
        g.fill(sx - 2, sy - 2, sx + 18, sy + 18, 0x302A4558);
        g.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BG);
        g.fill(sx - 1, sy - 1, sx + 17, sy, SLOT_HIGHLIGHT);
        g.fill(sx - 1, sy - 1, sx, sy + 17, SLOT_HIGHLIGHT);
        g.fill(sx - 1, sy + 16, sx + 17, sy + 17, SLOT_BORDER);
        g.fill(sx + 16, sy - 1, sx + 17, sy + 17, SLOT_BORDER);
    }

    /** 竖直能量/流体条，数值只通过填充高度表现，不绘制文字。 */
    public static void verticalBar(GuiGraphics g, int x, int y, int w, int h,
                                   int stored, int max, int lowColor, int highColor) {
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, ACCENT_DIM);
        g.fill(x, y, x + w, y + h, 0xF0060D14);
        if (max > 0 && stored > 0) {
            int fillH = Math.max(1, Math.min(h - 4,
                    (int) ((long) stored * (h - 4) / max)));
            int top = y + h - 2 - fillH;
            for (int i = 0; i < fillH; i++) {
                float t = i / (float) Math.max(1, fillH - 1);
                int color = lerpColor(lowColor, highColor, t);
                g.fill(x + 2, top + i, x + w - 2, top + i + 1, color);
            }
        }
        g.fill(x, y, x + w, y + 1, PANEL_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER);
        g.fill(x, y, x + 1, y + h, PANEL_BORDER);
        g.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER);
    }

    /** 水平进度条，用于熔炼/压缩/合金进度。 */
    public static void progressBar(GuiGraphics g, int x, int y, int w, int h,
                                   int progress, int max, int lowColor, int highColor) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x302A4558);
        g.fill(x, y, x + w, y + h, 0xF0060D14);
        if (max > 0 && progress > 0) {
            int fillW = Math.max(1, Math.min(w - 2,
                    (int) ((long) progress * (w - 2) / max)));
            for (int i = 0; i < fillW; i++) {
                float t = i / (float) Math.max(1, fillW - 1);
                g.fill(x + 1 + i, y + 1, x + 1 + i + 1, y + h - 1,
                        lerpColor(lowColor, highColor, t));
            }
        }
        g.fill(x, y, x + w, y + 1, PANEL_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER);
        g.fill(x, y, x + 1, y + h, PANEL_BORDER);
        g.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER);
    }

    public static void cornerBrackets(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int s = 10;
        g.fill(x1, y1, x1 + s, y1 + 1, color);
        g.fill(x1, y1, x1 + 1, y1 + s, color);
        g.fill(x2 - s, y1, x2, y1 + 1, color);
        g.fill(x2 - 1, y1, x2, y1 + s, color);
        g.fill(x1, y2 - 1, x1 + s, y2, color);
        g.fill(x1, y2 - s, x1 + 1, y2, color);
        g.fill(x2 - s, y2 - 1, x2, y2, color);
        g.fill(x2 - 1, y2 - s, x2, y2, color);
    }

    public static int lerpColor(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = (int) (((from >>> 24) & 0xFF) * (1 - t) + ((to >>> 24) & 0xFF) * t);
        int r = (int) (((from >>> 16) & 0xFF) * (1 - t) + ((to >>> 16) & 0xFF) * t);
        int g = (int) (((from >>> 8) & 0xFF) * (1 - t) + ((to >>> 8) & 0xFF) * t);
        int b = (int) ((from & 0xFF) * (1 - t) + (to & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
