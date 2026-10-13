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

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
