package com.tairitsutek.newgc.event;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketLaunchService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 无目标漂流发射的聊天框确认：只有存在待确认请求时才拦截 {@code confirm}。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public final class RocketLaunchConfirmHandler {

    private RocketLaunchConfirmHandler() {
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (RocketLaunchService.tryConfirm(player, event.getRawText())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RocketLaunchService.clear(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RocketLaunchService.clear(player);
        }
    }
}
