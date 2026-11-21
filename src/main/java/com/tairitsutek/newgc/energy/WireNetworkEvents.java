package com.tairitsutek.newgc.energy;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 在维度卸载/服务器停止时清理网络缓存，避免残留 ServerLevel 引用。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public final class WireNetworkEvents {

    private WireNetworkEvents() {
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WireNetworkManager.clear(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        WireNetworkManager.clearAll();
    }
}
