package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record TeleportRequestPacket(ResourceKey<Level> dimension, BlockPos pos)
        implements CustomPacketPayload {

    public static final Type<TeleportRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "teleport_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportRequestPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), TeleportRequestPacket::dimension,
                    BlockPos.STREAM_CODEC, TeleportRequestPacket::pos,
                    TeleportRequestPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}