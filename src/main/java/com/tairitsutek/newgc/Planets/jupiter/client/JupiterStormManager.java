package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterStormManager {

    // ===== 风暴周期（刻）=====
    // 一个完整周期：平静 → 增强 → 顶峰 → 减弱 → 平静
    private static final int STORM_CYCLE = 2400;   // 120 秒

    // ===== 风暴强度范围 =====
    private static final float MIN_STRENGTH = 0.2f;   // 平静时
    private static final float MAX_STRENGTH = 1.0f;   // 顶峰时

    // ===== 当前风暴强度（0.2 ~ 1.0）=====
    private static float stormStrength = MIN_STRENGTH;

    // ===== 当前周期内的 tick =====
    private static int cycleTick = 0;

    public static float getStormStrength() {
        return stormStrength;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 检查是否有木星维度加载
        boolean jupiterLoaded = false;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) {
                jupiterLoaded = true;
                break;
            }
        }
        if (!jupiterLoaded) return;

        cycleTick++;
        if (cycleTick >= STORM_CYCLE) {
            cycleTick = 0;
        }

        // 计算当前强度：用正弦波，平滑过渡
        // 0 到 PI 是上升，PI 到 2PI 是下降
        float phase = (float) cycleTick / STORM_CYCLE;
        float wave = (float) Math.sin(phase * Math.PI * 2.0);  // -1 ~ 1
        // 映射到 MIN_STRENGTH ~ MAX_STRENGTH
        float normalized = (wave + 1.0f) * 0.5f;  // 0 ~ 1
        stormStrength = MIN_STRENGTH + (MAX_STRENGTH - MIN_STRENGTH) * normalized;
    }
}