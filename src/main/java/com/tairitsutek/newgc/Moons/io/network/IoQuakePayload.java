package com.tairitsutek.newgc.Moons.io.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Moons.io.client.IoQuakeClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IoQuakePayload(
        boolean isQuaking,
        int quakeTicks,
        int quakeTotalTicks,
        int cooldownTimer
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<IoQuakePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io_quake"));

    public static final StreamCodec<ByteBuf, IoQuakePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,     IoQuakePayload::isQuaking,
                    ByteBufCodecs.VAR_INT,  IoQuakePayload::quakeTicks,
                    ByteBufCodecs.VAR_INT,  IoQuakePayload::quakeTotalTicks,
                    ByteBufCodecs.VAR_INT,  IoQuakePayload::cooldownTimer,
                    IoQuakePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IoQuakePayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                IoQuakeClientCache.set(
                        payload.isQuaking(),
                        payload.quakeTicks(),
                        payload.quakeTotalTicks(),
                        payload.cooldownTimer()
                )
        );
    }
}