package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Locale;

/**
 * 火箭上升段飞行进度与漂流状态 HUD。
 * <p>
 * 竖直进度条从底部向顶部填充，玩家头像停在填充液面顶部；
 * 当进度到达顶部后，服务端会把火箭切换到轨道状态并发送打开星图的同步包。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class RocketFlightHud {

    private static final int BAR_X = 10;
    private static final int BAR_W = 10;
    private static final int BAR_MAX_H = 150;
    private static final int HEAD_SIZE = 20;

    private static final int COLOR_BAR_BG = 0xF0060D14;
    private static final int COLOR_BAR_BORDER = UiTheme.ACCENT;
    private static final int COLOR_BAR_HIGH = 0xFF8DEBFF;
    private static final int COLOR_TEXT = UiTheme.TEXT;
    private static final int COLOR_SUBTLE = UiTheme.MUTED;

    private RocketFlightHud() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui || mc.screen != null) {
            return;
        }
        if (!(mc.player.getVehicle() instanceof RocketEntity rocket)) {
            return;
        }
        if (rocket.getState() == RocketEntity.STATE_DRIFT) {
            drawDriftHud(mc, event.getGuiGraphics(), rocket);
            return;
        }
        if (rocket.getState() != RocketEntity.STATE_ASCENT) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int barH = Math.min(BAR_MAX_H, Math.max(96, screenH - 120));
        int barTop = (screenH - barH) / 2;
        int barBottom = barTop + barH;

        float progress = Mth.clamp(
                rocket.getTimer() / (float) rocket.getAscentTicks(), 0.0F, 1.0F);

        // 半透明底板与刻度
        graphics.fill(BAR_X - 3, barTop - 3, BAR_X + BAR_W + 3, barBottom + 3, 0x50102030);
        graphics.fill(BAR_X, barTop, BAR_X + BAR_W, barBottom, COLOR_BAR_BG);
        graphics.fill(BAR_X - 2, barTop, BAR_X, barBottom, 0x804AD8FF);
        graphics.fill(BAR_X + BAR_W, barTop, BAR_X + BAR_W + 2, barBottom, 0x804AD8FF);

        for (int i = 0; i <= 4; i++) {
            int y = barTop + (barH - 1) * i / 4;
            graphics.fill(BAR_X - 5, y, BAR_X - 1, y + 1, 0x804AD8FF);
        }

        // 从底部向顶部填充的渐变进度
        int innerH = barH - 4;
        int fillH = Math.round(innerH * progress);
        for (int i = 0; i < fillH; i++) {
            float t = i / (float) Math.max(1, innerH);
            int r = (int) Mth.lerp(t, 255.0F, 70.0F);
            int g = (int) Mth.lerp(t, 145.0F, 220.0F);
            int b = (int) Mth.lerp(t, 35.0F, 255.0F);
            int color = 0xFF000000 | (r << 16) | (g << 8) | b;
            graphics.fill(BAR_X + 2, barBottom - 2 - i,
                    BAR_X + BAR_W - 2, barBottom - 1 - i, color);
        }

        // 边框和顶部高亮
        graphics.fill(BAR_X, barTop, BAR_X + BAR_W, barTop + 1, COLOR_BAR_BORDER);
        graphics.fill(BAR_X, barBottom - 1, BAR_X + BAR_W, barBottom, COLOR_BAR_BORDER);
        graphics.fill(BAR_X, barTop, BAR_X + 1, barBottom, COLOR_BAR_BORDER);
        graphics.fill(BAR_X + BAR_W - 1, barTop, BAR_X + BAR_W, barBottom, COLOR_BAR_BORDER);

        // 玩家头像跟随液面移动
        int fillTop = barBottom - 2 - fillH;
        int headX = BAR_X + BAR_W / 2 - HEAD_SIZE / 2;
        int headY = Mth.clamp(fillTop - HEAD_SIZE / 2,
                barTop, barBottom - HEAD_SIZE);
        graphics.fill(headX - 3, headY - 3, headX + HEAD_SIZE + 3, headY + HEAD_SIZE + 3, 0x704AD8FF);
        graphics.fill(headX - 1, headY - 1, headX + HEAD_SIZE + 1, headY + HEAD_SIZE + 1, 0xFF0A1418);
        PlayerFaceRenderer.draw(graphics, mc.player.getSkin(), headX, headY, HEAD_SIZE);

        // 文字信息
        int textX = BAR_X + BAR_W + 10;
        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.flight_progress"),
                textX, barTop - 2, COLOR_TEXT, true);
        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.ascent_stage"),
                textX, barTop + 12, COLOR_SUBTLE, false);
        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.tier_label", rocket.getTier()),
                textX, barTop + 58, COLOR_SUBTLE, false);
        String percent = Math.round(progress * 100.0F) + "%";
        graphics.drawString(mc.font, percent, textX, barTop + 30, COLOR_BAR_HIGH, true);
        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.to_orbit"),
                textX, barTop + 46, COLOR_SUBTLE, false);
    }

    /** 漂流状态：只显示坐标边界与警告；生命值由原版 HUD 显示。 */
    private static void drawDriftHud(Minecraft mc, GuiGraphics graphics, RocketEntity rocket) {
        int screenH = mc.getWindow().getGuiScaledHeight();
        int x = 12;
        int y = screenH / 2 - 42;
        int w = 196;
        int h = 74;

        graphics.fill(x, y, x + w, y + h, 0xC0100808);
        graphics.fill(x, y, x + w, y + 1, 0xFFFF5544);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFFFF5544);
        graphics.fill(x, y, x + 1, y + h, 0xFFFF5544);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFFFF5544);

        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.drift_title"),
                x + 8, y + 7, 0xFFFF7766, true);
        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.tier_label", rocket.getTier()),
                x + 8, y + 21, COLOR_SUBTLE, false);

        String coordinate;
        if (rocket.level().dimension().equals(ModDimensions.SPACE_LEVEL_KEY)) {
            coordinate = Component.translatable("gui.newgc.rocket.drift_y",
                    String.format(Locale.ROOT, "%.1f", rocket.getY()),
                    String.format(Locale.ROOT, "%.1f", rocket.getSpaceVoidY())).getString();
        } else {
            coordinate = Component.translatable("gui.newgc.rocket.drift_altitude",
                    String.format(Locale.ROOT, "%.1f", rocket.getY()),
                    String.format(Locale.ROOT, "%.1f", rocket.getLegacyDriftDamageY())).getString();
        }
        graphics.drawString(mc.font, coordinate,
                x + 8, y + 35, 0xFFFFCCAA, false);

        boolean noDamage = mc.player != null
                && (mc.player.isCreative() || mc.player.isSpectator());
        graphics.drawString(mc.font,
                Component.translatable(noDamage
                        ? "gui.newgc.rocket.drift_creative"
                        : "gui.newgc.rocket.drift_warning"),
                x + 8, y + 48, noDamage ? UiTheme.GOOD : 0xFFFFAA88, false);
        graphics.drawString(mc.font,
                Component.translatable("gui.newgc.rocket.drift_no_control"),
                x + 8, y + 61, 0xFF907070, false);
    }
}
