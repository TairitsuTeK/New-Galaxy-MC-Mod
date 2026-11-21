package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.OxygenExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/** 氧气制造机界面：仅显示能量和氧气储量。 */
public class OxygenExtractorScreen extends StyledContainerScreen<OxygenExtractorMenu> {

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
        UiTheme.screen(g, x, y, imageWidth, imageHeight);
        UiTheme.slot(g, x + 80, y + 35);
        UiTheme.verticalBar(g, x + 10, y + 20, 14, 46,
                menu.getEnergyStored(), menu.getMaxEnergyStored(),
                0xFF1C5A78, 0xFF82E6FF);
        UiTheme.verticalBar(g, x + 150, y + 20, 14, 46,
                menu.getOxygenBuffer(), menu.getOxygenBufferMax(),
                0xFF1D626A, 0xFF9BF4E4);
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
