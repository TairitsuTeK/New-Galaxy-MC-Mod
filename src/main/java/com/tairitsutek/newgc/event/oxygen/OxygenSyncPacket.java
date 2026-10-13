package com.tairitsutek.newgc.event.oxygen;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OxygenSyncPacket(int oxygen, int tankOxygen, int tankMax) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OxygenSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "oxygen_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OxygenSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, OxygenSyncPacket::oxygen,
                    ByteBufCodecs.INT, OxygenSyncPacket::tankOxygen,
                    ByteBufCodecs.INT, OxygenSyncPacket::tankMax,
                    OxygenSyncPacket::new
            );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OxygenSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                OxygenData data = Minecraft.getInstance().player.getData(ModAttachments.OXYGEN.get());
                data.setTankMax(packet.tankMax());
                data.setOxygen(packet.oxygen());
                data.setTankOxygen(packet.tankOxygen());
            }
        });
    }
}