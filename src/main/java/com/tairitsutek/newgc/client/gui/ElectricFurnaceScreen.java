package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.client.jei.JeiRecipeOpener;
import com.tairitsutek.newgc.menu.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 电力熔炉界面。 */
public class ElectricFurnaceScreen extends StyledContainerScreen<ElectricFurnaceMenu> {

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected List<StatusArea> statusAreas() {
        int x = this.leftPos;
        int y = this.topPos;
        Runnable openRecipes = JeiRecipeOpener.isAvailable()
                ? () -> JeiRecipeOpener.open(JeiRecipeOpener.Kind.SMELTING)
                : null;
        return List.of(
                new StatusArea(x + 8, y + 18, 18, 50,
                        () -> SolidFuelGeneratorScreen.energyTooltip(
                                menu.getEnergyStored(), menu.getMaxEnergyStored()), null),
                new StatusArea(x + 76, y + 35, 38, 14,
                        () -> CompressorScreen.progressTooltip(
                                menu.getProgress(), menu.getMaxProgress()),
                        openRecipes)
        );
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
                0xFF6A412A, 0xFFF0B97A);
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
