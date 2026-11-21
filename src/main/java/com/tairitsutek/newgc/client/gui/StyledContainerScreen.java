package com.tairitsutek.newgc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

/**
 * 所有机器界面的共同父类。
 * <p>
 * 原版 {@link AbstractContainerScreen} 使用深灰色绘制标题与“物品栏”，
 * 在深色玻璃面板上几乎不可读；这里统一改成浅色标题和弱化物品栏文字。
 */
public abstract class StyledContainerScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T> {

    protected StyledContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        String titleText = fit(this.title.getString(),
                Math.max(0, this.imageWidth - this.titleLabelX - 4));
        String inventoryText = fit(this.playerInventoryTitle.getString(),
                Math.max(0, this.imageWidth - this.inventoryLabelX - 4));
        g.drawString(this.font, titleText, this.titleLabelX, this.titleLabelY,
                UiTheme.TEXT, false);
        g.drawString(this.font, inventoryText,
                this.inventoryLabelX, this.inventoryLabelY, UiTheme.MUTED, false);
    }

    private String fit(String text, int maxWidth) {
        if (text == null || this.font.width(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        int ellipsis = this.font.width("…");
        return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsis)) + "…";
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
