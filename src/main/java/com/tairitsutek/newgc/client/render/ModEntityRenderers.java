package com.tairitsutek.newgc.client.render;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.entity.ModBlockEntities;
import com.tairitsutek.newgc.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 实体模型层与渲染器注册（仅客户端）。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RocketModel.LAYER, RocketModel::createLayerDefinition);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ROCKET_ASSEMBLER.get(),
                RocketAssemblerRenderer::new);
    }
}
