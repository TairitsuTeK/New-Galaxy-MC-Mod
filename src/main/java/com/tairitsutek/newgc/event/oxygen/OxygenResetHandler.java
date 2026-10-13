package com.tairitsutek.newgc.event.oxygen;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class OxygenResetHandler {

    private static final Map<UUID, Integer> CONFIRM_COUNT = new HashMap<>();
    private static final Map<UUID, Long> LAST_PRESS_TIME = new HashMap<>();

    private static final long CONFIRM_TIMEOUT_MS = 3000;
    private static final int REQUIRED_CONFIRMS = 3;

    public static void onResetKeyPressed(ServerPlayer player) {
        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();

        Long lastTime = LAST_PRESS_TIME.get(uuid);
        int count = CONFIRM_COUNT.getOrDefault(uuid, 0);

        if (lastTime == null || now - lastTime > CONFIRM_TIMEOUT_MS) {
            count = 0;
        }

        count++;
        LAST_PRESS_TIME.put(uuid, now);
        CONFIRM_COUNT.put(uuid, count);

        if (count >= REQUIRED_CONFIRMS) {
            OxygenData data = player.getData(ModAttachments.OXYGEN.get());
            data.setTankOxygen(0);
            data.setTankMax(0);

            CONFIRM_COUNT.remove(uuid);
            LAST_PRESS_TIME.remove(uuid);

            player.displayClientMessage(
                    Component.translatable("message.newgc.oxygen.reset")
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            PacketDistributor.sendToPlayer(player,
                    new OxygenSyncPacket(
                            data.getOxygen(),
                            data.getTankOxygen(),
                            data.getTankMax()));
        } else {
            int remaining = REQUIRED_CONFIRMS - count;
            player.displayClientMessage(
                    Component.translatable("message.newgc.oxygen.reset_confirm", remaining, count)
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        UUID uuid = sp.getUUID();
        Long lastTime = LAST_PRESS_TIME.get(uuid);
        if (lastTime != null && System.currentTimeMillis() - lastTime > CONFIRM_TIMEOUT_MS) {
            CONFIRM_COUNT.remove(uuid);
            LAST_PRESS_TIME.remove(uuid);
        }
    }
}