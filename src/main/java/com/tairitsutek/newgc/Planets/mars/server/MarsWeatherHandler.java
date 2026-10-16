package com.tairitsutek.newgc.Planets.mars.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 火星没有大气，强制关闭原版天气（下雨 / 雷暴）。
 * 避免原版天气系统在火星维度生成闪电 + 雷声。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MarsWeatherHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        // 每 100 刻检查一次，避免每刻都调
        if (level.getGameTime() % 100 != 0) return;

        if (level.isRaining() || level.isThundering()) {
            level.setWeatherParameters(
                    12000,  // clearTime：保持晴天
                    0,      // rainTime
                    false,  // raining
                    false   // thundering
            );
        }
    }
}