package com.tairitsutek.newgc.capability;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.entity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class CapabilitySetup {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 固体燃料发电机
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.SOLID_FUEL_GENERATOR.get(),
                (be, side) -> be.getEnergyStorage()
        );

        // 导线
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.WIRE.get(),
                (be, side) -> be.getEnergyStorage()
        );

        // 制氧机
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.OXYGEN_EXTRACTOR.get(),
                (be, side) -> be.getEnergyStorage()
        );

        // 压缩机
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.COMPRESSOR.get(),
                (be, side) -> be.getEnergyStorage()
        );
    }
}
