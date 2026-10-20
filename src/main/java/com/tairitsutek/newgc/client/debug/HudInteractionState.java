package com.tairitsutek.newgc.client.debug;

import net.minecraft.client.Minecraft;

public final class HudInteractionState {

    private static boolean active = false;

    private HudInteractionState() {}

    public static boolean isActive() {
        return active;
    }

    public static void toggle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        if (mc.level == null) return;

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
        // ★ 修复：如果正在断开连接或退出，直接跳过，防止崩溃
        if (mc.level == null) return;
        if (mc.screen != null) return;

        mc.mouseHandler.grabMouse();
    }
}