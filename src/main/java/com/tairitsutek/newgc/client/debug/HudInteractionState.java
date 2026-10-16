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
        // 断开连接 / 有 GUI 时不调 grabMouse
        if (mc.screen != null) return;
        if (mc.level == null) return;

        mc.mouseHandler.grabMouse();
    }
}