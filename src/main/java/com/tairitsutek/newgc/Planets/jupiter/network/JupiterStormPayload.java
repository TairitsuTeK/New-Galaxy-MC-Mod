package com.tairitsutek.newgc.Planets.jupiter.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.jupiter.client.JupiterStormClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record JupiterStormPayload(
        int phaseOrdinal,
        int phaseTimer,
        float stormStrength
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JupiterStormPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "jupiter_storm"));

    public static final StreamCodec<ByteBuf, JupiterStormPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, JupiterStormPayload::phaseOrdinal,
                    ByteBufCodecs.VAR_INT, JupiterStormPayload::phaseTimer,
                    ByteBufCodecs.FLOAT,   JupiterStormPayload::stormStrength,
                    JupiterStormPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(JupiterStormPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                JupiterStormClientCache.set(
                        payload.phaseOrdinal(),
                        payload.phaseTimer(),
                        payload.stormStrength()
                )
        );
    }
}