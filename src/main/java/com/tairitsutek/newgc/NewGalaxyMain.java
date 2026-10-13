package com.tairitsutek.newgc;

import com.mojang.logging.LogUtils;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.block.PlanetsBlock;
import com.tairitsutek.newgc.block.entity.ModBlockEntities;
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
        ModDimensions.register();
        MachinesBlock.register(modEventBus);
        Material.register(modEventBus);
        ModRecipes.register(modEventBus);

    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                OxygenSyncPacket.TYPE,
                OxygenSyncPacket.STREAM_CODEC,
                OxygenSyncPacket::handle
        );

        registrar.playToServer(
                ResetTankPacket.TYPE,
                ResetTankPacket.STREAM_CODEC,
                ResetTankPacket::handle
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}