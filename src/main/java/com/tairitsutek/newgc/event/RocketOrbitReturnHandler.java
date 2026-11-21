package com.tairitsutek.newgc.event;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 轨道界面恢复。
 * <p>
 * 玩家在轨道上打开星图时强制退出游戏，重新登录后应该仍然坐在同一枚火箭里，
 * 并且星图会重新弹出（星图在轨道模式下不允许用 ESC 关闭，只有选择目的地才能离开）。
 * <p>
 * 做法：退出时把火箭 UUID / 维度 / 位置写进玩家持久数据，登录时找回火箭并重新骑乘，
 * 然后由火箭的 {@code announceOrbit()} 再次通知客户端打开星图。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public final class RocketOrbitReturnHandler {

    private static final String KEY_ROCKET = "newgc_orbit_rocket";
    private static final String KEY_DIMENSION = "newgc_orbit_dimension";
    private static final String KEY_POS = "newgc_orbit_pos";

    private RocketOrbitReturnHandler() {
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.getVehicle() instanceof RocketEntity rocket)) {
            return;
        }
        if (rocket.getState() != RocketEntity.STATE_ORBIT) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        data.putUUID(KEY_ROCKET, rocket.getUUID());
        data.putString(KEY_DIMENSION,
                player.level().dimension().location().toString());
        data.putLong(KEY_POS, rocket.blockPosition().asLong());
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(KEY_ROCKET)) {
            return;
        }

        UUID rocketId = data.getUUID(KEY_ROCKET);
        String dimensionId = data.getString(KEY_DIMENSION);
        BlockPos rocketPos = BlockPos.of(data.getLong(KEY_POS));
        data.remove(KEY_ROCKET);
        data.remove(KEY_DIMENSION);
        data.remove(KEY_POS);

        ResourceLocation location = ResourceLocation.tryParse(dimensionId);
        if (location == null) {
            return;
        }
        ServerLevel targetLevel = player.server.getLevel(
                ResourceKey.create(Registries.DIMENSION, location));
        if (targetLevel == null) {
            return;
        }

        // 区块未加载时 getEntity 找不到实体，先强制加载火箭所在区块
        targetLevel.getChunkAt(rocketPos);
        Entity entity = targetLevel.getEntity(rocketId);
        if (!(entity instanceof RocketEntity rocket)
                || rocket.getState() != RocketEntity.STATE_ORBIT) {
            return;
        }

        player.teleportTo(targetLevel,
                rocket.getX(), rocket.getY() + 2.3D, rocket.getZ(),
                player.getYRot(), player.getXRot());
        player.startRiding(rocket, true);
        rocket.announceOrbit();
    }
}
