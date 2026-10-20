package com.tairitsutek.newgc.Moons.io.server;

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
public class IoVolcanoDamageHandler {

    private static final double MAX_DAMAGE_RADIUS = 48.0;
    private static final float MAX_DPS = 12.0f; // 木卫一火山更致命，最高 12 点伤害/秒
    private static final int MIN_INTERVAL = 5;
    private static final int MAX_INTERVAL = 40;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (!IoVolcanoManager.isErupting()) return;

        BlockPos vp = IoVolcanoManager.getVolcanoPos();
        if (vp == null) return;

        Vec3 p = player.position();
        double dx = p.x - (vp.getX() + 0.5);
        double dy = p.y - (vp.getY() + 0.5);
        double dz = p.z - (vp.getZ() + 0.5);
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > MAX_DAMAGE_RADIUS) return;

        double t = dist / MAX_DAMAGE_RADIUS;
        double distanceFactor = 1.0 - t * t;
        float intensity = IoVolcanoManager.getEruptionIntensity();
        float dps = MAX_DPS * intensity * (float) distanceFactor;

        int interval = (int) (MIN_INTERVAL + (MAX_INTERVAL - MIN_INTERVAL) * t);
        if (player.tickCount % interval != 0) return;

        float damageAmount = dps * interval / 20f;
        if (damageAmount <= 0.01f) return;

        DamageSource src = player.damageSources().lava(); // 岩浆伤害
        player.hurt(src, damageAmount);
    }
}