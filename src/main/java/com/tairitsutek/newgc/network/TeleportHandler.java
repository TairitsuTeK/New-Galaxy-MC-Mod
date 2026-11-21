package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.item.ToolsItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class TeleportHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                TeleportRequestPacket.TYPE,
                TeleportRequestPacket.STREAM_CODEC,
                TeleportHandler::handle
        );
    }

    private static void handle(TeleportRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 服务端复核：防止改包客户端在没有传送器时任意传送。
            if (!player.getMainHandItem().is(ToolsItem.TELEPORTER.get())
                    && !player.getOffhandItem().is(ToolsItem.TELEPORTER.get())) {
                return;
            }

            ResourceKey<Level> targetKey = packet.dimension();
            if (!isAllowedDimension(targetKey)) return;

            ServerLevel targetLevel = player.server.getLevel(targetKey);
            if (targetLevel == null) return;
            if (player.level().dimension().equals(targetKey)) return;

            // 忽略客户端传来的 Y/坐标，使用当前 X/Z + 目标维度地表重新计算安全落点。
            int x = Mth.floor(player.getX());
            int z = Mth.floor(player.getZ());

            // getHeight() returns the minimum build height for an unloaded
            // chunk, which would otherwise put the player underground.
            targetLevel.getChunk(x >> 4, z >> 4);
            int surface = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos safePos = findSafeSpot(targetLevel, x, surface, z);

            player.changeDimension(new DimensionTransition(
                    targetLevel,
                    Vec3.atBottomCenterOf(safePos),
                    Vec3.ZERO,
                    player.getYRot(),
                    player.getXRot(),
                    DimensionTransition.DO_NOTHING
            ));
        });
    }

    private static boolean isAllowedDimension(ResourceKey<Level> key) {
        return key.equals(Level.OVERWORLD) || ModDimensions.ALL_LEVEL_KEYS.contains(key);
    }

    private static BlockPos findSafeSpot(ServerLevel level, int x, int surface, int z) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 3;
        int start = Mth.clamp(surface, minY, maxY);

        for (int y = start; y < start + 32 && y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return new BlockPos(x, start, z);
    }
}