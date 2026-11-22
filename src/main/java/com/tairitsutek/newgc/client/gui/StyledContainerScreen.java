package com.tairitsutek.newgc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 所有机器界面的共同父类。
 * <p>
 * 原版 {@link AbstractContainerScreen} 使用深灰色绘制标题与“物品栏”，
 * 在深色玻璃面板上几乎不可读；这里统一改成浅色标题和弱化物品栏文字。
 * 子界面可以声明状态条区域，鼠标悬停时显示数值，点击进度条时打开 JEI 配方。
 */
public abstract class StyledContainerScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T> {

    /**
     * 一个可交互的装饰性状态区域。坐标使用屏幕绝对坐标，尺寸对应绘制出来的状态条。
     */
    protected record StatusArea(int x, int y, int width, int height,
                                Supplier<List<Component>> tooltip,
                                @Nullable Runnable onClick) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width
                    && mouseY >= y && mouseY < y + height;
        }
    }

    protected StyledContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    /** 子类覆盖此方法，声明能量条、进度条等可悬停/点击的区域。 */
    protected List<StatusArea> statusAreas() {
        return List.of();
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
        renderStatusTooltip(g, mouseX, mouseY);
        renderTooltip(g, mouseX, mouseY);
    }

    private void renderStatusTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (this.hoveredSlot != null) {
            return;
        }
        for (StatusArea area : statusAreas()) {
            if (!area.contains(mouseX, mouseY)) {
                continue;
            }
            List<Component> lines = area.tooltip().get();
            if (!lines.isEmpty()) {
                g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            }
            return;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 先处理状态条点击：AbstractContainerScreen 会把 GUI 内任意左键
        // 都交给槽位逻辑并返回 true，放在 super 之后会导致进度条永远点不动。
        if (button == 0) {
            for (StatusArea area : statusAreas()) {
                if (!area.contains(mouseX, mouseY) || area.onClick() == null) {
                    continue;
                }
                area.onClick().run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
