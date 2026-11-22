package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 火箭发射倒计时 HUD。
 * <p>
 * 骑乘处于倒计时状态的火箭时，在屏幕正中绘制一个**红色大号数字**（10 → 1），
 * 每过一秒做一次弹跳放大，并在下方显示「点火倒计时」标签。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class RocketCountdownHud {

    /** 数字颜色：亮红色。 */
    private static final int COLOR_NUMBER = 0xFFFF2A2A;
    /** 底衬边框颜色。 */
    private static final int COLOR_FRAME = 0xA0FF4040;
    private static final int COLOR_BACKDROP = 0x66000000;
    private static final int COLOR_LABEL = 0xFFFF9090;

    /** 基础缩放倍数，数字实际约为普通文字的 4 倍高。 */
    private static final float BASE_SCALE = 4.0F;

    private RocketCountdownHud() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!(mc.player.getVehicle() instanceof RocketEntity rocket)
                || rocket.getState() != RocketEntity.STATE_COUNTDOWN) {
            return;
        }

        int timer = rocket.getTimer();
        if (timer <= 0) {
            return;
        }
        // 剩余秒数：200..181 → 10，180..161 → 9，……，19..1 → 1
        String text = Integer.toString((timer + 19) / 20);

        GuiGraphics graphics = event.getGuiGraphics();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2;

        // 每秒开始时做一次弹跳：phase 从 1 衰减到 0
        float phase = (timer % 20) / 20.0F;
        float scale = BASE_SCALE * (1.0F + 0.18F * phase);

        int textWidth = mc.font.width(text);
        int textHeight = mc.font.lineHeight;

        int padWidth = (int) (textWidth * scale * 0.5F) + 16;
        int padHeight = (int) (textHeight * scale * 0.5F) + 12;

        graphics.fill(centerX - padWidth, centerY - padHeight,
                centerX + padWidth, centerY + padHeight, COLOR_BACKDROP);
        graphics.fill(centerX - padWidth, centerY - padHeight,
                centerX + padWidth, centerY - padHeight + 1, COLOR_FRAME);
        graphics.fill(centerX - padWidth, centerY + padHeight - 1,
                centerX + padWidth, centerY + padHeight, COLOR_FRAME);

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(mc.font, text,
                -textWidth / 2, -textHeight / 2, COLOR_NUMBER, true);
        graphics.pose().popPose();

        Component label = Component.translatable("gui.newgc.rocket.countdown_label");
        int labelWidth = mc.font.width(label);
        graphics.drawString(mc.font, label,
                centerX - labelWidth / 2, centerY + padHeight + 6, COLOR_LABEL, false);
    }
}
