package com.tairitsutek.newgc.config;

import com.tairitsutek.newgc.client.debug.DebugState;
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

        this.addRenderableWidget(CycleButton.onOffBuilder(
                        NewGalaxyConfig.CLIENT.debugMode.get())
                .create(cx - w / 2, cy - 30, w, h,
                        Component.literal("开启调试按钮"),
                        (btn, val) -> NewGalaxyConfig.CLIENT.debugMode.set(val)));

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

        // 关闭调试模式时，立即清空调试目标，恢复状态机
        if (!NewGalaxyConfig.CLIENT.debugMode.get()) {
            DebugState.resetAll();
        }

        this.minecraft.setScreen(parent);
    }
}