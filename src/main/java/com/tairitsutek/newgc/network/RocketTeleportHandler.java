package com.tairitsutek.newgc.network;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.entity.ModEntities;
import com.tairitsutek.newgc.entity.RocketDestinations;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 火箭轨道跃迁：玩家在星图中选择星球后，火箭连同玩家一起抵达目标维度地表。
 * <p>
 * 服务端会重新校验玩家是否真的坐在轨道状态的火箭里，避免改包客户端任意传送。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class RocketTeleportHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                RocketTeleportPacket.TYPE,
                RocketTeleportPacket.STREAM_CODEC,
                RocketTeleportHandler::handle
        );
        registrar.playToServer(
                OpenRocketStoragePacket.TYPE,
                OpenRocketStoragePacket.STREAM_CODEC,
                OpenRocketStoragePacket::handle
        );
        registrar.playToServer(
                RocketAssemblePacket.TYPE,
                RocketAssemblePacket.STREAM_CODEC,
                RocketAssemblePacket::handle
        );
        registrar.playToServer(
                RocketDriftRequestPacket.TYPE,
                RocketDriftRequestPacket.STREAM_CODEC,
                RocketTeleportHandler::handleDrift
        );
    }

    private static void handle(RocketTeleportPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.getVehicle() instanceof RocketEntity rocket)) {
                return;
            }
            if (rocket.getState() != RocketEntity.STATE_ORBIT) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.rocket.no_orbit"), true);
                return;
            }

            ResourceKey<Level> targetKey = packet.dimension();
            if (!isAllowedDimension(targetKey)) {
                return;
            }
            // 按当前等级和所在天体强制校验，客户端星图只是同一规则的展示。
            int tier = rocket.getTier();
            if (!RocketDestinations.canReach(targetKey, player.level().dimension(), tier)) {
                boolean hasTarget = RocketDestinations.hasAnyTarget(
                        player.level().dimension(), tier);
                if (!hasTarget) {
                    player.displayClientMessage(
                            Component.translatable("message.newgc.rocket.no_targets",
                                    RocketEntity.NO_TARGET_GRACE_TICKS / 20), true);
                } else {
                    player.displayClientMessage(Component.translatable(
                            "message.newgc.rocket.out_of_range",
                            RocketDestinations.nameComponent(targetKey),
                            Component.translatable(RocketDestinations.tierNameKey(tier)),
                            RocketDestinations.targetSummaryComponent(
                                    player.level().dimension(), tier)), true);
                }
                return;
            }
            ServerLevel targetLevel = player.server.getLevel(targetKey);
            if (targetLevel == null) {
                return;
            }

            int x = Mth.floor(player.getX());
            int z = Mth.floor(player.getZ());
            // 未加载区块的 getHeight 会返回最低高度，先强制生成区块
            targetLevel.getChunk(x >> 4, z >> 4);
            int surface = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos landing = findLandingSpot(targetLevel, x, surface, z);

            // 先备份储物舱，再销毁旧火箭
            NonNullList<ItemStack> contents = NonNullList.withSize(
                    RocketEntity.CARGO_SIZE, ItemStack.EMPTY);
            for (int i = 0; i < contents.size(); i++) {
                contents.set(i, rocket.getCargo().getItem(i).copy());
            }

            player.stopRiding();
            rocket.discard();

            double landingY = landing.getY();
            player.teleportTo(targetLevel,
                    landing.getX() + 0.5D, landingY, landing.getZ() + 0.5D,
                    player.getYRot(), player.getXRot());

            RocketEntity arrival = ModEntities.ROCKET.get().create(targetLevel);
            if (arrival == null) {
                return;
            }
            arrival.moveTo(landing.getX() + 0.5D, landingY, landing.getZ() + 0.5D,
                    player.getYRot(), 0.0F);
            arrival.setTier(rocket.getTier());
            for (int i = 0; i < contents.size(); i++) {
                arrival.getCargo().setItem(i, contents.get(i));
            }
            targetLevel.addFreshEntity(arrival);
            player.startRiding(arrival, true);

            player.displayClientMessage(Component.translatable(
                    "message.newgc.rocket.arrived",
                    targetKey.location().getPath()), false);
        });
    }

    /** 玩家在没有可用目的地时主动关闭星图：立即请求漂流。 */
    private static void handleDrift(RocketDriftRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.getVehicle() instanceof RocketEntity rocket)
                    || rocket.getState() != RocketEntity.STATE_ORBIT) {
                return;
            }
            if (!rocket.beginDrift()) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.rocket.drift_blocked"), true);
            }
        });
    }

    private static boolean isAllowedDimension(ResourceKey<Level> key) {
        return key.equals(Level.OVERWORLD) || ModDimensions.ALL_LEVEL_KEYS.contains(key);
    }

    /**
     * 在目标维度寻找一块能容纳火箭（6 格高）的落脚点。
     */
    private static BlockPos findLandingSpot(ServerLevel level, int x, int surface, int z) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 8;
        int start = Mth.clamp(surface, minY, maxY);

        for (int y = start; y < start + 40 && y <= maxY; y++) {
            boolean clear = true;
            for (int dy = 0; dy < RocketEntity.LANDING_CLEARANCE && clear; dy++) {
                clear = level.isEmptyBlock(new BlockPos(x, y + dy, z));
            }
            if (clear) {
                return new BlockPos(x, y, z);
            }
        }
        return new BlockPos(x, start, z);
    }
}
