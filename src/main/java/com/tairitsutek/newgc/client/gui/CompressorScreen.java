package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.CompressorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class CompressorScreen extends AbstractContainerScreen<CompressorMenu> {

    private static final int ENERGY_X = 10;
    private static final int ENERGY_Y = 20;
    private static final int ENERGY_W = 16;
    private static final int ENERGY_H = 46;

    private static final int PROGRESS_X = 78;
    private static final int PROGRESS_Y = 37;
    private static final int PROGRESS_W = 34;
    private static final int PROGRESS_H = 10;

    public CompressorScreen(CompressorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 背景渐变
        for (int i = 0; i < imageHeight; i++) {
            float t = (float) i / imageHeight;
            int r = (int) (14 + 14 * t);
            int gr = (int) (16 + 16 * t);
            int b = (int) (22 + 20 * t);
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
        g.fill(x, y, x + imageWidth, y + 2, 0xFFFF8C00);
        g.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFFFF8C00);
        g.fill(x, y, x + 2, y + imageHeight, 0xFFFF8C00);
        g.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFFFF8C00);

        // 四角
        int corner = 8;
        g.fill(x, y, x + corner, y + 4, 0xFFFFB040);
        g.fill(x, y, x + 4, y + corner, 0xFFFFB040);
        g.fill(x + imageWidth - corner, y, x + imageWidth, y + 4, 0xFFFFB040);
        g.fill(x + imageWidth - 4, y, x + imageWidth, y + corner, 0xFFFFB040);
        g.fill(x, y + imageHeight - 4, x + corner, y + imageHeight, 0xFFFFB040);
        g.fill(x, y + imageHeight - corner, x + 4, y + imageHeight, 0xFFFFB040);
        g.fill(x + imageWidth - corner, y + imageHeight - 4, x + imageWidth, y + imageHeight, 0xFFFFB040);
        g.fill(x + imageWidth - 4, y + imageHeight - corner, x + imageWidth, y + imageHeight, 0xFFFFB040);

        // 输入/输出槽
        drawSlot(g, x + 56, y + 34);
        drawSlot(g, x + 116, y + 34);

        // 玩家背包槽
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + 7 + col * 18;
                int sy = y + 83 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1418);
                g.fill(sx, sy, sx + 18, sy + 1, 0x60FFB040);
                g.fill(sx, sy + 17, sx + 18, sy + 18, 0x60FFB040);
                g.fill(sx, sy, sx + 1, sy + 18, 0x60FFB040);
                g.fill(sx + 17, sy, sx + 18, sy + 18, 0x60FFB040);
            }
        }
        for (int col = 0; col < 9; col++) {
            int sx = x + 7 + col * 18;
            int sy = y + 141;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1418);
            g.fill(sx, sy, sx + 18, sy + 1, 0x60FFB040);
            g.fill(sx, sy + 17, sx + 18, sy + 18, 0x60FFB040);
            g.fill(sx, sy, sx + 1, sy + 18, 0x60FFB040);
            g.fill(sx + 17, sy, sx + 18, sy + 18, 0x60FFB040);
        }

        // 能量条
        drawBar(g, x + ENERGY_X, y + ENERGY_Y, ENERGY_W, ENERGY_H,
                menu.getEnergyStored(), menu.getMaxEnergyStored(),
                0xFFFF8C00, 0x30FF8C00, 0x80FFB040, true);

        // 进度条
        drawProgressBar(g, x + PROGRESS_X, y + PROGRESS_Y, PROGRESS_W, PROGRESS_H,
                menu.getProgress(), menu.getMaxProgress());

        // ===== 标注文字 =====
        g.drawCenteredString(font, "能量", x + 18, y + 8, 0xFFFFB040);          // 能量条上方
        g.drawCenteredString(font, "进度", x + 95, y + 25, 0xFFFFB040);         // 进度条上方
        g.drawCenteredString(font, "输入", x + 65, y + 55, 0xFFD8D8D8);         // 输入槽下方
        g.drawCenteredString(font, "输出", x + 125, y + 55, 0xFFD8D8D8);        // 输出槽下方
    }

    private void drawSlot(GuiGraphics g, int sx, int sy) {
        g.fill(sx - 2, sy - 2, sx + 20, sy + 20, 0x40FF8C00);
        g.fill(sx, sy, sx + 18, sy + 18, 0xFF0A1418);
        g.fill(sx, sy, sx + 18, sy + 1, 0xFFFFB040);
        g.fill(sx, sy + 17, sx + 18, sy + 18, 0xFFFFB040);
        g.fill(sx, sy, sx + 1, sy + 18, 0xFFFFB040);
        g.fill(sx + 17, sy, sx + 18, sy + 18, 0xFFFFB040);
    }

    private void drawBar(GuiGraphics g, int x, int y, int w, int h,
                         int stored, int max,
                         int borderColor, int glowColor, int tickColor,
                         boolean tickLeft) {
        g.fill(x - 2, y - 2, x + w + 2, y + h + 2, glowColor);
        g.fill(x, y, x + w, y + h, 0xFF0A1418);

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

    private void drawProgressBar(GuiGraphics g, int x, int y, int w, int h, int progress, int max) {
        g.fill(x, y, x + w, y + h, 0xFF0A1418);
        if (max > 0 && progress > 0) {
            int fillW = (int) ((float) progress / max * w);
            for (int i = 0; i < fillW; i++) {
                float t = (float) i / w;
                int r = 255;
                int gr = (int) (90 + 130 * t);
                int b = 0;
                g.fill(x + i, y + 1, x + i + 1, y + h - 1, 0xFF000000 | (r << 16) | (gr << 8) | b);
            }
        }
        g.fill(x, y, x + w, y + 1, 0xFFFF8C00);
        g.fill(x, y + h - 1, x + w, y + h, 0xFFFF8C00);
        g.fill(x, y, x + 1, y + h, 0xFFFF8C00);
        g.fill(x + w - 1, y, x + w, y + h, 0xFFFF8C00);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (isHovering(mouseX, mouseY, x + ENERGY_X, y + ENERGY_Y, ENERGY_W, ENERGY_H)) {
            drawTooltip(g, mouseX, mouseY,
                    "⚡ " + menu.getEnergyStored() + " / " + menu.getMaxEnergyStored() + " FE",
                    0xFFFFB040);
        }

        if (isHovering(mouseX, mouseY, x + PROGRESS_X, y + PROGRESS_Y, PROGRESS_W, PROGRESS_H)) {
            drawTooltip(g, mouseX, mouseY,
                    "进度 " + menu.getProgress() + " / " + menu.getMaxProgress(),
                    0xFFFF8C00);
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
}
