package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.OxygenExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class OxygenExtractorScreen extends AbstractContainerScreen<OxygenExtractorMenu> {

    private static final int ENERGY_X = 10;
    private static final int ENERGY_Y = 20;
    private static final int ENERGY_W = 16;
    private static final int ENERGY_H = 46;

    private static final int OXYGEN_X = 150;
    private static final int OXYGEN_Y = 20;
    private static final int OXYGEN_W = 16;
    private static final int OXYGEN_H = 46;

    public OxygenExtractorScreen(OxygenExtractorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 背景
        for (int i = 0; i < imageHeight; i++) {
            float t = (float) i / imageHeight;
            int r = (int) (8 + 20 * t);
            int gr = (int) (20 + 30 * t);
            int b = (int) (25 + 20 * t);
            g.fill(x, y + i, x + imageWidth, y + i + 1, 0xFF000000 | (r << 16) | (gr << 8) | b);
        }

        // 网格
        for (int gx = 0; gx < imageWidth; gx += 8) {
            g.fill(x + gx, y, x + gx + 1, y + imageHeight, 0x10FFFFFF);
        }
        for (int gy = 0; gy < imageHeight; gy += 8) {
            g.fill(x, y + gy, x + imageWidth, y + gy + 1, 0x10FFFFFF);
        }

        // 边框
        g.fill(x, y, x + imageWidth, y + 2, 0xFF00FF88);
        g.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF00FF88);
        g.fill(x, y, x + 2, y + imageHeight, 0xFF00FF88);
        g.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF00FF88);

        // 四角
        int corner = 8;
        g.fill(x, y, x + corner, y + 4, 0xFF00FFCC);
        g.fill(x, y, x + 4, y + corner, 0xFF00FFCC);
        g.fill(x + imageWidth - corner, y, x + imageWidth, y + 4, 0xFF00FFCC);
        g.fill(x + imageWidth - 4, y, x + imageWidth, y + corner, 0xFF00FFCC);
        g.fill(x, y + imageHeight - 4, x + corner, y + imageHeight, 0xFF00FFCC);
        g.fill(x, y + imageHeight - corner, x + 4, y + imageHeight, 0xFF00FFCC);
        g.fill(x + imageWidth - corner, y + imageHeight - 4, x + imageWidth, y + imageHeight, 0xFF00FFCC);
        g.fill(x + imageWidth - 4, y + imageHeight - corner, x + imageWidth, y + imageHeight, 0xFF00FFCC);

        // 植物槽
        int slotX = x + 79;
        int slotY = y + 34;
        g.fill(slotX - 2, slotY - 2, slotX + 20, slotY + 20, 0x4000FF88);
        g.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF0A1818);
        g.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF00FF88);
        g.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF00FF88);
        g.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF00FF88);
        g.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF00FF88);

        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + 83 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1818);
                g.fill(sx, sy, sx + 18, sy + 1, 0x6000FF88);
                g.fill(sx, sy + 17, sx + 18, sy + 18, 0x6000FF88);
                g.fill(sx, sy, sx + 1, sy + 18, 0x6000FF88);
                g.fill(sx + 17, sy, sx + 18, sy + 18, 0x6000FF88);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = y + 141;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1818);
            g.fill(sx, sy, sx + 18, sy + 1, 0x6000FF88);
            g.fill(sx, sy + 17, sx + 18, sy + 18, 0x6000FF88);
            g.fill(sx, sy, sx + 1, sy + 18, 0x6000FF88);
            g.fill(sx + 17, sy, sx + 18, sy + 18, 0x6000FF88);
        }

        // 能量条
        drawBar(g, x + ENERGY_X, y + ENERGY_Y, ENERGY_W, ENERGY_H,
                menu.getEnergyStored(), menu.getMaxEnergyStored(),
                0xFF00FF88, 0x3000FF88, 0x8000FF88, true);

        // 氧气条
        drawBar(g, x + OXYGEN_X, y + OXYGEN_Y, OXYGEN_W, OXYGEN_H,
                menu.getOxygenBuffer(), menu.getOxygenBufferMax(),
                0xFF00FFFF, 0x3000FFFF, 0x8000FFFF, false);

        // 图标
        g.drawString(font, "⚡", x + ENERGY_X + 2, y + 8, 0xFF00FF88, true);
        g.drawString(font, "O₂", x + OXYGEN_X + 2, y + 8, 0xFF00FFFF, true);
    }

    private void drawBar(GuiGraphics g, int x, int y, int w, int h,
                         int stored, int max,
                         int borderColor, int glowColor, int tickColor,
                         boolean tickLeft) {
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, glowColor);
        g.fill(x, y, x + w, y + h, 0xFF0A1818);

        for (int i = 1; i < 6; i++) {
            int gy = y + (h * i / 6);
            g.fill(x + 1, gy, x + w - 1, gy + 1, 0x20000000 | (borderColor & 0x00FFFFFF));
        }

        if (max > 0 && stored > 0) {
            int fillH = (int) ((float) stored / max * (h - 4));
            int fillY = y + h - 2 - fillH;
            int baseR = (borderColor >> 16) & 0xFF;
            int baseG = (borderColor >> 8) & 0xFF;
            int baseB = borderColor & 0xFF;
            for (int i = 0; i < fillH; i++) {
                float t = (float) i / fillH;
                int r = (int) (baseR * (0.4f + 0.6f * t));
                int gr = (int) (baseG * (0.4f + 0.6f * t));
                int b = (int) (baseB * (0.4f + 0.6f * t));
                g.fill(x + 2, fillY + i, x + w - 2, fillY + i + 1,
                        0xFF000000 | (r << 16) | (gr << 8) | b);
            }
            g.fill(x + 2, fillY, x + w - 2, fillY + 1, 0xFFFFFFFF);
        }

        g.fill(x, y, x + w, y + 1, borderColor);
        g.fill(x, y + h - 1, x + w, y + h, borderColor);
        g.fill(x, y, x + 1, y + h, borderColor);
        g.fill(x + w - 1, y, x + w, y + h, borderColor);

        for (int i = 0; i <= 4; i++) {
            int ty = y + (h * i / 4);
            if (tickLeft) {
                g.fill(x - 2, ty, x, ty + 1, tickColor);
            } else {
                g.fill(x + w, ty, x + w + 2, ty + 1, tickColor);
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 悬停能量条
        if (isHovering(mouseX, mouseY, x + ENERGY_X, y + ENERGY_Y, ENERGY_W, ENERGY_H)) {
            drawTooltip(g, mouseX, mouseY,
                    "⚡ " + menu.getEnergyStored() + " / " + menu.getMaxEnergyStored() + " FE",
                    0xFF00FF88);
        }

        if (isHovering(mouseX, mouseY, x + OXYGEN_X, y + OXYGEN_Y, OXYGEN_W, OXYGEN_H)) {
            drawTooltip(g, mouseX, mouseY,
                    "O₂ " + menu.getOxygenBuffer() + " / " + menu.getOxygenBufferMax(),
                    0xFF00FFFF);
        }

        // 悬停植物槽
        if (isHovering(mouseX, mouseY, x + 79, y + 34, 18, 18)) {
            drawTwoLineTooltip(g, mouseX, mouseY,
                    "🌿 倍数 ×" + menu.getPlantMultiplier(),
                    "速度 +" + menu.getCurrentRate() + " /tick",
                    0xFF00CC44);
        }

        renderTooltip(g, mouseX, mouseY);
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void drawTooltip(GuiGraphics g, int mouseX, int mouseY, String text, int color) {
        int tw = font.width(text);
        int tx = mouseX + 8;
        int ty = mouseY - 4;
        int bw = tw + 8;
        int bh = 14;

        g.fill(tx - 2, ty - 2, tx + bw, ty + bh, 0xE0000D14);
        g.fill(tx - 2, ty - 2, tx + bw, ty, color);
        g.fill(tx - 2, ty + bh - 1, tx + bw, ty + bh, color);
        g.fill(tx - 2, ty - 2, tx, ty + bh, color);
        g.fill(tx + bw - 1, ty - 2, tx + bw, ty + bh, color);

        g.drawString(font, text, tx + 2, ty + 2, color, true);
    }

    private void drawTwoLineTooltip(GuiGraphics g, int mouseX, int mouseY,
                                    String line1, String line2, int color) {
        int w1 = font.width(line1);
        int w2 = font.width(line2);
        int maxW = Math.max(w1, w2);
        int tx = mouseX + 8;
        int ty = mouseY - 4;
        int bw = maxW + 8;
        int bh = 24;

        g.fill(tx - 2, ty - 2, tx + bw, ty + bh, 0xE0000D14);
        g.fill(tx - 2, ty - 2, tx + bw, ty, color);
        g.fill(tx - 2, ty + bh - 1, tx + bw, ty + bh, color);
        g.fill(tx - 2, ty - 2, tx, ty + bh, color);
        g.fill(tx + bw - 1, ty - 2, tx + bw, ty + bh, color);

        g.drawString(font, line1, tx + 2, ty + 2, color, true);
        g.drawString(font, line2, tx + 2, ty + 13, 0x8000FF88, false);
    }
}