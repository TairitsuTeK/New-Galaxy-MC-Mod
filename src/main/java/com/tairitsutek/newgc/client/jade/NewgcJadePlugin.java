package com.tairitsutek.newgc.client.jade;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.AlloySmelterBlock;
import com.tairitsutek.newgc.block.CompressorBlock;
import com.tairitsutek.newgc.block.ElectricFurnaceBlock;
import com.tairitsutek.newgc.block.EnergyCellBlock;
import com.tairitsutek.newgc.block.OxygenExtractorBlock;
import com.tairitsutek.newgc.block.RocketAssemblerBlock;
import com.tairitsutek.newgc.block.SolidFuelGeneratorBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade 可选兼容层。
 * <p>只有安装了 Jade 时 Jade 才会扫描并加载这个类；未安装时不会影响模组启动。
 */
@WailaPlugin(NewGalaxyMain.MODID)
public final class NewgcJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                SolidFuelGeneratorBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                OxygenExtractorBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                CompressorBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                ElectricFurnaceBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                AlloySmelterBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                EnergyCellBlock.class);
        registration.registerBlockComponent(MachineStatusProvider.INSTANCE,
                RocketAssemblerBlock.class);
    }
}
