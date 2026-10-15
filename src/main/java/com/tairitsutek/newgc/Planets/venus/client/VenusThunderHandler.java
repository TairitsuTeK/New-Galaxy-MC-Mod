package com.tairitsutek.newgc.Planets.venus.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class VenusThunderHandler {

    private static final int MIN_TICKS = 100;
    private static final int MAX_TICKS = 200;

    private static final int LIGHTNING_RADIUS = 24;

    private static int cooldown = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) return;

        ServerLevel venus = (ServerLevel) event.getLevel();

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        cooldown = MIN_TICKS + venus.random.nextInt(MAX_TICKS - MIN_TICKS + 1);

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