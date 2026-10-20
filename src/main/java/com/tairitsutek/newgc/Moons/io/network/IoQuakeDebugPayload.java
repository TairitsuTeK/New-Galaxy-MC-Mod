package com.tairitsutek.newgc.Moons.io.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Moons.io.server.IoQuakeManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S 调试包：客户端请求服务端切换地震状态。
 * mode: 0 = 强制地震，1 = 强制平静
 */
public record IoQuakeDebugPayload(int mode) implements CustomPacketPayload {

    public static final int MODE_QUAKE = 0;
    public static final int MODE_CALM = 1;

    public static final CustomPacketPayload.Type<IoQuakeDebugPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io_quake_debug"));

    public static final StreamCodec<ByteBuf, IoQuakeDebugPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, IoQuakeDebugPayload::mode,
                    IoQuakeDebugPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IoQuakeDebugPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.mode() == MODE_QUAKE) {
                IoQuakeManager.debugForceQuake();
            } else if (payload.mode() == MODE_CALM) {
                IoQuakeManager.debugForceCalm();
            }
        });
    }
}