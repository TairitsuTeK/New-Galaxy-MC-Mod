package com.tairitsutek.newgc.client.debug;

import net.minecraft.client.Minecraft;

/**
 * 管理 HUD 交互模式：
 * - 开启调试模式后，按左 Alt 解锁鼠标
 * - 再按一次锁定鼠标
 * - 打开任意 GUI 时自动关闭
 */
public final class HudInteractionState {

    private static boolean active = false;

    private HudInteractionState() {}

    public static boolean isActive() {
        return active;
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;   // 有 GUI 打开时不切换

        active = !active;

        if (active) {
            mc.mouseHandler.releaseMouse();
        } else {
            mc.mouseHandler.grabMouse();
        }
    }

    public static void deactivate() {
        if (!active) return;
        active = false;
        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.grabMouse();
    }
}