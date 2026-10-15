package com.tairitsutek.newgc.Planets.jupiter;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterWindHandler {

    private static final double WIND_STRENGTH = 0.015;

    private static final float WIND_ANGLE = (float) Math.toRadians(35.0f);

    private static final double VERTICAL_JITTER = 0.003;

    private static final double RANDOM_JITTER = 0.01;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!serverPlayer.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (serverPlayer.isCreative() || serverPlayer.isSpectator()) return;

        double dirX = Math.cos(WIND_ANGLE);
        double dirZ = Math.sin(WIND_ANGLE);

        double jitterX = (serverPlayer.getRandom().nextDouble() - 0.5) * RANDOM_JITTER;
        double jitterZ = (serverPlayer.getRandom().nextDouble() - 0.5) * RANDOM_JITTER;
        double jitterY = (serverPlayer.getRandom().nextDouble() - 0.5) * VERTICAL_JITTER;

        double pushX = dirX * WIND_STRENGTH + jitterX;
        double pushZ = dirZ * WIND_STRENGTH + jitterZ;
        double pushY = jitterY;

        serverPlayer.push(pushX, pushY, pushZ);
    }
}