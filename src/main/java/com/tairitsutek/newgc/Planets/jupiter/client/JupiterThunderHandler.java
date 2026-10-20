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
    private static final int COOLDOWN_MIN_CALM = 120;   // 6s
    private static final int COOLDOWN_MAX_CALM = 240;   // 12s
    private static final int COOLDOWN_MIN_STORM = 2;    // 0.1s
    private static final int COOLDOWN_MAX_STORM = 8;    // 0.4s

    // ===== 归一化参考值 =====
    private static final float NORM_PEAK = JupiterStormManager.PEAK_REFERENCE;
    private static final float NORM_SUDDEN = JupiterStormManager.SUDDEN_REFERENCE;

    /** 线性曲线（curve = t），让中等强度也能有较密的闪电 */
    private static final float CURVE_EXPONENT = 1.0f;

    /** 风暴时冷却最大加速倍数：curve=1 时每 tick 消耗 5 点冷却 */
    private static final int MAX_COOLDOWN_ACCEL = 4;

    private static final int DEBUG_LOG_INTERVAL = 20;

    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Float> LAST_STRENGTH = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // 严格过滤：只处理服务端 + 木星维度
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
        float curve = (float) Math.pow(t, CURVE_EXPONENT);

        // ===== 冷却连续耗散 =====
        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            // 每 tick 按强度扣冷却，加速风暴爆发
            int decrement = 1 + Math.round(curve * MAX_COOLDOWN_ACCEL);
            cooldown -= decrement;
            if (cooldown > 0) {
                COOLDOWNS.put(key, cooldown);
                return;
            }
            // 本 tick 冷却归零，直接劈雷
        }

        // ===== 计算新冷却范围（依据当前 curve） =====
        int cooldownMin = Math.round(COOLDOWN_MIN_STORM
                + (COOLDOWN_MIN_CALM - COOLDOWN_MIN_STORM) * (1 - curve));
        int cooldownMax = Math.round(COOLDOWN_MAX_STORM
                + (COOLDOWN_MAX_CALM - COOLDOWN_MAX_STORM) * (1 - curve));

        if (cooldownMax <= cooldownMin) {
            cooldownMax = cooldownMin + 1;
        }

        int newCooldown = cooldownMin + jupiter.random.nextInt(cooldownMax - cooldownMin + 1);
        COOLDOWNS.put(key, newCooldown);

        // ===== 调试日志 =====
        if (jupiter.getGameTime() % DEBUG_LOG_INTERVAL == 0) {
            System.out.println("[Thunder] strength=" + String.format("%.2f", strength)
                    + " smoothed=" + String.format("%.2f", smoothed)
                    + " curve=" + String.format("%.2f", curve)
                    + " cooldown=" + newCooldown
                    + " players=" + jupiter.players().size());
        }

        if (jupiter.players().isEmpty()) return;

        // ===== 决定劈雷数量 =====
        int extra = 0;
        if (curve > 0.5f) {
            // curve 0.5 -> 最多 2 道; curve 1.0 -> 最多 6 道
            int maxExtra = 1 + (int) ((curve - 0.5f) / 0.5f * 5);
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