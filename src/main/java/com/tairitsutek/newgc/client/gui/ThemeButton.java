package com.tairitsutek.newgc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import org.jetbrains.annotations.NotNull;

/** 与 UiTheme 一致的玻璃按钮。 */
public class ThemeButton extends Button {

    public ThemeButton(Button.Builder builder) {
        super(builder);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        boolean hover = active && isHovered();
        int top = active ? (hover ? 0xE0163344 : 0xE00C1D2A) : 0x90060C12;
        int bottom = active ? (hover ? 0xE00B1E2B : 0xE0061019) : 0x9004070A;
        int border = active ? (hover ? UiTheme.ACCENT : UiTheme.PANEL_BORDER) : 0x405D7E93;

        g.fill(x, y, x + w, y + h, top);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, top, bottom);
        g.fill(x, y, x + w, y + 1, border);
        g.fill(x, y + h - 1, x + w, y + h, border);
        g.fill(x, y, x + 1, y + h, border);
        g.fill(x + w - 1, y, x + w, y + h, border);
        UiTheme.cornerBrackets(g, x, y, x + w, y + h, border);

        String text = getMessage().getString();
        int color = active ? UiTheme.TEXT : 0xFF607080;
        g.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font,
                text, x + w / 2, y + (h - 8) / 2, color);
    }
}
