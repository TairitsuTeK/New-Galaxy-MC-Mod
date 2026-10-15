package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterWindHandler {

    // 风力强度，越大吹得越猛
    private static final double WIND_STRENGTH = 0.015;

    // 风向（弧度），和粒子保持一致
    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);

    // 上下波动幅度
    private static final double VERTICAL_JITTER = 0.003;

    // 随机抖动幅度
    private static final double RANDOM_JITTER = 0.01;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!serverPlayer.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (serverPlayer.isCreative() || serverPlayer.isSpectator()) return;

        // 风向单位向量
        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        // 随机抖动，让风力不完美一致
        double jitterX = (serverPlayer.getRandom().nextDouble() - 0.5) * RANDOM_JITTER;
        double jitterZ = (serverPlayer.getRandom().nextDouble() - 0.5) * RANDOM_JITTER;
        double jitterY = (serverPlayer.getRandom().nextDouble() - 0.5) * VERTICAL_JITTER;

        // 施加推力
        double pushX = dirX * WIND_STRENGTH + jitterX;
        double pushZ = dirZ * WIND_STRENGTH + jitterZ;
        double pushY = jitterY;

        serverPlayer.push(pushX, pushY, pushZ);
    }
}