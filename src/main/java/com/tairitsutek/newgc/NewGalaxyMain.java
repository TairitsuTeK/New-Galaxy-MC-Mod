package com.tairitsutek.newgc;

import com.mojang.logging.LogUtils;
import com.tairitsutek.newgc.Moons.io.network.IoQuakePayload;
import com.tairitsutek.newgc.Moons.io.network.IoVolcanoDebugPayload; // ★ 新增
import com.tairitsutek.newgc.Moons.io.network.IoVolcanoPayload;      // ★ 新增
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.network.JupiterStormPayload;
import com.tairitsutek.newgc.Planets.mars.network.MarsDustStormPayload;
import com.tairitsutek.newgc.Planets.mars.network.MarsVolcanoDebugPayload;
import com.tairitsutek.newgc.Planets.mars.network.MarsVolcanoPayload;
import com.tairitsutek.newgc.Planets.mercury.network.MercurySolarFlarePayload;
import com.tairitsutek.newgc.block.PlanetsBlock;
import com.tairitsutek.newgc.block.entity.ModBlockEntities;
import com.tairitsutek.newgc.config.NewGalaxyConfig;
import com.tairitsutek.newgc.item.material.Material;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.menu.ModMenus;
import com.tairitsutek.newgc.event.oxygen.ModAttachments;
import com.tairitsutek.newgc.event.oxygen.OxygenSyncPacket;
import com.tairitsutek.newgc.event.oxygen.ResetTankPacket;
import com.tairitsutek.newgc.item.ToolsItem;
import com.tairitsutek.newgc.recipe.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(NewGalaxyMain.MODID)
public class NewGalaxyMain {
    public static final String MODID = "newgc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NewGalaxyMain(IEventBus modEventBus, ModContainer modContainer) {
        PlanetsBlock.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ToolsItem.register(modEventBus);
        com.tairitsutek.newgc.fluid.ModFluids.register(modEventBus);
        ModAttachments.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, NewGalaxyConfig.SPEC);

        ModDimensions.register();
        MachinesBlock.register(modEventBus);
        Material.register(modEventBus);
        ModRecipes.register(modEventBus);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // ========== S2C ==========
        registrar.playToClient(
                OxygenSyncPacket.TYPE,
                OxygenSyncPacket.STREAM_CODEC,
                OxygenSyncPacket::handle
        );
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
        registrar.playToClient(
                MarsDustStormPayload.TYPE,
                MarsDustStormPayload.STREAM_CODEC,
                MarsDustStormPayload::handle
        );

        registrar.playToClient(
                MercurySolarFlarePayload.TYPE,
                MercurySolarFlarePayload.STREAM_CODEC,
                MercurySolarFlarePayload::handle
        );

        registrar.playToClient(
                IoQuakePayload.TYPE,
                IoQuakePayload.STREAM_CODEC,
                IoQuakePayload::handle
        );

        registrar.playToClient(
                IoVolcanoPayload.TYPE,
                IoVolcanoPayload.STREAM_CODEC,
                IoVolcanoPayload::handle
        );

        registrar.playToServer(
                ResetTankPacket.TYPE,
                ResetTankPacket.STREAM_CODEC,
                ResetTankPacket::handle
        );
        registrar.playToServer(
                MarsVolcanoDebugPayload.TYPE,
                MarsVolcanoDebugPayload.STREAM_CODEC,
                MarsVolcanoDebugPayload::handle
        );

        registrar.playToServer(
                IoVolcanoDebugPayload.TYPE,
                IoVolcanoDebugPayload.STREAM_CODEC,
                IoVolcanoDebugPayload::handle
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}