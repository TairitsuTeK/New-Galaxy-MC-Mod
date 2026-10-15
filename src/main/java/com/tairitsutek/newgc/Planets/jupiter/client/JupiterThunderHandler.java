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

    // 每个维度独立的冷却
    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;

        // 严格判断维度
        if (!event.getLevel().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        ServerLevel jupiter = (ServerLevel) event.getLevel();
        ResourceKey<Level> key = jupiter.dimension();

        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            COOLDOWNS.put(key, cooldown - 1);
            return;
        }

        float strength = JupiterStormManager.getStormStrength();
        strength = Math.max(0.0f, Math.min(1.0f, strength));

        int dynamicMin = Math.max(20, (int) (120 - 100 * strength));
        int dynamicMax = Math.max(dynamicMin + 1, (int) (240 - 200 * strength));

        int newCooldown = dynamicMin + jupiter.random.nextInt(dynamicMax - dynamicMin + 1);
        COOLDOWNS.put(key, newCooldown);

        if (jupiter.players().isEmpty()) return;

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