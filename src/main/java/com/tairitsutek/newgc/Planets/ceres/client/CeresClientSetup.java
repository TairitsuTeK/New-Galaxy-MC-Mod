package com.tairitsutek.newgc.Planets.ceres.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)

public class CeresClientSetup {

    public static final ResourceLocation CERES_SKY =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "ceres_sky");

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(CERES_SKY, new CeresSkyEffects());
    }
}