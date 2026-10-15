package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.jupiter.network.JupiterStormPayload;
import com.tairitsutek.newgc.Planets.mars.network.MarsDustStormPayload;
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

        registrar.playToClient(
                JupiterStormPayload.TYPE,
                JupiterStormPayload.STREAM_CODEC,
                JupiterStormPayload::handle
        );

        registrar.playToClient(
                MarsVolcanoPayload.TYPE,
                MarsVolcanoPayload.STREAM_CODEC,
                MarsVolcanoPayload::handle
        );

        // 新增：火星沙尘暴
        registrar.playToClient(
                MarsDustStormPayload.TYPE,
                MarsDustStormPayload.STREAM_CODEC,
                MarsDustStormPayload::handle
        );
    }
}