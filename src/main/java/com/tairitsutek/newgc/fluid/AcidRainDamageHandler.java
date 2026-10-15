package com.tairitsutek.newgc.fluid;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class AcidRainDamageHandler {

    // 伤害间隔：每 20 刻（1 秒）扣一次
    private static final int DAMAGE_INTERVAL = 20;

    // 扣血量比例：1/3 最大生命值
    private static final float DAMAGE_RATIO = 1.0f / 3.0f;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living.level().isClientSide()) return;

        // 每 1 秒检查一次
        if (living.tickCount % DAMAGE_INTERVAL != 0) return;

        // 判断实体是否接触酸雨流体
        if (!isTouchingAcidRain(living)) return;

        // 扣最大生命值的 1/3
        float damage = living.getMaxHealth() * DAMAGE_RATIO;
        living.hurt(living.damageSources().generic(), damage);
    }

    /**
     * 检查实体是否接触酸雨流体。
     * 酸雨流体的注册名是 newgc:acid_rain。
     */
    private static boolean isTouchingAcidRain(LivingEntity living) {
        // 检查实体脚部所在方块和身体所在方块是否有酸雨流体
        var fluidState = living.level().getFluidState(living.blockPosition());
        if (!fluidState.isEmpty()) {
            var fluidType = fluidState.getType();
            var key = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluidType);
            if (key != null && key.getNamespace().equals(NewGalaxyMain.MODID)
                    && key.getPath().startsWith("acid_rain")) {
                return true;
            }
        }

        // 再检查眼睛位置
        var eyeFluid = living.level().getFluidState(
                net.minecraft.core.BlockPos.containing(living.getEyePosition()));
        if (!eyeFluid.isEmpty()) {
            var fluidType = eyeFluid.getType();
            var key = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluidType);
            if (key != null && key.getNamespace().equals(NewGalaxyMain.MODID)
                    && key.getPath().startsWith("acid_rain")) {
                return true;
            }
        }

        return false;
    }
}