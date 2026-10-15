package com.tairitsutek.newgc.Planets.mars.server;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class MarsVolcanoDamageHandler {

    // ===== 伤害半径 & 参数 =====
    // 超过这个距离完全无伤
    private static final double MAX_DAMAGE_RADIUS = 48.0;

    // 在喷发点正上方时的最大每秒伤害（以 1 秒 = 20 刻为基准）
    private static final float MAX_DPS = 8.0f;

    // 伤害间隔（刻）：近距离更频繁
    private static final int MIN_INTERVAL = 5;    // 0.25 秒
    private static final int MAX_INTERVAL = 40;   // 2 秒

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (!MarsVolcanoManager.isErupting()) return;

        BlockPos vp = MarsVolcanoManager.getVolcanoPos();
        if (vp == null) return;

        Vec3 p = player.position();
        double dx = p.x - (vp.getX() + 0.5);
        double dy = p.y - (vp.getY() + 0.5);
        double dz = p.z - (vp.getZ() + 0.5);
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > MAX_DAMAGE_RADIUS) return;

        // ---- 归一化距离：0 = 正上方，1 = 边缘 ----
        double t = dist / MAX_DAMAGE_RADIUS;
        // 距离衰减：平方，近处急剧增大
        double distanceFactor = 1.0 - t * t;

        float intensity = MarsVolcanoManager.getEruptionIntensity();

        // ---- 每秒伤害 = 最大DPS × 强度 × 距离衰减 ----
        float dps = MAX_DPS * intensity * (float) distanceFactor;

        // ---- 伤害间隔：近处更频繁 ----
        // interval 从 MIN_INTERVAL（近）到 MAX_INTERVAL（远）
        int interval = (int) (MIN_INTERVAL + (MAX_INTERVAL - MIN_INTERVAL) * t);

        // ---- 每 interval 刻造成 dps * interval/20 的伤害 ----
        if (player.tickCount % interval != 0) return;

        float damageAmount = dps * interval / 20f;
        if (damageAmount <= 0.01f) return;

        DamageSource src = player.damageSources().lava();
        player.hurt(src, damageAmount);
    }
}