package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.client.RocketClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * S2C：火箭进入轨道，客户端应立即切换到星图界面选择目的地。
 */
public record RocketOrbitPacket() implements CustomPacketPayload {

    public static final Type<RocketOrbitPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "rocket_orbit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketOrbitPacket> STREAM_CODEC =
            StreamCodec.unit(new RocketOrbitPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RocketOrbitPacket packet, IPayloadContext context) {
        context.enqueueWork(RocketClientHandler::openStarMapInOrbit);
    }
}
