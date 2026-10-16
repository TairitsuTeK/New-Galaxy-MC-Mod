package com.tairitsutek.newgc.Planets.mercury.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.mercury.client.MercurySolarFlareClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MercurySolarFlarePayload(
        int phaseOrdinal,
        int phaseTimer,
        float intensity
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MercurySolarFlarePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mercury_solar_flare"));

    public static final StreamCodec<ByteBuf, MercurySolarFlarePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, MercurySolarFlarePayload::phaseOrdinal,
                    ByteBufCodecs.VAR_INT, MercurySolarFlarePayload::phaseTimer,
                    ByteBufCodecs.FLOAT,   MercurySolarFlarePayload::intensity,
                    MercurySolarFlarePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MercurySolarFlarePayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                MercurySolarFlareClientCache.set(
                        payload.phaseOrdinal(),
                        payload.phaseTimer(),
                        payload.intensity()
                )
        );
    }
}