package com.tairitsutek.newgc.event.oxygen;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class OxygenHandler {

    private static final int CONSUME_INTERVAL = 20;   // 1 秒
    private static final int CONSUME_AMOUNT = 3;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        OxygenData data = player.getData(ModAttachments.OXYGEN.get());
        ResourceKey<Level> dim = player.level().dimension();

        // ★ 创造/旁观模式：不消耗，且自动回满
        if (player.isCreative() || player.isSpectator()) {
            if (!data.isFull()) {
                data.setOxygen(data.getBaseMax());
                data.setTankOxygen(data.getTankMax());
            }
            data.setDamageCooldown(0);
            syncIfNeeded(player, data);
            return;
        }

        // 主世界：不消耗，恢复
        if (dim.equals(Level.OVERWORLD)) {
            if (!data.isFull()) {
                data.addOxygen(10);
            }
            data.setDamageCooldown(0);
            syncIfNeeded(player, data);
            return;
        }

        // 其它维度：消耗
        if (player.tickCount % CONSUME_INTERVAL == 0) {
            data.consumeOxygen(CONSUME_AMOUNT);
        }

        // 没氧气 → 掉血
        if (data.isEmpty()) {
            data.tickDamageCooldown();
            if (data.getDamageCooldown() <= 0) {
                data.setDamageCooldown(OxygenConfig.DAMAGE_INTERVAL);

                float maxHealth = player.getMaxHealth();
                float damage = maxHealth * OxygenConfig.DAMAGE_FRACTION;

                player.hurt(player.damageSources().generic(), damage);

                if (player.tickCount % 60 == 0) {
                    player.displayClientMessage(
                            Component.translatable("message.newgc.oxygen.low")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                }
            }
        } else {
            data.setDamageCooldown(0);
        }

        syncIfNeeded(player, data);
    }

    private static void syncIfNeeded(Player player, OxygenData data) {
        if (player instanceof ServerPlayer sp && player.tickCount % 5 == 0) {
            PacketDistributor.sendToPlayer(sp,
                    new OxygenSyncPacket(
                            data.getOxygen(),
                            data.getTankOxygen(),
                            data.getTankMax()));
        }
    }
}