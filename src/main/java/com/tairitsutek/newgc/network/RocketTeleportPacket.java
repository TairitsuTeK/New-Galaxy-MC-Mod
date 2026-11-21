package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * C2S：玩家在轨道星图中选择目标星球，请求火箭连同自身一起跃迁。
 */
public record RocketTeleportPacket(ResourceKey<Level> dimension) implements CustomPacketPayload {

    public static final Type<RocketTeleportPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    NewGalaxyMain.MODID, "rocket_teleport"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketTeleportPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), RocketTeleportPacket::dimension,
                    RocketTeleportPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
