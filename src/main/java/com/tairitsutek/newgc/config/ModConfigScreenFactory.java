package com.tairitsutek.newgc.config;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class ModConfigScreenFactory {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModContainer container = ModList.get()
                    .getModContainerById(NewGalaxyMain.MODID)
                    .orElseThrow();
            container.registerExtensionPoint(
                    IConfigScreenFactory.class,
                    (IConfigScreenFactory) (mod, parent) -> new NewGalaxyConfigScreen(parent)
            );
        });
    }
}