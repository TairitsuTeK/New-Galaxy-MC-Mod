package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    //region 注册器
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NewGalaxyMain.MODID);
    //endregion

    //region 方块实体类型 - 机器

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolidFuelGeneratorBlockEntity>> SOLID_FUEL_GENERATOR =
            BLOCK_ENTITIES.register("solid_fuel_generator", () ->
                    BlockEntityType.Builder.of(
                            SolidFuelGeneratorBlockEntity::new,
                            MachinesBlock.SOLID_FUEL_GENERATOR.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxygenExtractorBlockEntity>> OXYGEN_EXTRACTOR =
            BLOCK_ENTITIES.register("oxygen_extractor", () ->
                    BlockEntityType.Builder.of(
                            OxygenExtractorBlockEntity::new,
                            MachinesBlock.OXYGEN_EXTRACTOR.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireBlockEntity>> WIRE =
            BLOCK_ENTITIES.register("wire", () ->
                    BlockEntityType.Builder.of(
                            WireBlockEntity::new,
                            MachinesBlock.WIRE.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompressorBlockEntity>> COMPRESSOR =
            BLOCK_ENTITIES.register("compressor", () ->
                    BlockEntityType.Builder.of(
                            CompressorBlockEntity::new,
                            MachinesBlock.COMPRESSOR.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITIES.register("electric_furnace", () ->
                    BlockEntityType.Builder.of(
                            ElectricFurnaceBlockEntity::new,
                            MachinesBlock.ELECTRIC_FURNACE.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloySmelterBlockEntity>> ALLOY_SMELTER =
            BLOCK_ENTITIES.register("alloy_smelter", () ->
                    BlockEntityType.Builder.of(
                            AlloySmelterBlockEntity::new,
                            MachinesBlock.ALLOY_SMELTER.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyCellBlockEntity>> ENERGY_CELL =
            BLOCK_ENTITIES.register("energy_cell", () ->
                    BlockEntityType.Builder.of(
                            EnergyCellBlockEntity::new,
                            MachinesBlock.ENERGY_CELL.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD =
            BLOCK_ENTITIES.register("launch_pad", () ->
                    BlockEntityType.Builder.of(
                            LaunchPadBlockEntity::new,
                            MachinesBlock.LAUNCH_PAD.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RocketAssemblerBlockEntity>> ROCKET_ASSEMBLER =
            BLOCK_ENTITIES.register("rocket_assembler", () ->
                    BlockEntityType.Builder.of(
                            RocketAssemblerBlockEntity::new,
                            MachinesBlock.ROCKET_ASSEMBLER.get()
                    ).build(null));
    //endregion

    //region 注册入口
    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
    //endregion
}