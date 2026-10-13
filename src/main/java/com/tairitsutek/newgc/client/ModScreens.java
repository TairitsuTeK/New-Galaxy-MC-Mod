package com.tairitsutek.newgc.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.client.gui.CompressorScreen;
import com.tairitsutek.newgc.client.gui.SolidFuelGeneratorScreen;
import com.tairitsutek.newgc.client.gui.OxygenExtractorScreen;
import com.tairitsutek.newgc.menu.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class ModScreens {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SOLID_FUEL_GENERATOR.get(), SolidFuelGeneratorScreen::new);
        event.register(ModMenus.OXYGEN_EXTRACTOR.get(), OxygenExtractorScreen::new);
        event.register(ModMenus.COMPRESSOR.get(), CompressorScreen::new);
    }
}
