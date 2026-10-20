package com.tairitsutek.newgc.Moons.io.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Moons.io.server.IoVolcanoManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IoVolcanoDebugPayload(int mode) implements CustomPacketPayload {

    public static final int MODE_ERUPT = 0;
    public static final int MODE_COOLDOWN = 1;

    public static final CustomPacketPayload.Type<IoVolcanoDebugPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io_volcano_debug"));

    public static final StreamCodec<ByteBuf, IoVolcanoDebugPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, IoVolcanoDebugPayload::mode,
                    IoVolcanoDebugPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IoVolcanoDebugPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.mode() == MODE_ERUPT) {
                IoVolcanoManager.debugForceErupt();
            } else {
                IoVolcanoManager.debugForceCooldown();
            }
        });
    }
}