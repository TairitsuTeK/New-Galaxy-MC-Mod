package com.tairitsutek.newgc.block;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MachinesBlock {

    //region 注册器
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NewGalaxyMain.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewGalaxyMain.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewGalaxyMain.MODID);
    //endregion

    //region 机器方块
    public static final DeferredBlock<Block> MECHANICAL_CASING = registerBlock("mechanical_casing", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SOLID_FUEL_GENERATOR = registerBlock("solid_fuel_generator", () -> new SolidFuelGeneratorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> OXYGEN_EXTRACTOR = registerBlock("oxygen_extractor", () -> new OxygenExtractorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> WIRE = registerBlock("wire", () -> new WireBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1f, 3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> COMPRESSOR = registerBlock("compressor", () -> new CompressorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    //endregion

    //region 方块物品
    public static final DeferredItem<BlockItem> MECHANICAL_CASING_ITEM = registerItem("mechanical_casing", MECHANICAL_CASING);
    public static final DeferredItem<BlockItem> SOLID_FUEL_GENERATOR_ITEM = registerItem("solid_fuel_generator", SOLID_FUEL_GENERATOR);
    public static final DeferredItem<BlockItem> OXYGEN_EXTRACTOR_ITEM = registerItem("oxygen_extractor", OXYGEN_EXTRACTOR);
    public static final DeferredItem<BlockItem> WIRE_ITEM = registerItem("wire", WIRE);
    public static final DeferredItem<BlockItem> COMPRESSOR_ITEM = registerItem("compressor", COMPRESSOR);
    //endregion

    //region 注册辅助
    private static DeferredBlock<Block> registerBlock(String name, java.util.function.Supplier<Block> supplier) {
        return BLOCKS.register(name, supplier);
    }

    private static DeferredItem<BlockItem> registerItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block);
    }
    //endregion

    //region 机器创造模式物品栏
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MACHINE_TAB =
            CREATIVE_MODE_TABS.register("machine_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newgc.machines"))
                    .icon(() -> new ItemStack(SOLID_FUEL_GENERATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(MECHANICAL_CASING_ITEM.get());
                        output.accept(SOLID_FUEL_GENERATOR_ITEM.get());
                        output.accept(OXYGEN_EXTRACTOR_ITEM.get());
                        output.accept(WIRE_ITEM.get());
                        output.accept(COMPRESSOR_ITEM.get());
                    })
                    .build());
    //endregion

    //region 注册入口
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
    //endregion
}