package com.tairitsutek.newgc.Planets.jupiter;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
// 这里是关键修正：从 ...event.TickEvent 改为 ...event.tick.ServerTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterWindHandler {

    // 风力强度（先设大一点测试，确认能推动后再调小）
    private static final double WIND_STRENGTH = 0.05;

    // 风向（弧度），和粒子保持一致
    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);

    // 上下波动幅度
    private static final double VERTICAL_JITTER = 0.003;

    // 随机抖动幅度
    private static final double RANDOM_JITTER = 0.01;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 注意：这里获取 server 的方式也变了
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) continue;

            for (ServerPlayer player : level.players()) {
                // 创造/旁观模式不推
                if (player.isCreative() || player.isSpectator()) continue;

                // 风向单位向量
                double dirX = Math.cos(WIND_ANGLE);
                double dirZ = Math.sin(WIND_ANGLE);

                // 随机抖动
                double jitterX = (player.getRandom().nextDouble() - 0.5) * RANDOM_JITTER;
                double jitterZ = (player.getRandom().nextDouble() - 0.5) * RANDOM_JITTER;
                double jitterY = (player.getRandom().nextDouble() - 0.5) * VERTICAL_JITTER;

                // 最终推力
                double pushX = dirX * WIND_STRENGTH + jitterX;
                double pushZ = dirZ * WIND_STRENGTH + jitterZ;
                double pushY = jitterY;

                // 施加推力
                player.push(pushX, pushY, pushZ);

                // 关键：标记速度变化，让客户端同步
                player.hurtMarked = true;
            }
        }
    }
}