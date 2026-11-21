package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.menu.RocketStorageMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S：玩家在火箭内按下储物键（F），请求打开火箭储物舱。
 */
public record OpenRocketStoragePacket() implements CustomPacketPayload {

    public static final Type<OpenRocketStoragePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "open_rocket_storage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRocketStoragePacket> STREAM_CODEC =
            StreamCodec.unit(new OpenRocketStoragePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenRocketStoragePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.getVehicle() instanceof RocketEntity rocket)) {
                return;
            }
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new RocketStorageMenu(id, inv, rocket.getCargo()),
                    Component.translatable("menu.newgc.rocket_storage")));
        });
    }
}
