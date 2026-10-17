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

    private static final int COOLDOWN_MIN_CALM  = 120;
    private static final int COOLDOWN_MAX_CALM  = 240;
    private static final int COOLDOWN_MIN_STORM = 2;
    private static final int COOLDOWN_MAX_STORM = 8;

    private static final float NORM_PEAK = JupiterStormManager.PEAK_REFERENCE;
    private static final float CURVE_EXPONENT = 1.2f;

    /** 风暴时冷却最大加速倍数（curve=1 时，每 tick 消耗 5 点冷却） */
    private static final int MAX_COOLDOWN_ACCEL = 4;

    private static final int DEBUG_LOG_INTERVAL = 20;

    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();

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

        // ===== 读取风暴强度（只读一次，冷却和劈雷共用） =====
        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(JupiterStormManager.SUDDEN_REFERENCE, strength));

        float t = Math.min(1f, strength / NORM_PEAK);
        float curve = (float) Math.pow(t, CURVE_EXPONENT);

        // ===== 冷却：风暴越强，消耗越快（解决体感延迟） =====
        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            int decrement = 1 + Math.round(curve * MAX_COOLDOWN_ACCEL);
            cooldown -= decrement;
            if (cooldown > 0) {
                COOLDOWNS.put(key, cooldown);
                return;
            }
            // 冷却被压缩到 0 以下，本 tick 直接劈
        }

        // ===== 计算新冷却 =====
        int cooldownMin = Math.round(COOLDOWN_MIN_STORM
                + (COOLDOWN_MIN_CALM - COOLDOWN_MIN_STORM) * (1 - curve));
        int cooldownMax = Math.round(COOLDOWN_MAX_STORM
                + (COOLDOWN_MAX_CALM - COOLDOWN_MAX_STORM) * (1 - curve));
        if (cooldownMax <= cooldownMin) cooldownMax = cooldownMin + 1;
        int newCooldown = cooldownMin + jupiter.random.nextInt(cooldownMax - cooldownMin + 1);
        COOLDOWNS.put(key, newCooldown);

        // ===== 调试日志 =====
        if (jupiter.getGameTime() % DEBUG_LOG_INTERVAL == 0) {
            System.out.println("[Thunder] strength=" + String.format("%.2f", strength)
                    + " curve=" + String.format("%.2f", curve)
                    + " cooldown=" + newCooldown
                    + " players=" + jupiter.players().size());
        }

        if (jupiter.players().isEmpty()) return;

        // ===== 闪电数量 =====
        int extra = 0;
        if (curve > 0.5f) {
            int maxExtra = 1 + (int) ((curve - 0.5f) / 0.5f * 4);
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