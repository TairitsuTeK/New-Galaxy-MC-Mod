package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.SolidFuelGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SolidFuelGeneratorScreen extends AbstractContainerScreen<SolidFuelGeneratorMenu> {

    public SolidFuelGeneratorScreen(SolidFuelGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // ==================== 主背景 ====================
        for (int i = 0; i < imageHeight; i++) {
            float t = (float) i / imageHeight;
            int r = (int) (10 + 15 * t);
            int gr = (int) (18 + 20 * t);
            int b = (int) (24 + 28 * t);
            int color = 0xFF000000 | (r << 16) | (gr << 8) | b;
            g.fill(x, y + i, x + imageWidth, y + i + 1, color);
        }

        // ==================== 网格 ====================
        for (int gx = 0; gx < imageWidth; gx += 8) {
            g.fill(x + gx, y, x + gx + 1, y + imageHeight, 0x10FFFFFF);
        }
        for (int gy = 0; gy < imageHeight; gy += 8) {
            g.fill(x, y + gy, x + imageWidth, y + gy + 1, 0x10FFFFFF);
        }

        // ==================== 外边框 ====================
        g.fill(x, y, x + imageWidth, y + 2, 0xFF00E5FF);
        g.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF00E5FF);
        g.fill(x, y, x + 2, y + imageHeight, 0xFF00E5FF);
        g.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF00E5FF);

        // ==================== 四角装饰 ====================
        int corner = 8;
        g.fill(x, y, x + corner, y + 4, 0xFF00FFFF);
        g.fill(x, y, x + 4, y + corner, 0xFF00FFFF);
        g.fill(x + imageWidth - corner, y, x + imageWidth, y + 4, 0xFF00FFFF);
        g.fill(x + imageWidth - 4, y, x + imageWidth, y + corner, 0xFF00FFFF);
        g.fill(x, y + imageHeight - 4, x + corner, y + imageHeight, 0xFF00FFFF);
        g.fill(x, y + imageHeight - corner, x + 4, y + imageHeight, 0xFF00FFFF);
        g.fill(x + imageWidth - corner, y + imageHeight - 4, x + imageWidth, y + imageHeight, 0xFF00FFFF);
        g.fill(x + imageWidth - 4, y + imageHeight - corner, x + imageWidth, y + imageHeight, 0xFF00FFFF);

        // ==================== 燃料槽背景 ====================
        int slotX = x + 79;
        int slotY = y + 34;
        g.fill(slotX - 2, slotY - 2, slotX + 20, slotY + 20, 0x4000E5FF);
        g.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF0A1418);
        g.fill(slotX, slotY, slotX + 18, slotY + 1, 0xFF00B8D0);
        g.fill(slotX, slotY + 17, slotX + 18, slotY + 18, 0xFF00B8D0);
        g.fill(slotX, slotY, slotX + 1, slotY + 18, 0xFF00B8D0);
        g.fill(slotX + 17, slotY, slotX + 18, slotY + 18, 0xFF00B8D0);

        // ==================== 玩家背包槽 ====================
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + 83 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1418);
                g.fill(sx, sy, sx + 18, sy + 1, 0x6000B8D0);
                g.fill(sx, sy + 17, sx + 18, sy + 18, 0x6000B8D0);
                g.fill(sx, sy, sx + 1, sy + 18, 0x6000B8D0);
                g.fill(sx + 17, sy, sx + 18, sy + 18, 0x6000B8D0);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = y + 141;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1418);
            g.fill(sx, sy, sx + 18, sy + 1, 0x6000B8D0);
            g.fill(sx, sy + 17, sx + 18, sy + 18, 0x6000B8D0);
            g.fill(sx, sy, sx + 1, sy + 18, 0x6000B8D0);
            g.fill(sx + 17, sy, sx + 18, sy + 18, 0x6000B8D0);
        }

        // ==================== 能量条 ====================
        int energyX = x + 10;
        int energyY = y + 20;
        int energyW = 16;
        int energyH = 46;

        g.fill(energyX - 2, energyY - 2, energyX + energyW + 2, energyY + energyH + 2, 0x3000E5FF);
        g.fill(energyX, energyY, energyX + energyW, energyY + energyH, 0xFF0A1418);

        for (int i = 1; i < 6; i++) {
            int gy = energyY + (energyH * i / 6);
            g.fill(energyX + 1, gy, energyX + energyW - 1, gy + 1, 0x2000B8D0);
        }

        int stored = menu.getEnergyStored();
        int max = menu.getMaxEnergyStored();
        if (max > 0 && stored > 0) {
            int fillH = (int) ((float) stored / max * (energyH - 4));
            int fillY = energyY + energyH - 2 - fillH;

            for (int i = 0; i < fillH; i++) {
                float t = (float) i / fillH;
                int r = 255;
                int gr = (int) (140 + 80 * t);
                int b = (int) (30 + 30 * t);
                int color = 0xFF000000 | (r << 16) | (gr << 8) | b;
                g.fill(energyX + 2, fillY + i, energyX + energyW - 2, fillY + i + 1, color);
            }

            g.fill(energyX + 2, fillY, energyX + energyW - 2, fillY + 1, 0xFFFFFFFF);
        }

        g.fill(energyX, energyY, energyX + energyW, energyY + 1, 0xFF00E5FF);
        g.fill(energyX, energyY + energyH - 1, energyX + energyW, energyY + energyH, 0xFF00E5FF);
        g.fill(energyX, energyY, energyX + 1, energyY + energyH, 0xFF00E5FF);
        g.fill(energyX + energyW - 1, energyY, energyX + energyW, energyY + energyH, 0xFF00E5FF);

        for (int i = 0; i <= 4; i++) {
            int ty = energyY + (energyH * i / 4);
            g.fill(energyX - 2, ty, energyX, ty + 1, 0x8000B8D0);
        }

        // ==================== 燃烧进度 ====================
        int burnX = x + 65;
        int burnY = y + 60;
        int burnW = 46;
        int burnH = 6;

        g.fill(burnX, burnY, burnX + burnW, burnY + burnH, 0xFF0A1418);

        int burn = menu.getBurnTime();
        int maxBurn = menu.getMaxBurnTime();
        if (maxBurn > 0 && burn > 0) {
            int fillW = (int) ((float) burn / maxBurn * burnW);
            for (int i = 0; i < fillW; i++) {
                float t = (float) i / burnW;
                int r = 255;
                int gr = (int) (80 + 140 * t);
                int b = 0;
                int color = 0xFF000000 | (r << 16) | (gr << 8) | b;
                g.fill(burnX + i, burnY + 1, burnX + i + 1, burnY + burnH - 1, color);
            }
        }

        g.fill(burnX, burnY, burnX + burnW, burnY + 1, 0xFF00E5FF);
        g.fill(burnX, burnY + burnH - 1, burnX + burnW, burnY + burnH, 0xFF00E5FF);
        g.fill(burnX, burnY, burnX + 1, burnY + burnH, 0xFF00E5FF);
        g.fill(burnX + burnW - 1, burnY, burnX + burnW, burnY + burnH, 0xFF00E5FF);

        // ==================== 图标 ====================
        g.drawString(font, "⚡", x + 12, y + 8, 0xFFFFB300, true);
        g.drawString(font, "🔥", x + 59, y + 58, 0xFFFF5500, true);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int energyX = x + 10;
        int energyY = y + 20;
        int energyW = 16;
        int energyH = 46;

        if (mouseX >= energyX && mouseX <= energyX + energyW
                && mouseY >= energyY && mouseY <= energyY + energyH) {
            int stored = menu.getEnergyStored();
            int max = menu.getMaxEnergyStored();

            String text1 = "⚡ " + stored;
            String text2 = "/ " + max + " FE";

            int tw1 = font.width(text1);
            int tw2 = font.width(text2);

            int tx = mouseX + 8;
            int ty = mouseY - 4;

            // 背景框
            int boxW = Math.max(tw1, tw2) + 8;
            int boxH = 24;
            g.fill(tx - 2, ty - 2, tx + boxW, ty + boxH, 0xE0000D14);
            g.fill(tx - 2, ty - 2, tx + boxW, ty, 0xFF00E5FF);
            g.fill(tx - 2, ty + boxH - 1, tx + boxW, ty + boxH, 0xFF00E5FF);
            g.fill(tx - 2, ty - 2, tx, ty + boxH, 0xFF00E5FF);
            g.fill(tx + boxW - 1, ty - 2, tx + boxW, ty + boxH, 0xFF00E5FF);

            g.drawString(font, text1, tx + 2, ty + 2, 0xFFFFB300, true);
            g.drawString(font, text2, tx + 2, ty + 12, 0x80FFB300, false);
        }

        int burnX = x + 65;
        int burnY = y + 60;
        int burnW = 46;
        int burnH = 6;

        if (mouseX >= burnX && mouseX <= burnX + burnW
                && mouseY >= burnY && mouseY <= burnY + burnH) {
            int burn = menu.getBurnTime();
            int maxBurn = menu.getMaxBurnTime();

            String text1 = "🔥 " + burn + " tick";
            String text2 = "/ " + maxBurn;

            int tw1 = font.width(text1);
            int tw2 = font.width(text2);

            int tx = mouseX + 8;
            int ty = mouseY - 4;

            int boxW = Math.max(tw1, tw2) + 8;
            int boxH = 24;
            g.fill(tx - 2, ty - 2, tx + boxW, ty + boxH, 0xE0000D14);
            g.fill(tx - 2, ty - 2, tx + boxW, ty, 0xFF00E5FF);
            g.fill(tx - 2, ty + boxH - 1, tx + boxW, ty + boxH, 0xFF00E5FF);
            g.fill(tx - 2, ty - 2, tx, ty + boxH, 0xFF00E5FF);
            g.fill(tx + boxW - 1, ty - 2, tx + boxW, ty + boxH, 0xFF00E5FF);

            g.drawString(font, text1, tx + 2, ty + 2, 0xFFFF5500, true);
            g.drawString(font, text2, tx + 2, ty + 12, 0x80FF5500, false);
        }

        renderTooltip(g, mouseX, mouseY);
    }
}