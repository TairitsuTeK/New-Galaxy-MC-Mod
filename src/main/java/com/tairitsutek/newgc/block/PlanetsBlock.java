package com.tairitsutek.newgc.block;

import com.mojang.serialization.MapCodec;
import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public final class PlanetsBlock {

    //region 注册器
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NewGalaxyMain.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewGalaxyMain.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewGalaxyMain.MODID);
    //endregion

    //region 主世界矿物
    public static final DeferredBlock<Block> TIN_ORE = registerBlock("tin_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(3f, 4.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> ALUMINUM_ORE = registerBlock("aluminum_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(3f, 4.5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SILVER_ORE = registerBlock("silver_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(3f, 4.5f).requiresCorrectToolForDrops()));
    //endregion

    //region 星球地形方块
    public static final DeferredBlock<Block> MOON_SOIL = registerBlock("moon_soil", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.GRAVEL).strength(1f, 1f)));
    public static final DeferredBlock<Block> MOON_SUB_SURFACE = registerBlock("moon_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MOON_STONE = registerBlock("moon_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(6f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MOON_ORE_IRON = registerBlock("moon_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(4f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MOON_ORE_COPPER = registerBlock("moon_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(4f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MERCURY_STONE = registerBlock("mercury_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(7f, 7f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MERCURY_SUB_SURFACE = registerBlock("mercury_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(7f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MERCURY_SURFACE = registerBlock("mercury_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(6f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MERCURY_ORE_IRON = registerBlock("mercury_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(7f, 7f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MERCURY_ORE_COPPER = registerBlock("mercury_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(7f, 7f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VENUS_SURFACE = registerBlock("venus_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 5f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VENUS_SUB_SURFACE = registerBlock("venus_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VENUS_STONE = registerBlock("venus_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(6f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VENUS_ORE_IRON = registerBlock("venus_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(6f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> VENUS_ORE_COPPER = registerBlock("venus_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(6f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CERES_SURFACE = registerBlock("ceres_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CERES_SUB_SURFACE = registerBlock("ceres_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CERES_STONE = registerBlock("ceres_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(3f, 3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CERES_ORE_IRON = registerBlock("ceres_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(3f, 3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CERES_ORE_COPPER = registerBlock("ceres_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(3f, 3f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MARS_SURFACE = registerBlock("mars_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MARS_SUB_SURFACE = registerBlock("mars_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MARS_STONE = registerBlock("mars_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MARS_ORE_IRON = registerBlock("mars_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> MARS_ORE_COPPER = registerBlock("mars_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> PHOBOS_SURFACE = registerBlock("phobos_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> PHOBOS_SUB_SURFACE = registerBlock("phobos_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> PHOBOS_STONE = registerBlock("phobos_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> PHOBOS_ORE_IRON = registerBlock("phobos_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> PHOBOS_ORE_COPPER = registerBlock("phobos_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEIMOS_SURFACE = registerBlock("deimos_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEIMOS_SUB_SURFACE = registerBlock("deimos_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEIMOS_STONE = registerBlock("deimos_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEIMOS_ORE_IRON = registerBlock("deimos_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEIMOS_ORE_COPPER = registerBlock("deimos_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> JUPITER_GRAVEL = registerBlock("jupiter_gravel", () -> new FallingPlanetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).sound(SoundType.GRAVEL).strength(1f, 1f)));
    public static final DeferredBlock<Block> JUPITER_SURFACE = registerBlock("jupiter_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> JUPITER_SUB_SURFACE = registerBlock("jupiter_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(2f, 2f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> JUPITER_STONE = registerBlock("jupiter_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> JUPITER_ORE_IRON = registerBlock("jupiter_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> JUPITER_ORE_COPPER = registerBlock("jupiter_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> SULFUR = registerBlock("sulfur", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.GRAVEL).strength(2f, 1f)));
    public static final DeferredBlock<Block> IO_SUB_SURFACE = registerBlock("io_sub_surface", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> IO_STONE = registerBlock("io_stone", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f,6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> IO_ORE_IRON = registerBlock("io_ore_iron", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f, 6f).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> IO_ORE_COPPER = registerBlock("io_ore_copper", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).sound(SoundType.STONE).strength(5f,6f).requiresCorrectToolForDrops()));
    //endregion

    //region 方块物品
    public static final DeferredItem<BlockItem> TIN_ORE_ITEM = registerItem("tin_ore", TIN_ORE);
    public static final DeferredItem<BlockItem> ALUMINUM_ORE_ITEM = registerItem("aluminum_ore", ALUMINUM_ORE);
    public static final DeferredItem<BlockItem> SILVER_ORE_ITEM = registerItem("silver_ore", SILVER_ORE);
    public static final DeferredItem<BlockItem> MOON_SOIL_ITEM = registerItem("moon_soil", MOON_SOIL);
    public static final DeferredItem<BlockItem> MOON_SUB_SURFACE_ITEM = registerItem("moon_sub_surface", MOON_SUB_SURFACE);
    public static final DeferredItem<BlockItem> MOON_STONE_ITEM = registerItem("moon_stone", MOON_STONE);
    public static final DeferredItem<BlockItem> MOON_ORE_IRON_ITEM = registerItem("moon_ore_iron", MOON_ORE_IRON);
    public static final DeferredItem<BlockItem> MOON_ORE_COPPER_ITEM = registerItem("moon_ore_copper", MOON_ORE_COPPER);
    public static final DeferredItem<BlockItem> MERCURY_STONE_ITEM = registerItem("mercury_stone", MERCURY_STONE);
    public static final DeferredItem<BlockItem> MERCURY_SUB_SURFACE_ITEM = registerItem("mercury_sub_surface", MERCURY_SUB_SURFACE);
    public static final DeferredItem<BlockItem> MERCURY_SURFACE_ITEM = registerItem("mercury_surface", MERCURY_SURFACE);
    public static final DeferredItem<BlockItem> MERCURY_ORE_IRON_ITEM = registerItem("mercury_ore_iron", MERCURY_ORE_IRON);
    public static final DeferredItem<BlockItem> MERCURY_ORE_COPPER_ITEM = registerItem("mercury_ore_copper", MERCURY_ORE_COPPER);
    public static final DeferredItem<BlockItem> VENUS_SURFACE_ITEM = registerItem("venus_surface", VENUS_SURFACE);
    public static final DeferredItem<BlockItem> VENUS_SUB_SURFACE_ITEM = registerItem("venus_sub_surface", VENUS_SUB_SURFACE);
    public static final DeferredItem<BlockItem> VENUS_STONE_ITEM = registerItem("venus_stone", VENUS_STONE);
    public static final DeferredItem<BlockItem> VENUS_ORE_IRON_ITEM = registerItem("venus_ore_iron", VENUS_ORE_IRON);
    public static final DeferredItem<BlockItem> VENUS_ORE_COPPER_ITEM = registerItem("venus_ore_copper", VENUS_ORE_COPPER);
    public static final DeferredItem<BlockItem> CERES_SURFACE_ITEM = registerItem("ceres_surface", CERES_SURFACE);
    public static final DeferredItem<BlockItem> CERES_SUB_SURFACE_ITEM = registerItem("ceres_sub_surface", CERES_SUB_SURFACE);
    public static final DeferredItem<BlockItem> CERES_STONE_ITEM = registerItem("ceres_stone", CERES_STONE);
    public static final DeferredItem<BlockItem> CERES_ORE_IRON_ITEM = registerItem("ceres_ore_iron", CERES_ORE_IRON);
    public static final DeferredItem<BlockItem> CERES_ORE_COPPER_ITEM = registerItem("ceres_ore_copper", CERES_ORE_COPPER);
    public static final DeferredItem<BlockItem> MARS_SURFACE_ITEM = registerItem("mars_surface", MARS_SURFACE);
    public static final DeferredItem<BlockItem> MARS_SUB_SURFACE_ITEM = registerItem("mars_sub_surface", MARS_SUB_SURFACE);
    public static final DeferredItem<BlockItem> MARS_STONE_ITEM = registerItem("mars_stone", MARS_STONE);
    public static final DeferredItem<BlockItem> MARS_ORE_IRON_ITEM = registerItem("mars_ore_iron", MARS_ORE_IRON);
    public static final DeferredItem<BlockItem> MARS_ORE_COPPER_ITEM = registerItem("mars_ore_copper", MARS_ORE_COPPER);
    public static final DeferredItem<BlockItem> PHOBOS_SURFACE_ITEM = registerItem("phobos_surface", PHOBOS_SURFACE);
    public static final DeferredItem<BlockItem> PHOBOS_SUB_SURFACE_ITEM = registerItem("phobos_sub_surface", PHOBOS_SUB_SURFACE);
    public static final DeferredItem<BlockItem> PHOBOS_STONE_ITEM = registerItem("phobos_stone", PHOBOS_STONE);
    public static final DeferredItem<BlockItem> PHOBOS_ORE_IRON_ITEM = registerItem("phobos_ore_iron", PHOBOS_ORE_IRON);
    public static final DeferredItem<BlockItem> PHOBOS_ORE_COPPER_ITEM = registerItem("phobos_ore_copper", PHOBOS_ORE_COPPER);
    public static final DeferredItem<BlockItem> DEIMOS_SURFACE_ITEM = registerItem("deimos_surface", DEIMOS_SURFACE);
    public static final DeferredItem<BlockItem> DEIMOS_SUB_SURFACE_ITEM = registerItem("deimos_sub_surface", DEIMOS_SUB_SURFACE);
    public static final DeferredItem<BlockItem> DEIMOS_STONE_ITEM = registerItem("deimos_stone", DEIMOS_STONE);
    public static final DeferredItem<BlockItem> DEIMOS_ORE_IRON_ITEM = registerItem("deimos_ore_iron", DEIMOS_ORE_IRON);
    public static final DeferredItem<BlockItem> DEIMOS_ORE_COPPER_ITEM = registerItem("deimos_ore_copper", DEIMOS_ORE_COPPER);
    public static final DeferredItem<BlockItem> JUPITER_GRAVEL_ITEM = registerItem("jupiter_gravel", JUPITER_GRAVEL);
    public static final DeferredItem<BlockItem> JUPITER_SURFACE_ITEM = registerItem("jupiter_surface", JUPITER_SURFACE);
    public static final DeferredItem<BlockItem> JUPITER_SUB_SURFACE_ITEM = registerItem("jupiter_sub_surface", JUPITER_SUB_SURFACE);
    public static final DeferredItem<BlockItem> JUPITER_STONE_ITEM = registerItem("jupiter_stone", JUPITER_STONE);
    public static final DeferredItem<BlockItem> JUPITER_ORE_IRON_ITEM = registerItem("jupiter_ore_iron", JUPITER_ORE_IRON);
    public static final DeferredItem<BlockItem> JUPITER_ORE_COPPER_ITEM = registerItem("jupiter_ore_copper", JUPITER_ORE_COPPER);
    public static final DeferredItem<BlockItem> SULFUR_ITEM = registerItem("sulfur", SULFUR);
    public static final DeferredItem<BlockItem> IO_SUB_SURFACE_ITEM = registerItem("io_sub_surface", IO_SUB_SURFACE);
    public static final DeferredItem<BlockItem> IO_STONE_ITEM = registerItem("io_stone", IO_STONE);
    public static final DeferredItem<BlockItem> IO_ORE_IRON_ITEM = registerItem("io_ore_iron", IO_ORE_IRON);
    public static final DeferredItem<BlockItem> IO_ORE_COPPER_ITEM = registerItem("io_ore_copper", IO_ORE_COPPER);
    //endregion

    //region 注册辅助
    private static DeferredBlock<Block> registerBlock(String name, java.util.function.Supplier<Block> supplier) {
        return BLOCKS.register(name, supplier);
    }

    private static DeferredItem<BlockItem> registerItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block);
    }
    //endregion

    //region 内部下坠方块
    private static class FallingPlanetBlock extends FallingBlock {
        private static final MapCodec<FallingPlanetBlock> CODEC = simpleCodec(FallingPlanetBlock::new);

        FallingPlanetBlock(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        protected @NotNull MapCodec<? extends FallingPlanetBlock> codec() {
            return CODEC;
        }
    }
    //endregion

    //region 创造模式物品栏
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCK_TAB =
            CREATIVE_MODE_TABS.register("newgc_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newgc"))
                    .icon(() -> new ItemStack(MERCURY_STONE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(TIN_ORE_ITEM.get());
                        output.accept(ALUMINUM_ORE_ITEM.get());
                        output.accept(SILVER_ORE_ITEM.get());

                        output.accept(MOON_SOIL_ITEM.get());
                        output.accept(MOON_SUB_SURFACE_ITEM.get());
                        output.accept(MOON_STONE_ITEM.get());
                        output.accept(MOON_ORE_IRON_ITEM.get());
                        output.accept(MOON_ORE_COPPER_ITEM.get());

                        output.accept(MERCURY_STONE_ITEM.get());
                        output.accept(MERCURY_SUB_SURFACE_ITEM.get());
                        output.accept(MERCURY_SURFACE_ITEM.get());
                        output.accept(MERCURY_ORE_IRON_ITEM.get());
                        output.accept(MERCURY_ORE_COPPER_ITEM.get());

                        output.accept(VENUS_SURFACE_ITEM.get());
                        output.accept(VENUS_SUB_SURFACE_ITEM.get());
                        output.accept(VENUS_STONE_ITEM.get());
                        output.accept(VENUS_ORE_IRON_ITEM.get());
                        output.accept(VENUS_ORE_COPPER_ITEM.get());

                        output.accept(CERES_SURFACE_ITEM.get());
                        output.accept(CERES_SUB_SURFACE_ITEM.get());
                        output.accept(CERES_STONE_ITEM.get());
                        output.accept(CERES_ORE_IRON_ITEM.get());
                        output.accept(CERES_ORE_COPPER_ITEM.get());

                        output.accept(MARS_SURFACE_ITEM.get());
                        output.accept(MARS_SUB_SURFACE_ITEM.get());
                        output.accept(MARS_STONE_ITEM.get());
                        output.accept(MARS_ORE_IRON_ITEM.get());
                        output.accept(MARS_ORE_COPPER_ITEM.get());

                        output.accept(PHOBOS_SURFACE_ITEM.get());
                        output.accept(PHOBOS_SUB_SURFACE_ITEM.get());
                        output.accept(PHOBOS_STONE_ITEM.get());
                        output.accept(PHOBOS_ORE_IRON_ITEM.get());
                        output.accept(PHOBOS_ORE_COPPER_ITEM.get());

                        output.accept(DEIMOS_SURFACE_ITEM.get());
                        output.accept(DEIMOS_SUB_SURFACE_ITEM.get());
                        output.accept(DEIMOS_STONE_ITEM.get());
                        output.accept(DEIMOS_ORE_IRON_ITEM.get());
                        output.accept(DEIMOS_ORE_COPPER_ITEM.get());

                        output.accept(JUPITER_GRAVEL_ITEM.get());
                        output.accept(JUPITER_SURFACE_ITEM.get());
                        output.accept(JUPITER_SUB_SURFACE_ITEM.get());
                        output.accept(JUPITER_STONE_ITEM.get());
                        output.accept(JUPITER_ORE_IRON_ITEM.get());
                        output.accept(JUPITER_ORE_COPPER_ITEM.get());

                        output.accept(SULFUR_ITEM.get());
                        output.accept(IO_SUB_SURFACE_ITEM.get());
                        output.accept(IO_STONE_ITEM.get());
                        output.accept(IO_ORE_IRON_ITEM.get());
                        output.accept(IO_ORE_COPPER_ITEM.get());
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