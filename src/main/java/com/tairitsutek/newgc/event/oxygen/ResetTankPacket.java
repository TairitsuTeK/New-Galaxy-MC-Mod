package com.tairitsutek.newgc.event.oxygen;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ResetTankPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ResetTankPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "reset_tank"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResetTankPacket> STREAM_CODEC =
            StreamCodec.unit(new ResetTankPacket());

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ResetTankPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) {
                OxygenResetHandler.onResetKeyPressed(sp);
            }
        });
    }
}