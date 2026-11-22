package com.tairitsutek.newgc.client.jade;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.entity.AlloySmelterBlockEntity;
import com.tairitsutek.newgc.block.entity.CompressorBlockEntity;
import com.tairitsutek.newgc.block.entity.ElectricFurnaceBlockEntity;
import com.tairitsutek.newgc.block.entity.EnergyCellBlockEntity;
import com.tairitsutek.newgc.block.entity.OxygenExtractorBlockEntity;
import com.tairitsutek.newgc.block.entity.RocketAssemblerBlockEntity;
import com.tairitsutek.newgc.block.entity.SolidFuelGeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

/**
 * 在 Jade 的方块提示里显示机器状态，与准星下方的 HUD 使用同一套信息。
 */
public enum MachineStatusProvider implements IBlockComponentProvider {

    INSTANCE;

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            NewGalaxyMain.MODID, "machine_status");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(@NotNull ITooltip tooltip, @NotNull BlockAccessor accessor,
                              @NotNull IPluginConfig config) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof SolidFuelGeneratorBlockEntity generator) {
            addBar(tooltip, generator.getBurnTime(), generator.getMaxBurnTime(),
                    "gui.newgc.machine.burn", 0xFF6B3A22, 0xFFF0B45A);
        } else if (be instanceof OxygenExtractorBlockEntity extractor) {
            addBar(tooltip, extractor.getOxygenBuffer(), extractor.getOxygenBufferMax(),
                    "gui.newgc.machine.oxygen", 0xFF1D626A, 0xFF9BF4E4);
            tooltip.add(Component.translatable("gui.newgc.machine.rate_value",
                            extractor.getCurrentRate(), extractor.getPlantMultiplier())
                    .withStyle(ChatFormatting.AQUA));
        } else if (be instanceof CompressorBlockEntity compressor) {
            addBar(tooltip, compressor.getProgress(), compressor.getMaxProgress(),
                    "gui.newgc.machine.compressing", 0xFF5B4B2A, 0xFFEBC27A);
        } else if (be instanceof ElectricFurnaceBlockEntity furnace) {
            addBar(tooltip, furnace.getProgress(), furnace.getMaxProgress(),
                    "gui.newgc.machine.smelting", 0xFF6A412A, 0xFFF0B97A);
        } else if (be instanceof AlloySmelterBlockEntity smelter) {
            addBar(tooltip, smelter.getProgress(), smelter.getMaxProgress(),
                    "gui.newgc.machine.alloying", 0xFF5B4B2A, 0xFFEBC27A);
        } else if (be instanceof EnergyCellBlockEntity cell) {
            tooltip.add(Component.translatable("gui.newgc.machine.energy_value",
                            cell.getEnergyStored(), cell.getMaxEnergyStored())
                    .withStyle(ChatFormatting.GOLD));
        } else if (be instanceof RocketAssemblerBlockEntity assembler) {
            boolean ready = assembler.canAssembleNow();
            tooltip.add(Component.translatable(ready
                            ? "gui.newgc.machine.assembler_ready"
                            : "gui.newgc.machine.assembler_waiting")
                    .withStyle(ready ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
    }

    private static void addBar(ITooltip tooltip, int value, int max, String labelKey,
                               int lowColor, int highColor) {
        if (max <= 0) {
            return;
        }
        if (labelKey != null) {
            tooltip.add(Component.translatable(labelKey).withStyle(ChatFormatting.AQUA));
        }
        float fraction = Math.max(0.0F, Math.min(1.0F, value / (float) max));
        IElementHelper helper = IElementHelper.get();
        ProgressStyle style = helper.progressStyle()
                .color(lowColor, highColor)
                .textColor(0xFFE7F4FA);
        tooltip.add(helper.progress(fraction,
                Component.literal(value + " / " + max),
                style, BoxStyle.getNestedBox(), true));
    }
}
