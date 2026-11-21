package com.tairitsutek.newgc.mixin;

import com.tairitsutek.newgc.gravity.PlanetGravity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 用重力倍率缩放坠落距离，而不是覆盖整个坠落伤害公式。
 *
 * <p>原版 {@code LivingEntity#calculateFallDamage(float, float)} 会读取
 * {@code Attributes.SAFE_FALL_DISTANCE} 和 {@code Attributes.FALL_DAMAGE_MULTIPLIER}，
 * 因此这种写法可以保留羽落、其它模组的属性修改以及本模组后续可能加入的配置。</p>
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityFallDamage {

    @ModifyVariable(
            method = "calculateFallDamage",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float newgc$scaleFallDistance(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;
        double multiplier = PlanetGravity.getMultiplier(self.level());
        if (multiplier == 1.0) {
            return fallDistance;
        }
        return (float) (fallDistance * multiplier);
    }
}
