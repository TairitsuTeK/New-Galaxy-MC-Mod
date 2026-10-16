package com.tairitsutek.newgc.Planets.mars.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.mars.server.MarsVolcanoManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S 调试包：客户端请求服务端切换火山状态。
 * mode: 0 = 强制喷发，1 = 强制冷却
 */
public record MarsVolcanoDebugPayload(int mode) implements CustomPacketPayload {

    public static final int MODE_ERUPT = 0;
    public static final int MODE_COOLDOWN = 1;

    public static final CustomPacketPayload.Type<MarsVolcanoDebugPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars_volcano_debug"));

    public static final StreamCodec<ByteBuf, MarsVolcanoDebugPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, MarsVolcanoDebugPayload::mode,
                    MarsVolcanoDebugPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MarsVolcanoDebugPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.mode() == MODE_ERUPT) {
                MarsVolcanoManager.debugForceErupt();
            } else {
                MarsVolcanoManager.debugForceCooldown();
            }
        });
    }
}