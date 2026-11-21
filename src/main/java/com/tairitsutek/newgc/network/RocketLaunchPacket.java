package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.entity.RocketLaunchService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S：玩家在火箭内按下空格，请求点火升空。
 * <p>
 * 服务端会重新校验玩家是否真的坐在火箭里、火箭是否停在完整的 2x2 发射平台上。
 */
public record RocketLaunchPacket() implements CustomPacketPayload {

    public static final Type<RocketLaunchPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "rocket_launch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketLaunchPacket> STREAM_CODEC =
            StreamCodec.unit(new RocketLaunchPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RocketLaunchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.getVehicle() instanceof RocketEntity rocket)) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.rocket.need_board"), true);
                return;
            }
            RocketLaunchService.LaunchResult result =
                    RocketLaunchService.request(player, rocket);
            if (result == RocketLaunchService.LaunchResult.LAUNCHED) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.rocket.countdown",
                                RocketEntity.COUNTDOWN_TICKS / 20), true);
            } else if (result == RocketLaunchService.LaunchResult.FAILED) {
                if (rocket.getState() != RocketEntity.STATE_IDLE) {
                    player.displayClientMessage(
                            Component.translatable("message.newgc.rocket.busy"), true);
                } else {
                    player.displayClientMessage(
                            Component.translatable("message.newgc.rocket.need_pad"), true);
                }
            }
        });
    }
}
