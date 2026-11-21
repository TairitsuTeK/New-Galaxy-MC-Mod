package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * C2S：玩家在没有可用目的地时关闭星图，请求立即进入漂流。
 */
public record RocketDriftRequestPacket() implements CustomPacketPayload {

    public static final Type<RocketDriftRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "rocket_drift_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketDriftRequestPacket> STREAM_CODEC =
            StreamCodec.unit(new RocketDriftRequestPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
