package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.server.JupiterStormManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterThunderHandler {

    private static final int LIGHTNING_RADIUS = 16;

    // ===== 冷却范围（tick）=====
    // 平静期：6~12 秒劈一次
    private static final int COOLDOWN_MIN_CALM = 120;
    private static final int COOLDOWN_MAX_CALM = 240;
    // 风暴期：0.1~0.4 秒劈一次
    private static final int COOLDOWN_MIN_STORM = 2;
    private static final int COOLDOWN_MAX_STORM = 8;

    // ===== 归一化参考值 =====
    private static final float NORM_PEAK = JupiterStormManager.PEAK_REFERENCE;
    private static final float NORM_SUDDEN = JupiterStormManager.SUDDEN_REFERENCE;

    private static final int DEBUG_LOG_INTERVAL = 20;

    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Float> LAST_STRENGTH = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        ServerLevel jupiter = (ServerLevel) event.getLevel();
        ResourceKey<Level> key = jupiter.dimension();

        // ===== 强制劈雷调试 =====
        if (JupiterStormManager.isForceThunder()) {
            if (jupiter.players().isEmpty()) return;
            ServerPlayer player = jupiter.players().get(jupiter.random.nextInt(jupiter.players().size()));
            double x = player.getX() + (jupiter.random.nextDouble() * 2 - 1) * LIGHTNING_RADIUS;
            double z = player.getZ() + (jupiter.random.nextDouble() * 2 - 1) * LIGHTNING_RADIUS;
            int y = jupiter.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, jupiter);
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(true);
            jupiter.addFreshEntity(bolt);
            System.out.println("[Thunder] FORCED bolt at " + (int) x + "," + y + "," + (int) z);
            return;
        }

        // ===== 读取并平滑强度 =====
        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(NORM_SUDDEN, strength));

        float last = LAST_STRENGTH.getOrDefault(key, strength);
        float smoothed = last + (strength - last) * 0.2f;
        LAST_STRENGTH.put(key, smoothed);

        // 归一化到 0~1
        float t = Math.min(1f, smoothed / NORM_PEAK);

        // ===== 冷却倒计时 =====
        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            COOLDOWNS.put(key, cooldown - 1);
            return; // 冷却没结束，本 tick 不劈雷
        }

        // ===== 冷却归零，准备劈雷 =====
        // 根据当前强度计算下一次的冷却时间
        int cooldownMin = Math.round(COOLDOWN_MIN_STORM + (COOLDOWN_MIN_CALM - COOLDOWN_MIN_STORM) * (1 - t));
        int cooldownMax = Math.round(COOLDOWN_MAX_STORM + (COOLDOWN_MAX_CALM - COOLDOWN_MAX_STORM) * (1 - t));

        if (cooldownMax <= cooldownMin) {
            cooldownMax = cooldownMin + 1;
        }

        int newCooldown = cooldownMin + jupiter.random.nextInt(cooldownMax - cooldownMin + 1);
        COOLDOWNS.put(key, newCooldown);

        // ===== 调试日志 =====
        if (jupiter.getGameTime() % DEBUG_LOG_INTERVAL == 0) {
            System.out.println("[Thunder] strength=" + String.format("%.2f", strength)
                    + " smoothed=" + String.format("%.2f", smoothed)
                    + " t=" + String.format("%.2f", t)
                    + " cooldown=" + newCooldown
                    + " players=" + jupiter.players().size());
        }

        if (jupiter.players().isEmpty()) return;

        // ===== 决定劈雷数量 =====
        int extra = 0;
        if (t > 0.5f) {
            // t 0.5 -> 最多 2 道; t 1.0 -> 最多 6 道
            int maxExtra = 1 + (int) ((t - 0.5f) / 0.5f * 5);
            extra = jupiter.random.nextInt(maxExtra + 1);
        }
        int boltCount = 1 + extra;

        // ===== 生成闪电 =====
        for (int i = 0; i < boltCount; i++) {
            ServerPlayer player = jupiter.players().get(jupiter.random.nextInt(jupiter.players().size()));
            double x = player.getX() + (jupiter.random.nextDouble() * 2 - 1) * LIGHTNING_RADIUS;
            double z = player.getZ() + (jupiter.random.nextDouble() * 2 - 1) * LIGHTNING_RADIUS;
            int y = jupiter.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, jupiter);
            bolt.moveTo(x, y, z);
            bolt.setVisualOnly(true);
            jupiter.addFreshEntity(bolt);
        }
    }
}