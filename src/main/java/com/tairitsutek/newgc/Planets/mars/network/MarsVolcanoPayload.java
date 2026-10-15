package com.tairitsutek.newgc.Planets.mars.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.mars.client.MarsVolcanoClientCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MarsVolcanoPayload(
        boolean erupting,
        BlockPos volcanoPos,
        int eruptionTicks,
        int eruptionTotalTicks,
        int cooldownTimer
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MarsVolcanoPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars_volcano"));

    public static final StreamCodec<ByteBuf, MarsVolcanoPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,     MarsVolcanoPayload::erupting,
                    BlockPos.STREAM_CODEC,  MarsVolcanoPayload::volcanoPos,
                    ByteBufCodecs.VAR_INT,  MarsVolcanoPayload::eruptionTicks,
                    ByteBufCodecs.VAR_INT,  MarsVolcanoPayload::eruptionTotalTicks,
                    ByteBufCodecs.VAR_INT,  MarsVolcanoPayload::cooldownTimer,
                    MarsVolcanoPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarsVolcanoPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockPos pos = payload.erupting() ? payload.volcanoPos() : null;
            MarsVolcanoClientCache.set(
                    pos,
                    payload.eruptionTicks(),
                    payload.eruptionTotalTicks(),
                    payload.cooldownTimer()
            );
        });
    }
}