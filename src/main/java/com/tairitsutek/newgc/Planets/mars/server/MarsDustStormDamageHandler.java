package com.tairitsutek.newgc.Planets.mars.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MarsDustStormDamageHandler {

    // 沙尘暴强度阈值：低于此不伤害
    private static final float DAMAGE_THRESHOLD = 0.6f;

    // 最大每秒伤害（在强度 1.5 时）
    private static final float MAX_DPS = 2.0f;

    // 伤害间隔（刻）
    private static final int DAMAGE_INTERVAL = 40;   // 2 秒

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        float strength = MarsDustStormManager.getStormStrength();
        if (strength <= DAMAGE_THRESHOLD) return;

        if (player.tickCount % DAMAGE_INTERVAL != 0) return;

        // 归一化：0 = 阈值，1 = 最大强度
        float t = (strength - DAMAGE_THRESHOLD) / (1.5f - DAMAGE_THRESHOLD);
        t = Math.max(0f, Math.min(1f, t));

        float damage = MAX_DPS * t * DAMAGE_INTERVAL / 20f;
        if (damage <= 0.01f) return;

        // 用 generic 伤害，避免火焰免疫
        player.hurt(player.damageSources().generic(), damage);
    }
}