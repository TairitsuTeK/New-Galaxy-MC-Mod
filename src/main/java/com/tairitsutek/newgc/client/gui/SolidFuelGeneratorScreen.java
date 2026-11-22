package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.menu.SolidFuelGeneratorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 固体燃料发电机界面：只保留可视化能量条和燃烧条。 */
public class SolidFuelGeneratorScreen extends StyledContainerScreen<SolidFuelGeneratorMenu> {

    public SolidFuelGeneratorScreen(SolidFuelGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected List<StatusArea> statusAreas() {
        int x = this.leftPos;
        int y = this.topPos;
        return List.of(
                new StatusArea(x + 8, y + 18, 18, 50,
                        () -> energyTooltip(menu.getEnergyStored(), menu.getMaxEnergyStored()),
                        null),
                new StatusArea(x + 60, y + 58, 56, 9,
                        () -> List.of(
                                Component.translatable("gui.newgc.machine.burn")
                                        .withStyle(ChatFormatting.GOLD),
                                Component.translatable("gui.newgc.machine.tick_value",
                                        menu.getBurnTime(), menu.getMaxBurnTime())),
                        null)
        );
    }

    static List<Component> energyTooltip(int stored, int max) {
        int percent = max <= 0 ? 0 : Math.round(stored * 100.0F / max);
        return List.of(
                Component.translatable("gui.newgc.machine.energy")
                        .withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.newgc.machine.energy_value", stored, max),
                Component.translatable("gui.newgc.machine.percent", percent));
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
        UiTheme.progressBar(g, x + 62, y + 60, 52, 5,
                menu.getBurnTime(), menu.getMaxBurnTime(),
                0xFF6B3A22, 0xFFF0B45A);
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
