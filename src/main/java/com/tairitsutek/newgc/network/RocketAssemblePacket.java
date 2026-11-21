package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.menu.RocketAssemblerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * C2S：玩家在火箭组装台 GUI 点击“组装”按钮。
 */
public record RocketAssemblePacket() implements CustomPacketPayload {

    public static final Type<RocketAssemblePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "rocket_assemble"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketAssemblePacket> STREAM_CODEC =
            StreamCodec.unit(new RocketAssemblePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RocketAssemblePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof RocketAssemblerMenu menu) {
                menu.tryAssemble(player);
            }
        });
    }
}
