package com.tairitsutek.newgc.Moons.moon.client;

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
public class MoonGravityHandler {

    private static final ResourceLocation GRAVITY_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "moon_low_gravity");

    /**
     * 月球重力系数（地球 = 1.0）
     * 真实物理：0.165（地球的 1/6）
     * 游戏化：0.3（推荐，和谷神星一致，手感舒适）
     */
    private static final double GRAVITY_MULTIPLIER = 0.3;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        boolean isInMoon = player.level().dimension().equals(ModDimensions.MOON_LEVEL_KEY);
        AttributeInstance gravityAttr = player.getAttribute(Attributes.GRAVITY);
        if (gravityAttr == null) return;

        boolean hasModifier = gravityAttr.hasModifier(GRAVITY_MODIFIER_ID);

        if (isInMoon && !hasModifier) {
            AttributeModifier modifier = new AttributeModifier(
                    GRAVITY_MODIFIER_ID,
                    GRAVITY_MULTIPLIER - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            gravityAttr.addTransientModifier(modifier);
        } else if (!isInMoon && hasModifier) {
            gravityAttr.removeModifier(GRAVITY_MODIFIER_ID);
        }
    }
}