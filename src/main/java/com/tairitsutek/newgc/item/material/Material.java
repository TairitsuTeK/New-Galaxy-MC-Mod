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

import java.util.List;

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

    //region 金属粒
    public static final DeferredItem<Item> TIN_NUGGET = registerItem("tin_nugget");
    public static final DeferredItem<Item> ALUMINUM_NUGGET = registerItem("aluminum_nugget");
    public static final DeferredItem<Item> SILVER_NUGGET = registerItem("silver_nugget");
    //endregion

    //region 板材
    public static final DeferredItem<Item> IRON_PLATE = registerItem("iron_plate");
    public static final DeferredItem<Item> COPPER_PLATE = registerItem("copper_plate");
    public static final DeferredItem<Item> GOLD_PLATE = registerItem("gold_plate");
    public static final DeferredItem<Item> DIAMOND_PLATE = registerItem("diamond_plate");
    public static final DeferredItem<Item> LAPIS_PLATE = registerItem("lapis_plate");
    public static final DeferredItem<Item> EMERALD_PLATE = registerItem("emerald_plate");
    public static final DeferredItem<Item> NETHERITE_PLATE = registerItem("netherite_plate");
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

    //region 扩展金属与合金
    /**
     * 一套完整的基础金属材料：原料、锭、粒、板、储存块。
     */
    public record MetalSet(DeferredItem<Item> raw, DeferredItem<Item> ingot,
                           DeferredItem<Item> nugget, DeferredItem<Item> plate,
                           DeferredBlock<Block> block, DeferredItem<BlockItem> blockItem) {
    }

    /**
     * 合金没有原料形态，只包含锭、粒、板和储存块。
     */
    public record AlloySet(DeferredItem<Item> ingot, DeferredItem<Item> nugget,
                           DeferredItem<Item> plate, DeferredBlock<Block> block,
                           DeferredItem<BlockItem> blockItem) {
    }

    public static final MetalSet LEAD = registerMetal("lead");
    public static final MetalSet NICKEL = registerMetal("nickel");
    public static final MetalSet PLATINUM = registerMetal("platinum");
    public static final MetalSet TITANIUM = registerMetal("titanium");
    public static final MetalSet URANIUM = registerMetal("uranium");
    public static final MetalSet ZINC = registerMetal("zinc");

    public static final AlloySet BRONZE = registerAlloy("bronze");
    public static final AlloySet STEEL = registerAlloy("steel");
    public static final AlloySet ELECTRUM = registerAlloy("electrum");
    public static final AlloySet INVAR = registerAlloy("invar");

    public static final List<MetalSet> BASE_METALS = List.of(
            LEAD, NICKEL, PLATINUM, TITANIUM, URANIUM, ZINC);
    public static final List<AlloySet> ALLOYS = List.of(
            BRONZE, STEEL, ELECTRUM, INVAR);

    private static MetalSet registerMetal(String name) {
        DeferredItem<Item> raw = registerItem("raw_" + name);
        DeferredItem<Item> ingot = registerItem(name + "_ingot");
        DeferredItem<Item> nugget = registerItem(name + "_nugget");
        DeferredItem<Item> plate = registerItem(name + "_plate");
        DeferredBlock<Block> block = registerMetalBlock(name + "_block");
        DeferredItem<BlockItem> blockItem = registerBlockItem(name + "_block", block);
        return new MetalSet(raw, ingot, nugget, plate, block, blockItem);
    }

    private static AlloySet registerAlloy(String name) {
        DeferredItem<Item> ingot = registerItem(name + "_ingot");
        DeferredItem<Item> nugget = registerItem(name + "_nugget");
        DeferredItem<Item> plate = registerItem(name + "_plate");
        DeferredBlock<Block> block = registerMetalBlock(name + "_block");
        DeferredItem<BlockItem> blockItem = registerBlockItem(name + "_block", block);
        return new AlloySet(ingot, nugget, plate, block, blockItem);
    }

    private static DeferredBlock<Block> registerMetalBlock(String name) {
        return registerBlock(name, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(5f, 6f)
                .requiresCorrectToolForDrops()));
    }
    //endregion

    //region 电路材料
    public static final DeferredItem<Item> CIRCUIT_CORE_COPPER = registerItem("circuit_core_copper");
    public static final DeferredItem<Item> CIRCUIT_CORE_IRON = registerItem("circuit_core_iron");
    public static final DeferredItem<Item> CIRCUIT_CORE_GOLD = registerItem("circuit_core_golden");
    public static final DeferredItem<Item> CIRCUIT_CORE_DIAMOND = registerItem("circuit_core_diamond");
    public static final DeferredItem<Item> CIRCUIT_CORE_NETHER = registerItem("circuit_core_nether");
    //endregion

    //region 火箭材料与部件
    public static final DeferredItem<Item> HEAVY_ALLOY = registerItem("heavy_alloy");
    public static final DeferredItem<Item> HEAVY_ALLOY_PLATE = registerItem("heavy_alloy_plate");
    public static final DeferredItem<Item> TIER1_NOSE_CONE = registerItem("tier1_nose_cone");
    public static final DeferredItem<Item> ROCKET_ENGINE = registerItem("rocket_engine");
    public static final DeferredItem<Item> BOOSTER = registerItem("booster");
    public static final DeferredItem<Item> TIER2_NOSE_CONE = registerItem("tier2_nose_cone");
    public static final DeferredItem<Item> ADVANCED_ROCKET_ENGINE = registerItem("advanced_rocket_engine");
    public static final DeferredItem<Item> TIER3_NOSE_CONE = registerItem("tier3_nose_cone");
    public static final DeferredItem<Item> TIER3_ROCKET_ENGINE = registerItem("tier3_rocket_engine");
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

                        // 金属粒
                        output.accept(TIN_NUGGET.get());
                        output.accept(ALUMINUM_NUGGET.get());
                        output.accept(SILVER_NUGGET.get());

                        // 扩展基础金属
                        for (MetalSet set : BASE_METALS) {
                            output.accept(set.raw().get());
                            output.accept(set.ingot().get());
                            output.accept(set.nugget().get());
                            output.accept(set.plate().get());
                        }

                        // 合金
                        for (AlloySet set : ALLOYS) {
                            output.accept(set.ingot().get());
                            output.accept(set.nugget().get());
                            output.accept(set.plate().get());
                        }

                        // 金属板
                        output.accept(IRON_PLATE.get());
                        output.accept(COPPER_PLATE.get());
                        output.accept(TIN_PLATE.get());
                        output.accept(ALUMINUM_PLATE.get());
                        output.accept(SILVER_PLATE.get());
                        output.accept(GOLD_PLATE.get());
                        output.accept(DIAMOND_PLATE.get());
                        output.accept(LAPIS_PLATE.get());
                        output.accept(EMERALD_PLATE.get());
                        output.accept(NETHERITE_PLATE.get());

                        // 电路之心
                        output.accept(CIRCUIT_CORE_COPPER.get());
                        output.accept(CIRCUIT_CORE_IRON.get());
                        output.accept(CIRCUIT_CORE_GOLD.get());
                        output.accept(CIRCUIT_CORE_DIAMOND.get());
                        output.accept(CIRCUIT_CORE_NETHER.get());

                        // 重型合金与 TBMK 工作台零件
                        output.accept(HEAVY_ALLOY.get());
                        output.accept(HEAVY_ALLOY_PLATE.get());
                        output.accept(TIER1_NOSE_CONE.get());
                        output.accept(ROCKET_ENGINE.get());
                        output.accept(BOOSTER.get());
                        output.accept(TIER2_NOSE_CONE.get());
                        output.accept(ADVANCED_ROCKET_ENGINE.get());
                        output.accept(TIER3_NOSE_CONE.get());
                        output.accept(TIER3_ROCKET_ENGINE.get());

                        // 矿物块
                        output.accept(ALUMINUM_BLOCK_ITEM.get());
                        output.accept(TIN_BLOCK_ITEM.get());
                        output.accept(SILVER_BLOCK_ITEM.get());

                        for (MetalSet set : BASE_METALS) {
                            output.accept(set.blockItem().get());
                        }
                        for (AlloySet set : ALLOYS) {
                            output.accept(set.blockItem().get());
                        }
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