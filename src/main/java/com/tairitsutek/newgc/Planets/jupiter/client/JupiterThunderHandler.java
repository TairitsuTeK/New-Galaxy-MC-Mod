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

    // ===== 冷却范围（刻）=====
    // 平静期：120~240 刻（6~12 秒一次）
    // 暴烈期：2~8 刻（0.1~0.4 秒一次）
    private static final int COOLDOWN_MIN_CALM = 120;
    private static final int COOLDOWN_MAX_CALM = 240;

    private static final int COOLDOWN_MIN_STORM = 2;
    private static final int COOLDOWN_MAX_STORM = 8;

    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();
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

        float strength = JupiterStormManager.getStormStrength();
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0f;
        strength = Math.max(0f, Math.min(2.5f, strength));

        // 平滑：0.15 → 0.3，更快跟上
        float last = LAST_STRENGTH.getOrDefault(key, strength);
        float smoothed = last + (strength - last) * 0.3f;
        LAST_STRENGTH.put(key, smoothed);

        float t = smoothed / 2.5f;

        // 曲线：2.2 → 1.5，更线性
        float curve = (float) Math.pow(t, 1.5);

        // 冷却范围
        int cooldownMin = (int) (COOLDOWN_MIN_STORM + (COOLDOWN_MIN_CALM - COOLDOWN_MIN_STORM) * (1 - curve));
        int cooldownMax = (int) (COOLDOWN_MAX_STORM + (COOLDOWN_MAX_CALM - COOLDOWN_MAX_STORM) * (1 - curve));
        if (cooldownMax <= cooldownMin) cooldownMax = cooldownMin + 1;

        int newCooldown = cooldownMin + jupiter.random.nextInt(cooldownMax - cooldownMin + 1);
        COOLDOWNS.put(key, newCooldown);

        if (jupiter.players().isEmpty()) return;

        // 暴烈期：一次劈多道闪电
        int boltCount = 1;
        if (curve > 0.4f) {
            // 0.4~1.0 区间额外加 0~3 道
            boltCount += jupiter.random.nextInt(1 + (int) ((curve - 0.4f) / 0.6f * 3));
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