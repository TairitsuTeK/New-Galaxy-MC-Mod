package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.EnergyCellMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 能量电池界面：只有一个大号能量显示条。 */
public class EnergyCellScreen extends StyledContainerScreen<EnergyCellMenu> {

    public EnergyCellScreen(EnergyCellMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected List<StatusArea> statusAreas() {
        int x = this.leftPos;
        int y = this.topPos;
        return List.of(new StatusArea(x + 62, y + 16, 52, 52,
                () -> SolidFuelGeneratorScreen.energyTooltip(
                        menu.getEnergyStored(), menu.getMaxEnergyStored()), null));
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        UiTheme.screen(g, x, y, imageWidth, imageHeight);
        UiTheme.verticalBar(g, x + 64, y + 18, 48, 48,
                menu.getEnergyStored(), menu.getMaxEnergyStored(),
                0xFF17506C, 0xFF8DEBFF);
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
