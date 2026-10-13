package com.tairitsutek.newgc.fluid;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class ModFluidClientExtensions {

    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL =
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "block/acid_still");
            private static final ResourceLocation FLOW =
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "block/acid_flow");

            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFFD8EE25;
            }
        }, ModFluids.ACID_RAIN_TYPE.get());
    }
}