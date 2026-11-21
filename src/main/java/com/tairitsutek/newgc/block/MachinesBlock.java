package com.tairitsutek.newgc.block;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
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
    public static final DeferredBlock<Block> ELECTRIC_FURNACE = registerBlock("electric_furnace", () -> new ElectricFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ALLOY_SMELTER = registerBlock("alloy_smelter", () -> new AlloySmelterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ENERGY_CELL = registerBlock("energy_cell", () -> new EnergyCellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3.5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> LAUNCH_PAD = registerBlock("launch_pad", () -> new LaunchPadBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(4f, 8f).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<Block> ROCKET_ASSEMBLER = registerBlock("rocket_assembler", () -> new RocketAssemblerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(4f, 8f).requiresCorrectToolForDrops().noOcclusion()));
    //endregion

    //region 方块物品
    public static final DeferredItem<BlockItem> MECHANICAL_CASING_ITEM = registerItem("mechanical_casing", MECHANICAL_CASING);
    public static final DeferredItem<BlockItem> SOLID_FUEL_GENERATOR_ITEM = registerItem("solid_fuel_generator", SOLID_FUEL_GENERATOR);
    public static final DeferredItem<BlockItem> OXYGEN_EXTRACTOR_ITEM = registerItem("oxygen_extractor", OXYGEN_EXTRACTOR);
    public static final DeferredItem<BlockItem> WIRE_ITEM = registerItem("wire", WIRE);
    public static final DeferredItem<BlockItem> COMPRESSOR_ITEM = registerItem("compressor", COMPRESSOR);
    public static final DeferredItem<BlockItem> ELECTRIC_FURNACE_ITEM = registerItem("electric_furnace", ELECTRIC_FURNACE);
    public static final DeferredItem<BlockItem> ALLOY_SMELTER_ITEM = registerItem("alloy_smelter", ALLOY_SMELTER);
    public static final DeferredItem<BlockItem> ENERGY_CELL_ITEM = registerItem("energy_cell", ENERGY_CELL);
    public static final DeferredItem<BlockItem> LAUNCH_PAD_ITEM = registerItem("launch_pad", LAUNCH_PAD);
    public static final DeferredItem<BlockItem> ROCKET_ASSEMBLER_ITEM = registerItem("rocket_assembler", ROCKET_ASSEMBLER);

    /** 一阶火箭物品：只能放在发射平台上变成火箭实体。 */
    public static final DeferredItem<Item> ROCKET_ITEM =
            ITEMS.registerSimpleItem("rocket", new Item.Properties().stacksTo(16));

    /** 二阶火箭物品：更快的上升速度与更宽一格的跃迁范围。 */
    public static final DeferredItem<Item> TIER2_ROCKET_ITEM =
            ITEMS.registerSimpleItem("tier2_rocket", new Item.Properties().stacksTo(16));

    /** 三阶火箭物品：可前往最近行星与当前行星全部卫星。 */
    public static final DeferredItem<Item> TIER3_ROCKET_ITEM =
            ITEMS.registerSimpleItem("tier3_rocket", new Item.Properties().stacksTo(16));
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
                        output.accept(ELECTRIC_FURNACE_ITEM.get());
                        output.accept(ALLOY_SMELTER_ITEM.get());
                        output.accept(ENERGY_CELL_ITEM.get());
                        output.accept(LAUNCH_PAD_ITEM.get());
                        output.accept(ROCKET_ASSEMBLER_ITEM.get());
                        output.accept(ROCKET_ITEM.get());
                        output.accept(TIER2_ROCKET_ITEM.get());
                        output.accept(TIER3_ROCKET_ITEM.get());
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