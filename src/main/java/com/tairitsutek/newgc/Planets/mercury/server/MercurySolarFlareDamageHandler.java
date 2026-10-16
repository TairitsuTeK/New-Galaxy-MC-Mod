package com.tairitsutek.newgc.Planets.mercury.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MercurySolarFlareDamageHandler {

    // 最大每秒伤害（FLARE 峰值时）
    private static final float MAX_DPS = 10.0f;

    // 伤害间隔（刻）
    private static final int DAMAGE_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        float intensity = MercurySolarFlareManager.getFlareIntensity();
        if (intensity <= 0.2f) return;

        if (player.tickCount % DAMAGE_INTERVAL != 0) return;

        // 强度 0.2~1.5 映射到 0~1
        float t = (intensity - 0.2f) / 1.3f;
        t = Math.max(0f, Math.min(1f, t));

        float damage = MAX_DPS * t;
        if (damage <= 0.05f) return;

        player.hurt(player.damageSources().generic(), damage);
    }
}