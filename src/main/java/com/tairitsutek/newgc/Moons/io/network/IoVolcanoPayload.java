package com.tairitsutek.newgc.Moons.io.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Moons.io.client.IoVolcanoClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IoVolcanoPayload(
        boolean erupting,
        BlockPos volcanoPos,
        int eruptionTicks,
        int eruptionTotalTicks,
        int cooldownTimer
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<IoVolcanoPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io_volcano"));

    public static final StreamCodec<ByteBuf, IoVolcanoPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,     IoVolcanoPayload::erupting,
                    BlockPos.STREAM_CODEC,  IoVolcanoPayload::volcanoPos,
                    ByteBufCodecs.VAR_INT,  IoVolcanoPayload::eruptionTicks,
                    ByteBufCodecs.VAR_INT,  IoVolcanoPayload::eruptionTotalTicks,
                    ByteBufCodecs.VAR_INT,  IoVolcanoPayload::cooldownTimer,
                    IoVolcanoPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IoVolcanoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockPos pos = payload.erupting() ? payload.volcanoPos() : null;
            IoVolcanoClientCache.set(
                    pos,
                    payload.eruptionTicks(),
                    payload.eruptionTotalTicks(),
                    payload.cooldownTimer()
            );
        });
    }
}