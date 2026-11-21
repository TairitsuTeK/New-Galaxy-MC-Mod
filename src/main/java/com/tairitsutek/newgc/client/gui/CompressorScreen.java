package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.CompressorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/** 压缩机界面：无文字标签，只有输入/输出槽、能量条和进度条。 */
public class CompressorScreen extends StyledContainerScreen<CompressorMenu> {

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
        UiTheme.screen(g, x, y, imageWidth, imageHeight);
        UiTheme.slot(g, x + 56, y + 34);
        UiTheme.slot(g, x + 116, y + 34);
        UiTheme.verticalBar(g, x + 10, y + 20, 14, 46,
                menu.getEnergyStored(), menu.getMaxEnergyStored(),
                0xFF1C5A78, 0xFF82E6FF);
        UiTheme.progressBar(g, x + 78, y + 37, 34, 10,
                menu.getProgress(), menu.getMaxProgress(),
                0xFF5B4B2A, 0xFFEBC27A);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                UiTheme.slot(g, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            UiTheme.slot(g, x + 8 + col * 18, y + 142);
        }
    }
}
