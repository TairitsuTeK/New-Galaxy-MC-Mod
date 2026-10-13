package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsClientSetup {

    public static final ResourceLocation MARS_SKY =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars_sky");

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(MARS_SKY, new MarsSkyEffects());
    }
}