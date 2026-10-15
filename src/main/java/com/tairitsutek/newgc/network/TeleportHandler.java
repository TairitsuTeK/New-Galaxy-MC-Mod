package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class TeleportHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                TeleportRequestPacket.TYPE,
                TeleportRequestPacket.STREAM_CODEC,
                TeleportHandler::handle
        );
    }

    private static void handle(TeleportRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ServerLevel targetLevel = player.server.getLevel(packet.dimension());
            if (targetLevel == null) return;

            if (player.level().dimension().equals(packet.dimension())) return;

            player.changeDimension(new DimensionTransition(
                    targetLevel,
                    net.minecraft.world.phys.Vec3.atBottomCenterOf(packet.pos()),
                    net.minecraft.world.phys.Vec3.ZERO,
                    player.getYRot(),
                    player.getXRot(),
                    DimensionTransition.DO_NOTHING
            ));
        });
    }
}