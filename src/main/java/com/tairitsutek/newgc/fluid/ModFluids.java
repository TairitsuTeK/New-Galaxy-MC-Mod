package com.tairitsutek.newgc.fluid;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, NewGalaxyMain.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, NewGalaxyMain.MODID);

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(NewGalaxyMain.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(NewGalaxyMain.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewGalaxyMain.MODID);

    // ==================== FluidType ====================
    public static final DeferredHolder<FluidType, FluidType> ACID_RAIN_TYPE =
            FLUID_TYPES.register("acid_rain", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.newgc.acid_rain")
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
                    .density(1200)
                    .viscosity(1100)
                    .temperature(300)));

    // ==================== Fluid 源 + 流动 ====================
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> ACID_RAIN_SOURCE =
            FLUIDS.register("acid_rain",
                    () -> new BaseFlowingFluid.Source(acidRainProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> ACID_RAIN_FLOWING =
            FLUIDS.register("acid_rain_flowing",
                    () -> new BaseFlowingFluid.Flowing(acidRainProperties()));

    // ==================== LiquidBlock ====================
    public static final DeferredBlock<LiquidBlock> ACID_RAIN_BLOCK =
            BLOCKS.register("acid_rain", () ->
                    new LiquidBlock(ACID_RAIN_SOURCE.get(),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                                    .noCollission()
                                    .strength(100.0F)
                                    .noOcclusion()));

    // ==================== BucketItem ====================
    public static final DeferredItem<BucketItem> ACID_RAIN_BUCKET =
            ITEMS.register("acid_rain_bucket", () ->
                    new BucketItem(ACID_RAIN_SOURCE.get(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    // ==================== Properties ====================
    public static BaseFlowingFluid.Properties acidRainProperties() {
        return new BaseFlowingFluid.Properties(
                ACID_RAIN_TYPE,
                ACID_RAIN_SOURCE,
                ACID_RAIN_FLOWING
        ).block(ACID_RAIN_BLOCK).bucket(ACID_RAIN_BUCKET);
    }

    // ==================== Creative Tab ==================
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LIQUID_TAB =
            CREATIVE_MODE_TABS.register("liquid_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newgc.liquids"))
                    .icon(() -> new ItemStack(ACID_RAIN_BUCKET.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ACID_RAIN_BUCKET.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}