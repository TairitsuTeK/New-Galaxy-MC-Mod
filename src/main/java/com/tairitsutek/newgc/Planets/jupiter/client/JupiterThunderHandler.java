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

    // ===== 冷却时间范围（tick）=====
    // 平静期（t=0）：20~30秒劈一次（雷击极少）
    private static final int COOLDOWN_MAX_CALM = 600;
    private static final int COOLDOWN_MIN_CALM = 400;

    // 暴烈期（t=1）：0.1~0.4秒劈一次（狂轰滥炸）
    private static final int COOLDOWN_MAX_STORM = 8;
    private static final int COOLDOWN_MIN_STORM = 2;

    // ===== 归一化参考值 =====
    private static final float NORM_SUDDEN = JupiterStormManager.SUDDEN_REFERENCE;

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
            return;
        }

        // ===== 读取并平滑强度 =====
        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(NORM_SUDDEN, strength));

        float last = LAST_STRENGTH.getOrDefault(key, strength);
        float smoothed = last + (strength - last) * 0.1f;
        LAST_STRENGTH.put(key, smoothed);

        // 归一化到 0~1
        float t = Math.min(1f, smoothed / NORM_SUDDEN);

        // ===== 冷却倒计时 =====
        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            COOLDOWNS.put(key, cooldown - 1);
            return; // 冷却没结束，直接返回
        }

        // ===== 冷却结束，执行劈雷 =====
        if (jupiter.players().isEmpty()) {
            COOLDOWNS.put(key, 20);
            return;
        }

        // 1. 决定劈雷数量 (t=0时最多1道，t=1时最多7道)
        int extra = 0;
        if (t > 0.2f) {
            // 线性增长，烈度只要稍微上去一点，就会开始多劈
            extra = jupiter.random.nextInt((int)(t * 6) + 1);
        }
        int boltCount = 1 + extra;

        // 2. 生成闪电
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

        // 3. 根据当前烈度，计算下一次劈雷的冷却时间
        // ★ 修改：改为纯线性插值（去掉 Math.pow），让中等强度也能有高频雷击
        int nextMin = Math.round(COOLDOWN_MIN_CALM + (COOLDOWN_MIN_STORM - COOLDOWN_MIN_CALM) * t);
        int nextMax = Math.round(COOLDOWN_MAX_CALM + (COOLDOWN_MAX_STORM - COOLDOWN_MAX_CALM) * t);

        if (nextMax <= nextMin) nextMax = nextMin + 1;

        int newCooldown = nextMin + jupiter.random.nextInt(nextMax - nextMin + 1);
        COOLDOWNS.put(key, newCooldown);
    }
}