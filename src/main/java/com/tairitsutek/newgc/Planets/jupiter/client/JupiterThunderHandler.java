package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
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

    // ===== 冷却范围（刻）=====
    // 平静期：240~480 刻（12~24 秒一次）
    // 暴烈期：4~12 刻（0.2~0.6 秒一次，非常密集）
    private static final int COOLDOWN_MIN_CALM = 240;
    private static final int COOLDOWN_MAX_CALM = 480;

    private static final int COOLDOWN_MIN_STORM = 4;
    private static final int COOLDOWN_MAX_STORM = 12;

    // 每个维度独立的冷却
    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    // 每个维度上一次的强度（用于平滑，避免强度突变时闪电突然爆）
    private static final Map<ResourceKey<Level>, Float> LAST_STRENGTH = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        ServerLevel jupiter = (ServerLevel) event.getLevel();
        ResourceKey<Level> key = jupiter.dimension();

        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            COOLDOWNS.put(key, cooldown - 1);
            return;
        }

        // ===== 读取真实强度（不再截到 1.0）=====
        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(2.5f, strength));

        // 平滑：避免强度突变时闪电一次性爆
        float last = LAST_STRENGTH.getOrDefault(key, strength);
        float smoothed = last + (strength - last) * 0.15f;
        LAST_STRENGTH.put(key, smoothed);

        // ===== 归一化：0 = 平静，1 = 暴烈顶点 =====
        float t = smoothed / 2.5f;

        // ===== 非线性曲线：低强度几乎无闪，高强度爆炸式增长 =====
        // t^2.2 让 0~0.4 区间几乎没闪电，1.0 以上区间闪电密集
        float curve = (float) Math.pow(t, 2.2);

        // ===== 计算冷却范围 =====
        int cooldownMin = (int) (COOLDOWN_MIN_STORM + (COOLDOWN_MIN_CALM - COOLDOWN_MIN_STORM) * (1 - curve));
        int cooldownMax = (int) (COOLDOWN_MAX_STORM + (COOLDOWN_MAX_CALM - COOLDOWN_MAX_STORM) * (1 - curve));
        if (cooldownMax <= cooldownMin) cooldownMax = cooldownMin + 1;

        int newCooldown = cooldownMin + jupiter.random.nextInt(cooldownMax - cooldownMin + 1);
        COOLDOWNS.put(key, newCooldown);

        if (jupiter.players().isEmpty()) return;

        // ===== 暴烈期：一次可能劈多道闪电 =====
        int boltCount = 1;
        if (curve > 0.7f) {
            // 0.7~1.0 区间额外加 0~3 道
            boltCount += jupiter.random.nextInt(1 + (int) ((curve - 0.7f) / 0.3f * 3));
        }

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