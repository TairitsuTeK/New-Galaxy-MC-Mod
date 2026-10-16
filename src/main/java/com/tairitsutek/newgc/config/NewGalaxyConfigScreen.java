package com.tairitsutek.newgc.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NewGalaxyConfigScreen extends Screen {

    private final Screen parent;

    public NewGalaxyConfigScreen(Screen parent) {
        super(Component.literal("新星系 · 设置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int w = 220;
        int h = 20;

        // 只有一个开关：调试模式
        this.addRenderableWidget(CycleButton.onOffBuilder(
                        NewGalaxyConfig.CLIENT.debugMode.get())
                .create(cx - w / 2, cy - 30, w, h,
                        Component.literal("开启调试按钮"),
                        (btn, val) -> NewGalaxyConfig.CLIENT.debugMode.set(val)));

        // 完成
        this.addRenderableWidget(Button.builder(
                        Component.literal("完成"),
                        btn -> this.onClose())
                .bounds(cx - w / 2, cy + 10, w, h)
                .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        NewGalaxyConfig.SPEC.save();
        this.minecraft.setScreen(parent);
    }
}