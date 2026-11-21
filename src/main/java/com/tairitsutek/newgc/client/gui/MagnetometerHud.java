package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.client.magnetometer.MagnetometerClientCache;
import com.tairitsutek.newgc.client.magnetometer.MagnetometerData;
import com.tairitsutek.newgc.item.ToolsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MagnetometerHud {

    // ===== 布局 =====
    private static final int WIDTH  = 150;
    private static final int HEIGHT = 66;
    private static final int MARGIN_LEFT   = 10;
    private static final int MARGIN_BOTTOM = 90;

    // ===== 主色 =====
    private static final int COLOR_BG          = 0xD0081420;
    private static final int COLOR_BORDER      = 0xFF3AA0C8;
    private static final int COLOR_ACCENT      = 0xFF4AD8FF;
    private static final int COLOR_GLOW        = 0x403AA0C8;
    private static final int COLOR_TITLE       = 0xFFA0F0FF;
    private static final int COLOR_LABEL       = 0xFF8898B0;
    private static final int COLOR_VALUE       = 0xFFE0F8FF;
    private static final int COLOR_SEPARATOR   = 0x604AD8FF;
    private static final int COLOR_TITLE_BG    = 0xFF0A1A2A;

    private static final int COLOR_BAR_BG      = 0xFF060C14;
    private static final int COLOR_BAR_BORDER  = 0xFF1A2A3C;

    private static final int COLOR_STRENGTH_WEAK    = 0xFF4A5A6E;
    private static final int COLOR_STRENGTH_LOW     = 0xFF7A8FA6;
    private static final int COLOR_STRENGTH_MID     = 0xFF5A9BD0;
    private static final int COLOR_STRENGTH_HIGH    = 0xFFE0A050;
    private static final int COLOR_STRENGTH_EXTREME = 0xFFFF5B3A;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.options.hideGui) return;

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off  = mc.player.getOffhandItem();
        boolean holding = main.is(ToolsItem.MAGNETOMETER.get())
                || off.is(ToolsItem.MAGNETOMETER.get());
        if (!holding) return;

        MagnetometerClientCache.tick();

        GuiGraphics g = event.getGuiGraphics();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int x0 = MARGIN_LEFT;
        int y0 = screenH - HEIGHT - MARGIN_BOTTOM;
        int x1 = x0 + WIDTH;
        int y1 = y0 + HEIGHT;

        g.pose().pushPose();
        g.pose().last().pose().identity();

        UiTheme.panel(g, x0, y0, WIDTH, HEIGHT);

        // ===== 标题行（0~12）=====
        g.fillGradient(x0 + 1, y0 + 1, x1 - 1, y0 + 12,
                0x301D4560, 0x001D4560);
        g.fill(x0 + 1, y0 + 11, x1 - 1, y0 + 12, COLOR_SEPARATOR);

        g.drawString(mc.font, Component.translatable("gui.newgc.magnetometer.title").getString(),
                x0 + 6, y0 + 3, UiTheme.TEXT, false);

        float strength = MagnetometerClientCache.getStrength();

        // 强度等级（右上角，短文字）
        String level = MagnetometerData.levelText(strength);
        int lvlW = mc.font.width(level);
        g.drawString(mc.font, level, x1 - 6 - lvlW, y0 + 3,
                MagnetometerClientCache.getStatusColor(), false);

        // ===== 强度条（14~21）=====
        int barX = x0 + 6;
        int barY = y0 + 14;
        int barW = WIDTH - 12;
        int barH = 7;

        g.fill(barX, barY, barX + barW, barY + barH, COLOR_BAR_BG);

        float t = MagnetometerData.normalize(strength);
        int fillW = (int) (barW * t);
        int fillColor = strengthColor(strength);

        if (fillW > 0) {
            g.fill(barX, barY, barX + fillW, barY + barH, fillColor);
            g.fill(barX, barY, barX + fillW, barY + 1, lighten(fillColor, 0x40));
            g.fill(barX, barY + barH - 1, barX + fillW, barY + barH, darken(fillColor, 0x40));
        }

        // 刻度
        for (int i = 1; i <= 5; i++) {
            int tickX = barX + barW * i / 6;
            g.fill(tickX, barY, tickX + 1, barY + barH, 0x50FFFFFF);
        }

        drawRect2(g, barX, barY, barX + barW, barY + barH, COLOR_BAR_BORDER);

        // ===== 数值行（24~32）=====
        int valueY = barY + barH + 3;
        String strengthStr = String.format("%.2f", strength);
        g.drawString(mc.font, strengthStr, barX, valueY, COLOR_VALUE, false);

        // 右侧显示"噪声"（小字）
        String noiseStr = String.format("n=%.2f", MagnetometerClientCache.getNoise());
        int noiseW = mc.font.width(noiseStr);
        g.drawString(mc.font, noiseStr, barX + barW - noiseW, valueY, COLOR_LABEL, false);

        // ===== 下半部分（36~64）=====
        int halfY = y0 + 36;

        // 罗盘（半径缩到 11）
        int compassCX = x0 + 20;
        int compassCY = halfY + 10;
        int compassR = 11;
        drawCompassRose(g, compassCX, compassCY, compassR);

        // 右侧文字
        int infoX = x0 + 42;
        int infoY = halfY;
        int infoRight = x1 - 6;

        // 朝向
        float angle = MagnetometerClientCache.getAngle();
        String arrow = angleToArrow(angle);
        String angleStr = String.format("%s %d°", arrow, (int) angle);
        drawRow(g, mc, infoX, infoY, infoRight,
                Component.translatable("gui.newgc.magnetometer.heading").getString(),
                angleStr, COLOR_VALUE);
        infoY += 11;

        // 状态
        drawRow(g, mc, infoX, infoY, infoRight,
                Component.translatable("gui.newgc.magnetometer.status").getString(),
                MagnetometerClientCache.getStatus(),
                MagnetometerClientCache.getStatusColor());

        // 矿脉提示（如果检测到，放最下方一行）
        if (MagnetometerClientCache.isIronNearby()) {
            g.drawString(mc.font, Component.translatable(
                            "gui.newgc.magnetometer.iron_nearby").getString(),
                    infoX, infoY + 12, 0xFFC89650, false);
        }

        g.pose().popPose();
    }

    // ================= 罗盘（简化版）=================
    private static void drawCompassRose(GuiGraphics g, int cx, int cy, int r) {
        Minecraft mc = Minecraft.getInstance();

        // 外圈
        drawCircleOutline(g, cx, cy, r, COLOR_BORDER);

        // 十字线
        g.fill(cx - r, cy, cx + r, cy + 1, 0x40FFFFFF);
        g.fill(cx, cy - r, cx + 1, cy + r, 0x40FFFFFF);

        // 方位标记（用点代替字母，更紧凑）
        g.fill(cx - 1, cy - r - 2, cx + 1, cy - r, 0xFFFF8080);  // N（红点）
        g.fill(cx - 1, cy + r, cx + 1, cy + r + 2, 0xFF8898B0);  // S（灰点）
        g.fill(cx - r - 2, cy - 1, cx - r, cy + 1, 0xFF8898B0);  // W（灰点）
        g.fill(cx + r, cy - 1, cx + r + 2, cy + 1, 0xFF8898B0);  // E（灰点）

        // 指针
        float angle = MagnetometerClientCache.getAngle();
        double rad = Math.toRadians(angle - 90);
        int px = cx + (int) (Math.cos(rad) * (r - 3));
        int py = cy + (int) (Math.sin(rad) * (r - 3));
        drawLine(g, cx, cy, px, py, 0xFFFF4040);

        // 中心点
        g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    // ================= 工具 =================
    private static void drawRow(GuiGraphics g, Minecraft mc, int x, int y, int rightEdge,
                                String label, String value, int valueColor) {
        g.drawString(mc.font, label, x, y, COLOR_LABEL, false);
        int maxValueW = Math.max(12, rightEdge - x - mc.font.width(label) - 4);
        String shown = mc.font.width(value) <= maxValueW
                ? value
                : mc.font.plainSubstrByWidth(value, Math.max(0, maxValueW - 4)) + "…";
        int vW = mc.font.width(shown);
        g.drawString(mc.font, shown, rightEdge - vW, y, valueColor, false);
    }

    private static void drawRect2(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private static void drawCornerBrackets(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int s = 6, t = 2;
        g.fill(x1, y1, x1 + s, y1 + t, color);
        g.fill(x1, y1, x1 + t, y1 + s, color);
        g.fill(x2 - s, y1, x2, y1 + t, color);
        g.fill(x2 - t, y1, x2, y1 + s, color);
        g.fill(x1, y2 - t, x1 + s, y2, color);
        g.fill(x1, y2 - s, x1 + t, y2, color);
        g.fill(x2 - s, y2 - t, x2, y2, color);
        g.fill(x2 - t, y2 - s, x2, y2, color);
    }

    private static void drawCircleOutline(GuiGraphics g, int cx, int cy, int r, int color) {
        int segments = 48;
        for (int i = 0; i < segments; i++) {
            double t0 = (i / (double) segments) * Math.PI * 2;
            int px = cx + (int) Math.round(Math.cos(t0) * r);
            int py = cy + (int) Math.round(Math.sin(t0) * r);
            g.fill(px, py, px + 1, py + 1, color);
        }
    }

    private static void drawLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            g.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx)  { err += dx; y0 += sy; }
        }
    }

    private static int strengthColor(float s) {
        if (s < 0.05f) return COLOR_STRENGTH_WEAK;
        if (s < 0.5f)  return COLOR_STRENGTH_LOW;
        if (s < 1.5f)  return COLOR_STRENGTH_MID;
        if (s < 2.2f)  return COLOR_STRENGTH_HIGH;
        return COLOR_STRENGTH_EXTREME;
    }

    private static String angleToArrow(float angle) {
        int sector = (int) ((angle + 22.5f) / 45f) % 8;
        if (sector < 0) sector += 8;
        return switch (sector) {
            case 0 -> "↑";
            case 1 -> "↗";
            case 2 -> "→";
            case 3 -> "↘";
            case 4 -> "↓";
            case 5 -> "↙";
            case 6 -> "←";
            default -> "↖";
        };
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