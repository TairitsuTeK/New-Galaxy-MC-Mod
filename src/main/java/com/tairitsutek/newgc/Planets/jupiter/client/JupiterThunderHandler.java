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

    private static final int LIGHTNING_RADIUS = 16;

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

        float strength = JupiterStormManager.getStormStrength();
        // 关键：钳制 strength 到 0~1，避免算出负数
        strength = Math.max(0.0f, Math.min(1.0f, strength));

        int dynamicMin = (int) (20 - 17 * strength);
        int dynamicMax = (int) (40 - 34 * strength);

        // 再兜底一次，确保范围合法
        dynamicMin = Math.max(3, dynamicMin);
        dynamicMax = Math.max(dynamicMin + 1, dynamicMax);

        cooldown = dynamicMin + jupiter.random.nextInt(dynamicMax - dynamicMin + 1);

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