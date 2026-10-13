package com.tairitsutek.newgc.item.material;

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

public class Material {

    //region 注册器
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewGalaxyMain.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NewGalaxyMain.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewGalaxyMain.MODID);
    //endregion

    //region 矿物原料
    public static final DeferredItem<Item> RAW_TIN = registerItem("raw_tin");
    public static final DeferredItem<Item> RAW_ALUMINUM = registerItem("raw_aluminum");
    public static final DeferredItem<Item> RAW_SILVER = registerItem("raw_silver");
    //endregion

    //region 金属锭
    public static final DeferredItem<Item> TIN_INGOT = registerItem("tin_ingot");
    public static final DeferredItem<Item> ALUMINUM_INGOT = registerItem("aluminum_ingot");
    public static final DeferredItem<Item> SILVER_INGOT = registerItem("silver_ingot");
    //endregion

    //region 金属板
    public static final DeferredItem<Item> IRON_PLATE = registerItem("iron_plate");
    public static final DeferredItem<Item> COPPER_PLATE = registerItem("copper_plate");
    public static final DeferredItem<Item> TIN_PLATE = registerItem("tin_plate");
    public static final DeferredItem<Item> ALUMINUM_PLATE = registerItem("aluminum_plate");
    public static final DeferredItem<Item> SILVER_PLATE = registerItem("silver_plate");
    //endregion

    //region 矿物块
    public static final DeferredBlock<Block> ALUMINUM_BLOCK = registerBlock("aluminum_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> TIN_BLOCK = registerBlock("tin_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SILVER_BLOCK = registerBlock("silver_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(5f, 6f).requiresCorrectToolForDrops()));
    //endregion

    //region 矿物块物品
    public static final DeferredItem<BlockItem> ALUMINUM_BLOCK_ITEM = registerBlockItem("aluminum_block", ALUMINUM_BLOCK);
    public static final DeferredItem<BlockItem> TIN_BLOCK_ITEM = registerBlockItem("tin_block", TIN_BLOCK);
    public static final DeferredItem<BlockItem> SILVER_BLOCK_ITEM = registerBlockItem("silver_block", SILVER_BLOCK);
    //endregion

    //region 注册辅助
    private static DeferredItem<Item> registerItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static DeferredBlock<Block> registerBlock(String name, java.util.function.Supplier<Block> supplier) {
        return BLOCKS.register(name, supplier);
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block);
    }
    //endregion

    //region 创造模式物品栏
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIALS_TAB =
            CREATIVE_MODE_TABS.register("materials_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newgc.materials"))
                    .icon(() -> new ItemStack(IRON_PLATE.get()))
                    .displayItems((parameters, output) -> {
                        // 矿物原料
                        output.accept(RAW_TIN.get());
                        output.accept(RAW_ALUMINUM.get());
                        output.accept(RAW_SILVER.get());

                        // 金属锭
                        output.accept(TIN_INGOT.get());
                        output.accept(ALUMINUM_INGOT.get());
                        output.accept(SILVER_INGOT.get());

                        // 金属板
                        output.accept(IRON_PLATE.get());
                        output.accept(COPPER_PLATE.get());
                        output.accept(TIN_PLATE.get());
                        output.accept(ALUMINUM_PLATE.get());
                        output.accept(SILVER_PLATE.get());

                        // 矿物块
                        output.accept(ALUMINUM_BLOCK_ITEM.get());
                        output.accept(TIN_BLOCK_ITEM.get());
                        output.accept(SILVER_BLOCK_ITEM.get());
                    })
                    .build());
    //endregion

    //region 注册入口
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
    //endregion
}