package com.tairitsutek.newgc.gui;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormClientCache;
import com.tairitsutek.newgc.Planets.jupiter.server.JupiterStormManager;
import com.tairitsutek.newgc.Planets.mars.client.MarsDustStormClientCache;
import com.tairitsutek.newgc.Planets.mars.client.MarsVolcanoClientCache;
import com.tairitsutek.newgc.Planets.mars.network.MarsVolcanoDebugPayload;
import com.tairitsutek.newgc.Planets.mercury.client.MercurySolarFlareClientCache;
import com.tairitsutek.newgc.Moons.io.client.IoQuakeClientCache;
import com.tairitsutek.newgc.Moons.io.client.IoVolcanoClientCache;
import com.tairitsutek.newgc.Moons.io.network.IoQuakeDebugPayload;
import com.tairitsutek.newgc.Moons.io.network.IoVolcanoDebugPayload;
import com.tairitsutek.newgc.client.debug.DebugState;
import com.tairitsutek.newgc.client.debug.HudInteractionState;
import com.tairitsutek.newgc.config.NewGalaxyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class PlanetStatusHud {

    private static final int WIDTH      = 210;
    private static final int PADDING    = 10;
    private static final int HEADER_H   = 20;
    private static final int ROW_H      = 15;
    private static final int CARD_GAP   = 6;
    private static final int MARGIN_TOP = 60;
    private static final int MARGIN_RIGHT = 10;
    private static final int CARD_3ROW  = ROW_H * 3 + PADDING * 2;

    private static final int BG          = 0xE00A121E;
    private static final int BG_INNER    = 0x90142436;
    private static final int BORDER_OUT  = 0xFF3A5570;
    private static final int BORDER_IN   = 0xFF1A2A3C;
    private static final int TITLE       = 0xFFB8D0F0;
    private static final int LABEL       = 0xFF8898B0;
    private static final int VALUE       = 0xFFE0ECFF;
    private static final int SEPARATOR   = 0xFF2A3E54;

    private static final int[] PHASE_COLORS = {
            0xFF7A8FA6, 0xFFE0A050, 0xFFE07A3A, 0xFFFF5B3A, 0xFFB07040
    };

    private static final int BAR_BG_TOP  = 0xFF0A1420;
    private static final int BAR_BG_BOT  = 0xFF060C14;
    private static final int BAR_TICK    = 0x60FFFFFF;

    private static final int MARS_DOT    = 0xFFE06B4B;
    private static final int DUST_DOT    = 0xFFC89650;
    private static final int FLARE_DOT   = 0xFFE0EEFF;
    private static final int IO_DOT      = 0xFFD4C84B;

    private static final int DEBUG_BTN_W = 40;
    private static final int DEBUG_BTN_H = 16;
    private static final int DEBUG_BTN_GAP = 4;

    private static int lastHudX = 0;
    private static int lastHudY = 0;
    private static int lastHudW = 0;
    private static int lastHudH = 0;
    private static boolean debugButtonsVisible = false;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.options.hideGui) return;

        if (!NewGalaxyConfig.CLIENT.debugMode.get()) {
            DebugState.resetAll();
        }

        boolean inJupiter = mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY);
        boolean inMars    = mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY);
        boolean inMercury = mc.level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY);
        boolean inIo      = mc.level.dimension().equals(ModDimensions.IO_LEVEL_KEY);

        if (!inJupiter && !inMars && !inMercury && !inIo) {
            debugButtonsVisible = false;
            return;
        }

        GuiGraphics g = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();

        int x = screenW - WIDTH - MARGIN_RIGHT;
        int y = MARGIN_TOP;

        int jupiterH = inJupiter ? CARD_3ROW : 0;
        int marsH = inMars ? CARD_3ROW * 2 : 0;
        int mercuryH = inMercury ? CARD_3ROW : 0;
        int ioH = inIo ? CARD_3ROW * 2 + CARD_GAP : 0;

        int cardCount = (inJupiter ? 1 : 0) + (inMars ? 2 : 0) + (inMercury ? 1 : 0) + (inIo ? 2 : 0);
        int totalH = HEADER_H + jupiterH + marsH + mercuryH + ioH
                + Math.max(0, cardCount - 1) * CARD_GAP
                + PADDING + 2;

        g.fill(x - 1, y - 1, x + WIDTH + 1, y + totalH + 1, 0x40000000);
        g.fill(x, y, x + WIDTH, y + totalH, BG);
        drawBorder(g, x, y, WIDTH, totalH, BORDER_OUT);

        g.fill(x + 1, y + 1, x + WIDTH - 1, y + 2, BORDER_IN);
        g.fill(x + 1, y + totalH - 2, x + WIDTH - 1, y + totalH - 1, BORDER_IN);

        // --- 标题栏 ---
        String planetNameKey;
        if (inJupiter)      planetNameKey = "gui.newgc.planet.title.jupiter";
        else if (inMars)    planetNameKey = "gui.newgc.planet.title.mars";
        else if (inMercury) planetNameKey = "gui.newgc.planet.title.mercury";
        else                planetNameKey = "gui.newgc.planet.title.io";
        String planetName = Component.translatable(planetNameKey).getString();

        String hudTitle = Component.translatable("gui.newgc.planet.title").getString();
        g.drawString(mc.font, hudTitle, x + PADDING, y + 6, TITLE, false);
        int nameW = mc.font.width(planetName);
        g.drawString(mc.font, planetName, x + WIDTH - PADDING - nameW, y + 6, TITLE, false);

        g.fill(x + PADDING, y + HEADER_H - 3, x + WIDTH - PADDING, y + HEADER_H - 2, SEPARATOR);

        int cy = y + HEADER_H;
        if (inJupiter) cy = drawJupiterCard(g, mc, x, cy);
        if (inMars) {
            cy = drawVolcanoCard(g, mc, x, cy);
            cy = drawDustStormCard(g, mc, x, cy);
        }
        if (inMercury) cy = drawSolarFlareCard(g, mc, x, cy);
        if (inIo) {
            cy = drawIoQuakeCard(g, mc, x, cy);
            cy = drawIoVolcanoCard(g, mc, x, cy);
        }

        lastHudX = x;
        lastHudY = y;
        lastHudW = WIDTH;
        lastHudH = totalH;

        if (NewGalaxyConfig.CLIENT.debugMode.get()) {
            debugButtonsVisible = true;
            drawDebugButtons(g, mc, x, y + totalH + 6, inJupiter, inMars, inMercury, inIo);
        } else {
            debugButtonsVisible = false;
        }
    }

    // ================= 木星卡片 =================
    private static int drawJupiterCard(GuiGraphics g, Minecraft mc, int x, int y) {
        JupiterStormClientCache.Phase phase = JupiterStormClientCache.getPhase();
        int phaseColor = PHASE_COLORS[phase.ordinal()];
        float strength = JupiterStormClientCache.getStormStrength();
        int remain = JupiterStormClientCache.getPhaseTimer();

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_3ROW, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_3ROW, 0x40FFFFFF & phaseColor | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_3ROW, phaseColor);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, Component.translatable("gui.newgc.card.jupiter_storm").getString(), tx, ty, TITLE, false);

        String phaseName = Component.translatable("gui.newgc.jupiter.phase." + phase.name().toLowerCase()).getString();
        int pW = mc.font.width(phaseName);
        int pillPad = 4;
        int pillX = rightEdge - pW - pillPad * 2;
        int pillY = ty - 1;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (phaseColor & 0x00FFFFFF));
        g.drawString(mc.font, phaseName, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;
        drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.phase_remain").getString(), formatTicks(remain), VALUE);
        ty += ROW_H;
        drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.strength").getString(), String.format("%.2f", strength), VALUE);

        drawStormBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, strength, phaseColor);
        return y + CARD_3ROW + CARD_GAP;
    }

    // ================= 火星 · 火山 =================
    private static int drawVolcanoCard(GuiGraphics g, Minecraft mc, int x, int y) {
        int eruption = MarsVolcanoClientCache.getEruptionTicks();
        int total = MarsVolcanoClientCache.getEruptionTotalTicks();
        int cooldown = MarsVolcanoClientCache.getCooldownTimer();
        BlockPos vp = MarsVolcanoClientCache.getVolcanoPos();
        boolean erupting = MarsVolcanoClientCache.isErupting();

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_3ROW, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_3ROW, 0x40FFFFFF & MARS_DOT | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_3ROW, MARS_DOT);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, Component.translatable("gui.newgc.card.mars_volcano").getString(), tx, ty, TITLE, false);

        String state = Component.translatable(erupting ? "gui.newgc.state.erupting" : "gui.newgc.state.cooldown").getString();
        int sW = mc.font.width(state);
        int pillPad = 4;
        int pillX = rightEdge - sW - pillPad * 2;
        int pillY = ty - 1;
        int pillColor = erupting ? MARS_DOT : 0xFF4A5A6E;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (pillColor & 0x00FFFFFF));
        g.drawString(mc.font, state, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;

        if (erupting) {
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.erupt_remain").getString(), formatTicks(eruption), VALUE);
            ty += ROW_H;
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.total_duration").getString(), formatTicks(total), VALUE);
            float progress = total > 0 ? (float) eruption / total : 0f;
            drawProgressBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, progress, MARS_DOT);
        } else {
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.cooldown_remain").getString(), formatTicks(cooldown), VALUE);
            ty += ROW_H;
            String dist = "—";
            if (vp != null) {
                Vec3 p = mc.player.position();
                double d = Math.sqrt(p.distanceToSqr(vp.getX() + 0.5, vp.getY() + 0.5, vp.getZ() + 0.5));
                dist = String.format("%.1f m", d);
            }
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.erupt_dist").getString(), dist, VALUE);
        }

        return y + CARD_3ROW + CARD_GAP;
    }

    // ================= 火星 · 沙尘暴 =================
    private static int drawDustStormCard(GuiGraphics g, Minecraft mc, int x, int y) {
        MarsDustStormClientCache.Phase phase = MarsDustStormClientCache.getPhase();
        int phaseTimer = MarsDustStormClientCache.getPhaseTimer();
        float strength = MarsDustStormClientCache.getStormStrength();

        int phaseColor = dustPhaseColor(phase);

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_3ROW, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_3ROW, 0x40FFFFFF & DUST_DOT | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_3ROW, DUST_DOT);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, Component.translatable("gui.newgc.card.mars_dust").getString(), tx, ty, TITLE, false);

        String phaseName = Component.translatable("gui.newgc.mars_dust.phase." + phase.name().toLowerCase()).getString();
        int pW = mc.font.width(phaseName);
        int pillPad = 4;
        int pillX = rightEdge - pW - pillPad * 2;
        int pillY = ty - 1;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (phaseColor & 0x00FFFFFF));
        g.drawString(mc.font, phaseName, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;
        drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.phase_remain").getString(), formatTicks(phaseTimer), VALUE);
        ty += ROW_H;
        drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.strength").getString(), String.format("%.2f", strength), VALUE);

        drawDustStormBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, strength, phaseColor);
        return y + CARD_3ROW + CARD_GAP;
    }

    // ================= 水星 · 太阳耀斑 =================
    private static int drawSolarFlareCard(GuiGraphics g, Minecraft mc, int x, int y) {
        MercurySolarFlareClientCache.Phase phase = MercurySolarFlareClientCache.getPhase();
        int phaseTimer = MercurySolarFlareClientCache.getPhaseTimer();
        float intensity = MercurySolarFlareClientCache.getIntensity();

        int phaseColor = flarePhaseColor(phase);

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_3ROW, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_3ROW, 0x40FFFFFF & FLARE_DOT | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_3ROW, FLARE_DOT);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, Component.translatable("gui.newgc.card.solar_flare").getString(), tx, ty, TITLE, false);

        String phaseName = Component.translatable("gui.newgc.solar_flare.phase." + phase.name().toLowerCase()).getString();
        int pW = mc.font.width(phaseName);
        int pillPad = 4;
        int pillX = rightEdge - pW - pillPad * 2;
        int pillY = ty - 1;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (phaseColor & 0x00FFFFFF));
        g.drawString(mc.font, phaseName, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;
        drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.phase_remain").getString(), formatTicks(phaseTimer), VALUE);
        ty += ROW_H;
        drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.strength").getString(), String.format("%.2f", intensity), VALUE);

        drawSolarFlareBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, intensity, phaseColor);
        return y + CARD_3ROW + CARD_GAP;
    }

    // ================= 木卫一 · 地震 =================
    private static int drawIoQuakeCard(GuiGraphics g, Minecraft mc, int x, int y) {
        boolean quaking = IoQuakeClientCache.isQuaking();
        int quakeTicks = IoQuakeClientCache.getQuakeTicks();
        int totalTicks = IoQuakeClientCache.getQuakeTotalTicks();
        int cooldown = IoQuakeClientCache.getCooldownTimer();
        float intensity = IoQuakeClientCache.getQuakeIntensity();

        int dotColor = quaking ? IO_DOT : 0xFF4A5A6E;

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_3ROW, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_3ROW, 0x40FFFFFF & dotColor | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_3ROW, dotColor);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, Component.translatable("gui.newgc.card.io_quake").getString(), tx, ty, TITLE, false);

        String state = Component.translatable(quaking ? "gui.newgc.state.quaking" : "gui.newgc.state.calm").getString();
        int sW = mc.font.width(state);
        int pillPad = 4;
        int pillX = rightEdge - sW - pillPad * 2;
        int pillY = ty - 1;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (dotColor & 0x00FFFFFF));
        g.drawString(mc.font, state, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;

        if (quaking) {
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.quake_remain").getString(), formatTicks(quakeTicks), VALUE);
            ty += ROW_H;
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.intensity").getString(), String.format("%.2f", intensity), VALUE);
            float progress = totalTicks > 0 ? (float) quakeTicks / totalTicks : 0f;
            drawProgressBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, progress, IO_DOT);
        } else {
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.geo_calm").getString(), "—", VALUE);
            ty += ROW_H;
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.next_quake").getString(), formatTicks(cooldown), VALUE);
            drawProgressBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, 0f, IO_DOT);
        }

        return y + CARD_3ROW + CARD_GAP;
    }

    // ================= 木卫一 · 火山 =================
    private static int drawIoVolcanoCard(GuiGraphics g, Minecraft mc, int x, int y) {
        int eruption = IoVolcanoClientCache.getEruptionTicks();
        int total = IoVolcanoClientCache.getEruptionTotalTicks();
        int cooldown = IoVolcanoClientCache.getCooldownTimer();
        BlockPos vp = IoVolcanoClientCache.getVolcanoPos();
        boolean erupting = IoVolcanoClientCache.isErupting();

        g.fill(x + PADDING, y, x + WIDTH - PADDING, y + CARD_3ROW, BG_INNER);
        g.fill(x + PADDING - 1, y, x + PADDING + 4, y + CARD_3ROW, 0x40FFFFFF & IO_DOT | 0x40000000);
        g.fill(x + PADDING, y, x + PADDING + 3, y + CARD_3ROW, IO_DOT);

        int tx = x + PADDING + 9;
        int ty = y + PADDING;
        int rightEdge = x + WIDTH - PADDING - 8;

        g.drawString(mc.font, Component.translatable("gui.newgc.card.io_volcano").getString(), tx, ty, TITLE, false);

        String state = Component.translatable(erupting ? "gui.newgc.state.erupting" : "gui.newgc.state.cooldown").getString();
        int sW = mc.font.width(state);
        int pillPad = 4;
        int pillX = rightEdge - sW - pillPad * 2;
        int pillY = ty - 1;
        int pillColor = erupting ? IO_DOT : 0xFF4A5A6E;
        g.fill(pillX, pillY, rightEdge, pillY + 10, 0x60000000 | (pillColor & 0x00FFFFFF));
        g.drawString(mc.font, state, pillX + pillPad, ty, 0xFFFFFFFF, false);

        ty += ROW_H;

        if (erupting) {
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.erupt_remain").getString(), formatTicks(eruption), VALUE);
            ty += ROW_H;
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.total_duration").getString(), formatTicks(total), VALUE);
            float progress = total > 0 ? (float) eruption / total : 0f;
            drawProgressBar(g, x + PADDING + 9, ty + ROW_H - 5, WIDTH - PADDING * 2 - 17 - 8, progress, IO_DOT);
        } else {
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.cooldown_remain").getString(), formatTicks(cooldown), VALUE);
            ty += ROW_H;
            String dist = "—";
            if (vp != null) {
                Vec3 p = mc.player.position();
                double d = Math.sqrt(p.distanceToSqr(vp.getX() + 0.5, vp.getY() + 0.5, vp.getZ() + 0.5));
                dist = String.format("%.1f m", d);
            }
            drawRow(g, mc, tx, ty, rightEdge, Component.translatable("gui.newgc.label.erupt_dist").getString(), dist, VALUE);
        }

        return y + CARD_3ROW + CARD_GAP;
    }

    // ================= 进度条与工具 =================
    private static void drawStormBar(GuiGraphics g, int x, int y, int w, float strength, int phaseColor) {
        int h = 7;
        g.fill(x, y, x + w, y + h / 2, BAR_BG_TOP);
        g.fill(x, y + h / 2, x + w, y + h, BAR_BG_BOT);

        float t = Math.max(0f, Math.min(1f, strength / 2.5f));
        int fillW = (int) (w * t);

        int fillColor;
        if (strength > 1.8f)      fillColor = 0xFFFF4A2A;
        else if (strength > 1.0f) fillColor = 0xFFFF8A3A;
        else if (strength > 0.5f) fillColor = 0xFFE0A050;
        else                      fillColor = phaseColor;

        g.fill(x, y, x + fillW, y + h, fillColor);
        g.fill(x, y, x + fillW, y + 1, lighten(fillColor, 0x40));
        g.fill(x, y + h - 1, x + fillW, y + h, darken(fillColor, 0x40));

        for (float tick : new float[]{0.2f, 0.4f, 0.6f, 0.8f}) {
            int tx = x + (int) (w * tick);
            g.fill(tx, y, tx + 1, y + h, BAR_TICK);
        }

        drawBorder(g, x, y, w, h, 0xFF1A2A3C);
        if (fillW > 0 && fillW < w) {
            g.fill(x + fillW, y - 1, x + fillW + 1, y + h + 1, 0xFFFFFFFF);
        }
    }

    private static void drawDustStormBar(GuiGraphics g, int x, int y, int w, float strength, int phaseColor) {
        int h = 7;
        g.fill(x, y, x + w, y + h / 2, BAR_BG_TOP);
        g.fill(x, y + h / 2, x + w, y + h, BAR_BG_BOT);

        float t = Math.max(0f, Math.min(1f, strength / 1.5f));
        int fillW = (int) (w * t);

        int fillColor;
        if (strength > 1.0f)      fillColor = 0xFFC89650;
        else if (strength > 0.5f) fillColor = 0xFFA8823C;
        else                      fillColor = phaseColor;

        g.fill(x, y, x + fillW, y + h, fillColor);
        g.fill(x, y, x + fillW, y + 1, lighten(fillColor, 0x40));
        g.fill(x, y + h - 1, x + fillW, y + h, darken(fillColor, 0x40));

        for (float tick : new float[]{1f / 3f, 2f / 3f}) {
            int tx = x + (int) (w * tick);
            g.fill(tx, y, tx + 1, y + h, BAR_TICK);
        }

        drawBorder(g, x, y, w, h, 0xFF1A2A3C);
        if (fillW > 0 && fillW < w) {
            g.fill(x + fillW, y - 1, x + fillW + 1, y + h + 1, 0xFFFFFFFF);
        }
    }

    private static void drawSolarFlareBar(GuiGraphics g, int x, int y, int w, float intensity, int phaseColor) {
        int h = 7;
        g.fill(x, y, x + w, y + h / 2, BAR_BG_TOP);
        g.fill(x, y + h / 2, x + w, y + h, BAR_BG_BOT);

        float t = Math.max(0f, Math.min(1f, intensity / 1.5f));
        int fillW = (int) (w * t);

        int fillColor;
        if (intensity > 1.0f)      fillColor = 0xFFFF4040;
        else if (intensity > 0.5f) fillColor = 0xFFE0A050;
        else                       fillColor = phaseColor;

        g.fill(x, y, x + fillW, y + h, fillColor);
        g.fill(x, y, x + fillW, y + 1, lighten(fillColor, 0x40));
        g.fill(x, y + h - 1, x + fillW, y + h, darken(fillColor, 0x40));

        for (float tick : new float[]{1f / 3f, 2f / 3f}) {
            int tx = x + (int) (w * tick);
            g.fill(tx, y, tx + 1, y + h, BAR_TICK);
        }

        drawBorder(g, x, y, w, h, 0xFF1A2A3C);
        if (fillW > 0 && fillW < w) {
            g.fill(x + fillW, y - 1, x + fillW + 1, y + h + 1, 0xFFFFFFFF);
        }
    }

    private static void drawProgressBar(GuiGraphics g, int x, int y, int w, float progress, int color) {
        int h = 7;
        progress = Math.max(0f, Math.min(1f, progress));

        g.fill(x, y, x + w, y + h / 2, BAR_BG_TOP);
        g.fill(x, y + h / 2, x + w, y + h, BAR_BG_BOT);

        int fillW = (int) (w * progress);
        g.fill(x, y, x + fillW, y + h, color);
        g.fill(x, y, x + fillW, y + 1, lighten(color, 0x40));
        g.fill(x, y + h - 1, x + fillW, y + h, darken(color, 0x40));

        drawBorder(g, x, y, w, h, 0xFF1A2A3C);
    }

    private static int dustPhaseColor(MarsDustStormClientCache.Phase p) {
        return switch (p) {
            case CALM    -> 0xFF6A5830;
            case RISING  -> 0xFFA8823C;
            case PEAK    -> 0xFFC89650;
            case FALLING -> 0xFF8A7040;
        };
    }

    private static int flarePhaseColor(MercurySolarFlareClientCache.Phase p) {
        return switch (p) {
            case CALM    -> 0xFF7A8FA6;
            case BUILDUP -> 0xFFE0A050;
            case FLARE   -> 0xFFFF4040;
            case DECAY   -> 0xFFB07040;
        };
    }

    // ================= 调试按钮 =================
    private static void drawDebugButtons(GuiGraphics g, Minecraft mc,
                                         int x, int y,
                                         boolean inJupiter, boolean inMars, boolean inMercury, boolean inIo) {
        int btnX = x;
        int btnY = y;

        if (inJupiter) {
            String[] keys = {"calm", "rising", "peak", "sudden", "reset", "thunder"};
            int[] phases = {
                    DebugState.JUPITER_CALM, DebugState.JUPITER_RISING, DebugState.JUPITER_PEAK,
                    DebugState.JUPITER_SUDDEN, DebugState.JUPITER_NONE, -999
            };
            int current = DebugState.getJupiterPhase();
            for (int i = 0; i < keys.length; i++) {
                boolean active = (phases[i] == -999) ? JupiterStormManager.isForceThunder() : phases[i] == current;
                String label = Component.translatable("gui.newgc.debug." + keys[i]).getString();
                drawDebugButton(g, mc, btnX, btnY, label, active);
                btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            }
        } else if (inMars) {
            String[] dustKeys = {"calm", "rising", "peak", "reset"};
            int[] dustPhases = {
                    DebugState.MARS_DUST_CALM, DebugState.MARS_DUST_RISING,
                    DebugState.MARS_DUST_PEAK, DebugState.MARS_DUST_NONE
            };
            int dustCurrent = DebugState.getMarsDustPhase();
            for (int i = 0; i < dustKeys.length; i++) {
                String label = Component.translatable("gui.newgc.debug." + dustKeys[i]).getString();
                drawDebugButton(g, mc, btnX, btnY, label, dustPhases[i] == dustCurrent);
                btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            }

            int row2Y = btnY + DEBUG_BTN_H + DEBUG_BTN_GAP;
            int btnX2 = x;

            boolean erupting = MarsVolcanoClientCache.isErupting();
            drawDebugButton(g, mc, btnX2, row2Y, Component.translatable("gui.newgc.debug.erupt").getString(), erupting);
            btnX2 += DEBUG_BTN_W + DEBUG_BTN_GAP;

            drawDebugButton(g, mc, btnX2, row2Y, Component.translatable("gui.newgc.debug.cooldown").getString(), !erupting && MarsVolcanoClientCache.getCooldownTimer() > 0);
        } else if (inMercury) {
            String[] keys = {"calm", "buildup", "flare", "decay", "reset"};
            int[] phases = {
                    DebugState.MERCURY_FLARE_CALM, DebugState.MERCURY_FLARE_BUILDUP,
                    DebugState.MERCURY_FLARE_FLARE, DebugState.MERCURY_FLARE_DECAY, DebugState.MERCURY_FLARE_NONE
            };
            int current = DebugState.getMercuryFlarePhase();
            for (int i = 0; i < keys.length; i++) {
                String label = Component.translatable("gui.newgc.debug." + keys[i]).getString();
                drawDebugButton(g, mc, btnX, btnY, label, phases[i] == current);
                btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            }
        } else if (inIo) {
            boolean quaking = IoQuakeClientCache.isQuaking();
            drawDebugButton(g, mc, btnX, btnY, Component.translatable("gui.newgc.debug.quake").getString(), quaking);
            btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            drawDebugButton(g, mc, btnX, btnY, Component.translatable("gui.newgc.debug.calm").getString(), !quaking);
            btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;

            int row2Y = btnY + DEBUG_BTN_H + DEBUG_BTN_GAP;
            int btnX2 = x;

            boolean erupting = IoVolcanoClientCache.isErupting();
            drawDebugButton(g, mc, btnX2, row2Y, Component.translatable("gui.newgc.debug.erupt").getString(), erupting);
            btnX2 += DEBUG_BTN_W + DEBUG_BTN_GAP;

            drawDebugButton(g, mc, btnX2, row2Y, Component.translatable("gui.newgc.debug.cooldown").getString(), !erupting && IoVolcanoClientCache.getCooldownTimer() > 0);
        }
    }

    private static void drawDebugButton(GuiGraphics g, Minecraft mc,
                                        int x, int y, String label, boolean active) {
        int bgColor = active ? 0xFF3A5570 : 0xB0142436;
        int borderColor = active ? 0xFF7AA0C0 : 0xFF2F4157;
        int textColor = active ? 0xFFFFFFFF : 0xC0D0E0;

        g.fill(x, y, x + DEBUG_BTN_W, y + DEBUG_BTN_H, bgColor);
        g.fill(x, y, x + DEBUG_BTN_W, y + 1, borderColor);
        g.fill(x, y + DEBUG_BTN_H - 1, x + DEBUG_BTN_W, y + DEBUG_BTN_H, borderColor);
        g.fill(x, y, x + 1, y + DEBUG_BTN_H, borderColor);
        g.fill(x + DEBUG_BTN_W - 1, y, x + DEBUG_BTN_W, y + DEBUG_BTN_H, borderColor);

        int textW = mc.font.width(label);
        g.drawString(mc.font, label, x + (DEBUG_BTN_W - textW) / 2, y + (DEBUG_BTN_H - 8) / 2, textColor, false);
    }

    // ================= 点击判定 =================
    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != 1) return;
        if (!NewGalaxyConfig.CLIENT.debugMode.get()) return;
        if (!debugButtonsVisible) return;
        if (!HudInteractionState.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.screen != null) return;

        double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth();
        double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight();

        boolean inJupiter = mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY);
        boolean inMars    = mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY);
        boolean inMercury = mc.level.dimension().equals(ModDimensions.MERCURY_LEVEL_KEY);
        boolean inIo      = mc.level.dimension().equals(ModDimensions.IO_LEVEL_KEY);

        int btnY = lastHudY + lastHudH + 6;
        int btnX = lastHudX;

        if (inJupiter) {
            int[] phases = {
                    DebugState.JUPITER_CALM, DebugState.JUPITER_RISING, DebugState.JUPITER_PEAK,
                    DebugState.JUPITER_SUDDEN, DebugState.JUPITER_NONE, -999
            };
            for (int i = 0; i < phases.length; i++) {
                if (isHover(mouseX, mouseY, btnX, btnY, DEBUG_BTN_W, DEBUG_BTN_H)) {
                    if (phases[i] == -999) {
                        JupiterStormManager.setForceThunder(!JupiterStormManager.isForceThunder());
                    } else {
                        DebugState.setJupiterPhase(phases[i]);
                    }
                    event.setCanceled(true);
                    return;
                }
                btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            }
        } else if (inMars) {
            int[] dustPhases = {
                    DebugState.MARS_DUST_CALM, DebugState.MARS_DUST_RISING,
                    DebugState.MARS_DUST_PEAK, DebugState.MARS_DUST_NONE
            };
            for (int phase : dustPhases) {
                if (isHover(mouseX, mouseY, btnX, btnY, DEBUG_BTN_W, DEBUG_BTN_H)) {
                    DebugState.setMarsDustPhase(phase);
                    event.setCanceled(true);
                    return;
                }
                btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            }

            int row2Y = btnY + DEBUG_BTN_H + DEBUG_BTN_GAP;
            int btnX2 = lastHudX;

            if (isHover(mouseX, mouseY, btnX2, row2Y, DEBUG_BTN_W, DEBUG_BTN_H)) {
                PacketDistributor.sendToServer(new MarsVolcanoDebugPayload(MarsVolcanoDebugPayload.MODE_ERUPT));
                event.setCanceled(true);
                return;
            }
            btnX2 += DEBUG_BTN_W + DEBUG_BTN_GAP;

            if (isHover(mouseX, mouseY, btnX2, row2Y, DEBUG_BTN_W, DEBUG_BTN_H)) {
                PacketDistributor.sendToServer(new MarsVolcanoDebugPayload(MarsVolcanoDebugPayload.MODE_COOLDOWN));
                event.setCanceled(true);
                return;
            }
        } else if (inMercury) {
            int[] phases = {
                    DebugState.MERCURY_FLARE_CALM, DebugState.MERCURY_FLARE_BUILDUP,
                    DebugState.MERCURY_FLARE_FLARE, DebugState.MERCURY_FLARE_DECAY, DebugState.MERCURY_FLARE_NONE
            };
            for (int phase : phases) {
                if (isHover(mouseX, mouseY, btnX, btnY, DEBUG_BTN_W, DEBUG_BTN_H)) {
                    DebugState.setMercuryFlarePhase(phase);
                    event.setCanceled(true);
                    return;
                }
                btnX += DEBUG_BTN_W + DEBUG_BTN_GAP;
            }
        } else if (inIo) {
            int btnX2 = lastHudX;

            if (isHover(mouseX, mouseY, btnX2, btnY, DEBUG_BTN_W, DEBUG_BTN_H)) {
                PacketDistributor.sendToServer(new IoQuakeDebugPayload(IoQuakeDebugPayload.MODE_QUAKE));
                event.setCanceled(true);
                return;
            }
            btnX2 += DEBUG_BTN_W + DEBUG_BTN_GAP;

            if (isHover(mouseX, mouseY, btnX2, btnY, DEBUG_BTN_W, DEBUG_BTN_H)) {
                PacketDistributor.sendToServer(new IoQuakeDebugPayload(IoQuakeDebugPayload.MODE_CALM));
                event.setCanceled(true);
                return;
            }

            int row2Y = btnY + DEBUG_BTN_H + DEBUG_BTN_GAP;
            int btnX3 = lastHudX;

            if (isHover(mouseX, mouseY, btnX3, row2Y, DEBUG_BTN_W, DEBUG_BTN_H)) {
                PacketDistributor.sendToServer(new IoVolcanoDebugPayload(IoVolcanoDebugPayload.MODE_ERUPT));
                event.setCanceled(true);
                return;
            }
            btnX3 += DEBUG_BTN_W + DEBUG_BTN_GAP;

            if (isHover(mouseX, mouseY, btnX3, row2Y, DEBUG_BTN_W, DEBUG_BTN_H)) {
                PacketDistributor.sendToServer(new IoVolcanoDebugPayload(IoVolcanoDebugPayload.MODE_COOLDOWN));
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean isHover(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static void drawRow(GuiGraphics g, Minecraft mc, int x, int y, int rightEdge, String label, String value, int valueColor) {
        g.drawString(mc.font, label, x, y, LABEL, false);
        int vW = mc.font.width(value);
        g.drawString(mc.font, value, rightEdge - vW, y, valueColor, false);
    }

    private static String formatTicks(int ticks) {
        if (ticks <= 0) return "0s";
        float sec = ticks / 20f;
        if (sec >= 60f) {
            int m = (int) (sec / 60);
            int s = (int) (sec % 60);
            return m + "m " + s + "s";
        }
        return String.format("%.1fs", sec);
    }

    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int lighten(int color, int amount) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, ((color >>> 16) & 0xFF) + amount);
        int g = Math.min(255, ((color >>> 8) & 0xFF) + amount);
        int b = Math.min(255, (color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int darken(int color, int amount) {
        int a = (color >>> 24) & 0xFF;
        int r = Math.max(0, ((color >>> 16) & 0xFF) - amount);
        int g = Math.max(0, ((color >>> 8) & 0xFF) - amount);
        int b = Math.max(0, (color & 0xFF) - amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}