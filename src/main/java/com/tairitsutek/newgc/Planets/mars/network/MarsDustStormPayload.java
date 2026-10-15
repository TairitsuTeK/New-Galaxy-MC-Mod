package com.tairitsutek.newgc.Planets.mars.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.mars.client.MarsDustStormClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarsDustStormPayload(
        int phaseOrdinal,
        int phaseTimer,
        float stormStrength
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MarsDustStormPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars_dust_storm"));

    public static final StreamCodec<ByteBuf, MarsDustStormPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, MarsDustStormPayload::phaseOrdinal,
                    ByteBufCodecs.VAR_INT, MarsDustStormPayload::phaseTimer,
                    ByteBufCodecs.FLOAT,   MarsDustStormPayload::stormStrength,
                    MarsDustStormPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarsDustStormPayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                MarsDustStormClientCache.set(
                        payload.phaseOrdinal(),
                        payload.phaseTimer(),
                        payload.stormStrength()
                )
        );
    }
}