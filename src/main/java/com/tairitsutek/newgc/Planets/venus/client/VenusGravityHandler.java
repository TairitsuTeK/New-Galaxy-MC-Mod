package com.tairitsutek.newgc.Planets.venus.client;

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
public class VenusGravityHandler {

    private static final ResourceLocation GRAVITY_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "venus_high_gravity");

    // 相对主世界（1.0）的重力系数
    private static final double GRAVITY_MULTIPLIER = 0.9;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        boolean isInVenus = player.level().dimension().equals(ModDimensions.VENUS_LEVEL_KEY);
        AttributeInstance gravityAttr = player.getAttribute(Attributes.GRAVITY);
        if (gravityAttr == null) return;

        boolean hasModifier = gravityAttr.hasModifier(GRAVITY_MODIFIER_ID);

        if (isInVenus && !hasModifier) {
            AttributeModifier modifier = new AttributeModifier(
                    GRAVITY_MODIFIER_ID,
                    GRAVITY_MULTIPLIER - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            gravityAttr.addTransientModifier(modifier);
        } else if (!isInVenus && hasModifier) {
            gravityAttr.removeModifier(GRAVITY_MODIFIER_ID);
        }
    }
}