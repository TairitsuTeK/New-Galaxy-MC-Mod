package com.tairitsutek.newgc.Moons.io.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class IoGravityHandler {

    private static final ResourceLocation GRAVITY_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io_low_gravity");

    private static final double GRAVITY_MULTIPLIER = 0.183;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        boolean isInIo = player.level().dimension().equals(ModDimensions.IO_LEVEL_KEY);
        AttributeInstance gravityAttr = player.getAttribute(Attributes.GRAVITY);
        if (gravityAttr == null) return;

        boolean hasModifier = gravityAttr.hasModifier(GRAVITY_MODIFIER_ID);

        if (isInIo && !hasModifier) {
            AttributeModifier modifier = new AttributeModifier(
                    GRAVITY_MODIFIER_ID,
                    GRAVITY_MULTIPLIER - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            gravityAttr.addTransientModifier(modifier);
        } else if (!isInIo && hasModifier) {
            gravityAttr.removeModifier(GRAVITY_MODIFIER_ID);
        }
    }
}