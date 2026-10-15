package com.tairitsutek.newgc.Planets.jupiter.client;

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
public class JupiterThunderHandler {

    private static final int MIN_TICKS = 10;
    private static final int MAX_TICKS = 20;
    private static final int LIGHTNING_RADIUS = 64;

    private static int cooldown = 0;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        ServerLevel jupiter = (ServerLevel) event.getLevel();

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        cooldown = MIN_TICKS + jupiter.random.nextInt(MAX_TICKS - MIN_TICKS + 1);

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