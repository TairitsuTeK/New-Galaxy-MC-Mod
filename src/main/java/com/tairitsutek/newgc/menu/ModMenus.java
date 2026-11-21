package com.tairitsutek.newgc.menu;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, NewGalaxyMain.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SolidFuelGeneratorMenu>> SOLID_FUEL_GENERATOR =
            MENUS.register("solid_fuel_generator", () ->
                    IMenuTypeExtension.create(SolidFuelGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<OxygenExtractorMenu>> OXYGEN_EXTRACTOR =
            MENUS.register("oxygen_extractor", () ->
                    IMenuTypeExtension.create(OxygenExtractorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CompressorMenu>> COMPRESSOR =
            MENUS.register("compressor", () ->
                    IMenuTypeExtension.create(CompressorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () ->
                    IMenuTypeExtension.create(ElectricFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AlloySmelterMenu>> ALLOY_SMELTER =
            MENUS.register("alloy_smelter", () ->
                    IMenuTypeExtension.create(AlloySmelterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<EnergyCellMenu>> ENERGY_CELL =
            MENUS.register("energy_cell", () ->
                    IMenuTypeExtension.create(EnergyCellMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RocketStorageMenu>> ROCKET_STORAGE =
            MENUS.register("rocket_storage", () ->
                    IMenuTypeExtension.create(RocketStorageMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RocketAssemblerMenu>> ROCKET_ASSEMBLER =
            MENUS.register("rocket_assembler", () ->
                    IMenuTypeExtension.create(RocketAssemblerMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
