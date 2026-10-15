package com.tairitsutek.newgc.Planets.venus.client;

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
public class VenusThunderHandler {

    private static final int LIGHTNING_RADIUS = 24;

    private static final Map<ResourceKey<Level>, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) return;

        ServerLevel venus = (ServerLevel) event.getLevel();
        ResourceKey<Level> key = venus.dimension();

        int cooldown = COOLDOWNS.getOrDefault(key, 0);
        if (cooldown > 0) {
            COOLDOWNS.put(key, cooldown - 1);
            return;
        }

        int newCooldown = 400 + venus.random.nextInt(400);
        COOLDOWNS.put(key, newCooldown);

        if (venus.players().isEmpty()) return;

        ServerPlayer player = venus.players().get(venus.random.nextInt(venus.players().size()));
        double x = player.getX() + (venus.random.nextDouble() * 2 - 1) * LIGHTNING_RADIUS;
        double z = player.getZ() + (venus.random.nextDouble() * 2 - 1) * LIGHTNING_RADIUS;
        int y = venus.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);

        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, venus);
        bolt.moveTo(x, y, z);
        bolt.setVisualOnly(true);
        venus.addFreshEntity(bolt);
    }
}