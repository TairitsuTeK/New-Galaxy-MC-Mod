package com.tairitsutek.newgc.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.entity.AlloySmelterBlockEntity;
import com.tairitsutek.newgc.block.entity.CompressorBlockEntity;
import com.tairitsutek.newgc.block.entity.ElectricFurnaceBlockEntity;
import com.tairitsutek.newgc.block.entity.EnergyCellBlockEntity;
import com.tairitsutek.newgc.block.entity.OxygenExtractorBlockEntity;
import com.tairitsutek.newgc.block.entity.RocketAssemblerBlockEntity;
import com.tairitsutek.newgc.block.entity.SolidFuelGeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class BlockHoverInfo {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);

        if (be instanceof SolidFuelGeneratorBlockEntity gen) {
            renderGeneratorInfo(event, mc, gen);
        } else if (be instanceof OxygenExtractorBlockEntity extractor) {
            renderExtractorInfo(event, mc, extractor);
        } else if (be instanceof CompressorBlockEntity compressor) {
            renderCompressorInfo(event, mc, compressor);
        } else if (be instanceof ElectricFurnaceBlockEntity furnace) {
            renderEnergyProgressInfo(event, mc, "block.newgc.electric_furnace",
                    0xFFFF9F68, furnace.getEnergyStored(), furnace.getMaxEnergyStored(),
                    furnace.getProgress(), furnace.getMaxProgress(),
                    "gui.newgc.machine.smelting");
        } else if (be instanceof AlloySmelterBlockEntity smelter) {
            renderEnergyProgressInfo(event, mc, "block.newgc.alloy_smelter",
                    0xFF8DEBFF, smelter.getEnergyStored(), smelter.getMaxEnergyStored(),
                    smelter.getProgress(), smelter.getMaxProgress(),
                    "gui.newgc.machine.alloying");
        } else if (be instanceof EnergyCellBlockEntity cell) {
            renderEnergyProgressInfo(event, mc, "block.newgc.energy_cell",
                    0xFF82E6FF, cell.getEnergyStored(), cell.getMaxEnergyStored(),
                    0, 0, null);
        } else if (be instanceof RocketAssemblerBlockEntity assembler) {
            renderAssemblerInfo(event, mc, assembler);
        }
    }

    private static void renderGeneratorInfo(RenderGuiEvent.Post event, Minecraft mc,
                                            SolidFuelGeneratorBlockEntity gen) {
        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 + 20;

        int stored = gen.getEnergyStorage().getEnergyStored();
        int max = gen.getEnergyStorage().getMaxEnergyStored();
        int burn = gen.getBurnTime();
        int maxBurn = gen.getMaxBurnTime();

        Component title = Component.translatable("block.newgc.solid_fuel_generator")
                .withStyle(ChatFormatting.AQUA);
        g.drawCenteredString(mc.font, title, centerX, centerY, 0xFFFFFFFF);

        Component energy = Component.literal("⚡ " + stored + " / " + max + " FE")
                .withStyle(ChatFormatting.GOLD);
        g.drawCenteredString(mc.font, energy, centerX, centerY + 12, 0xFFFFFFFF);

        drawBar(g, centerX - 40, centerY + 26, 80, 6, stored, max, 0xFFFFB300);

        if (burn > 0) {
            Component burnText = Component.translatable(
                            "gui.newgc.machine.burning", burn)
                    .withStyle(ChatFormatting.AQUA);
            g.drawCenteredString(mc.font, burnText, centerX, centerY + 40, 0xFFFFFFFF);
            drawBar(g, centerX - 40, centerY + 52, 80, 4, burn, maxBurn, 0xFF78D8F0);
        } else {
            Component idle = Component.translatable("gui.newgc.machine.idle")
                    .withStyle(ChatFormatting.GRAY);
            g.drawCenteredString(mc.font, idle, centerX, centerY + 40, 0xFFFFFFFF);
        }
    }

    private static void renderExtractorInfo(RenderGuiEvent.Post event, Minecraft mc,
                                            OxygenExtractorBlockEntity ext) {
        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 + 20;

        int stored = ext.getEnergyStorage().getEnergyStored();
        int max = ext.getEnergyStorage().getMaxEnergyStored();
        int rate = ext.getCurrentRate();
        int mult = ext.getPlantMultiplier();

        Component title = Component.translatable("block.newgc.oxygen_extractor")
                .withStyle(ChatFormatting.GREEN);
        g.drawCenteredString(mc.font, title, centerX, centerY, 0xFFFFFFFF);

        Component energy = Component.literal("⚡ " + stored + " / " + max + " FE")
                .withStyle(ChatFormatting.GOLD);
        g.drawCenteredString(mc.font, energy, centerX, centerY + 12, 0xFFFFFFFF);

        drawBar(g, centerX - 40, centerY + 26, 80, 6, stored, max, 0xFF00FF88);

        Component plantText = Component.translatable(
                        "gui.newgc.machine.oxygen_rate", mult, rate)
                .withStyle(ChatFormatting.AQUA);
        g.drawCenteredString(mc.font, plantText, centerX, centerY + 40, 0xFFFFFFFF);

        if (stored < rate * OxygenExtractorBlockEntity.FE_PER_OXYGEN) {
            Component warning = Component.translatable("gui.newgc.machine.no_energy")
                    .withStyle(ChatFormatting.RED);
            g.drawCenteredString(mc.font, warning, centerX, centerY + 54, 0xFFFFFFFF);
        } else {
            Component ok = Component.translatable("gui.newgc.machine.running")
                    .withStyle(ChatFormatting.AQUA);
            g.drawCenteredString(mc.font, ok, centerX, centerY + 54, 0xFFFFFFFF);
        }
    }

    private static void renderCompressorInfo(RenderGuiEvent.Post event, Minecraft mc,
                                             CompressorBlockEntity compressor) {
        GuiGraphics g = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 + 20;

        int stored = compressor.getEnergyStorage().getEnergyStored();
        int max = compressor.getEnergyStorage().getMaxEnergyStored();
        int progress = compressor.getProgress();

        Component title = Component.translatable("block.newgc.compressor")
                .withStyle(ChatFormatting.GOLD);
        g.drawCenteredString(mc.font, title, centerX, centerY, 0xFFFFFFFF);

        Component energy = Component.literal("⚡ " + stored + " / " + max + " FE")
                .withStyle(ChatFormatting.GOLD);
        g.drawCenteredString(mc.font, energy, centerX, centerY + 12, 0xFFFFFFFF);

        drawBar(g, centerX - 40, centerY + 26, 80, 6, stored, max, 0xFFFF8C00);

        if (progress > 0) {
            Component progressText = Component.translatable(
                            "gui.newgc.machine.compressing", progress,
                            compressor.getMaxProgress())
                    .withStyle(ChatFormatting.AQUA);
            g.drawCenteredString(mc.font, progressText, centerX, centerY + 40, 0xFFFFFFFF);
        } else {
            Component idle = Component.translatable("gui.newgc.machine.idle")
                    .withStyle(ChatFormatting.GRAY);
            g.drawCenteredString(mc.font, idle, centerX, centerY + 40, 0xFFFFFFFF);
        }
    }

    private static void renderEnergyProgressInfo(RenderGuiEvent.Post event, Minecraft mc,
                                                 String titleKey, int accent,
                                                 int stored, int max, int progress, int maxProgress,
                                                 String progressKey) {
        GuiGraphics g = event.getGuiGraphics();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2 + 20;

        g.drawCenteredString(mc.font,
                Component.translatable(titleKey).withStyle(ChatFormatting.AQUA),
                centerX, centerY, 0xFFFFFFFF);
        g.drawCenteredString(mc.font,
                Component.translatable("gui.newgc.machine.energy_value", stored, max)
                        .withStyle(ChatFormatting.GOLD),
                centerX, centerY + 12, 0xFFFFFFFF);
        drawBar(g, centerX - 40, centerY + 26, 80, 6, stored, max, accent);

        if (progressKey != null && maxProgress > 0) {
            g.drawCenteredString(mc.font,
                    Component.translatable(progressKey).withStyle(ChatFormatting.AQUA),
                    centerX, centerY + 40, 0xFFFFFFFF);
            drawBar(g, centerX - 40, centerY + 52, 80, 4, progress, maxProgress, 0xFF78D8F0);
        } else {
            g.drawCenteredString(mc.font,
                    Component.translatable("gui.newgc.machine.energy_cell_hint")
                            .withStyle(ChatFormatting.GRAY),
                    centerX, centerY + 40, 0xFFFFFFFF);
        }
    }

    private static void renderAssemblerInfo(RenderGuiEvent.Post event, Minecraft mc,
                                           RocketAssemblerBlockEntity assembler) {
        GuiGraphics g = event.getGuiGraphics();
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int centerY = mc.getWindow().getGuiScaledHeight() / 2 + 20;
        boolean ready = assembler.canAssembleNow();
        g.drawCenteredString(mc.font,
                Component.translatable("block.newgc.rocket_assembler")
                        .withStyle(ChatFormatting.AQUA),
                centerX, centerY, 0xFFFFFFFF);
        g.drawCenteredString(mc.font,
                Component.translatable(ready
                                ? "gui.newgc.machine.assembler_ready"
                                : "gui.newgc.machine.assembler_waiting")
                        .withStyle(ready ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                centerX, centerY + 14, 0xFFFFFFFF);
        g.drawCenteredString(mc.font,
                Component.translatable("gui.newgc.machine.assembler_hint")
                        .withStyle(ChatFormatting.DARK_AQUA),
                centerX, centerY + 30, 0xFFFFFFFF);
    }

    private static void drawBar(GuiGraphics g, int x, int y, int w, int h, int stored, int max, int fillColor) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF000000);
        g.fill(x, y, x + w, y + h, 0xFF101010);
        if (max > 0 && stored > 0) {
            int fillW = (int) ((float) stored / max * w);
            g.fill(x, y, x + fillW, y + h, fillColor);
        }
        g.fill(x, y, x + w, y + 1, 0xFF00B8D0);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF00B8D0);
        g.fill(x, y, x + 1, y + h, 0xFF00B8D0);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF00B8D0);
    }
}
