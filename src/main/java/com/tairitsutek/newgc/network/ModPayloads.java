package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.jupiter.network.JupiterStormPayload;
import com.tairitsutek.newgc.Planets.mars.network.MarsVolcanoPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class ModPayloads {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // 木星风暴状态同步（S2C）
        registrar.playToClient(
                JupiterStormPayload.TYPE,
                JupiterStormPayload.STREAM_CODEC,
                JupiterStormPayload::handle
        );

        // 火星火山喷发状态同步（S2C）
        registrar.playToClient(
                MarsVolcanoPayload.TYPE,
                MarsVolcanoPayload.STREAM_CODEC,
                MarsVolcanoPayload::handle
        );
    }
}