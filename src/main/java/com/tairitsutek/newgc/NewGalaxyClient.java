package com.tairitsutek.newgc;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class NewGalaxyClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NewGalaxyMain.LOGGER.info("HELLO FROM CLIENT SETUP");
        NewGalaxyMain.LOGGER.info("MINECRAFT NAME >> {}",
                net.minecraft.client.Minecraft.getInstance().getUser().getName());
    }
}