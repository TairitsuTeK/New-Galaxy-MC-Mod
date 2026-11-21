package com.tairitsutek.newgc.mixin;

import com.tairitsutek.newgc.gravity.PlanetGravity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 唯一的重力注入点。
 *
 * <p>{@code Entity#getGravity()} 是 final 方法，内部会调用
 * {@code getDefaultGravity()}；对 {@code LivingEntity} 来说后者读取
 * {@code Attributes.GRAVITY}。因此在这里统一乘倍率可以同时覆盖玩家、生物和掉落物，
 * 且不会再和 Attribute 修改器重复计算。</p>
 */
@Mixin(Entity.class)
public abstract class MixinEntityGravity {

    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void newgc$applyDimensionGravity(CallbackInfoReturnable<Double> cir) {
        Entity self = (Entity) (Object) this;
        double original = cir.getReturnValue();
        if (original <= 0.0) {
            return;
        }

        double multiplier = PlanetGravity.getMultiplier(self.level());
        if (multiplier != 1.0) {
            cir.setReturnValue(original * multiplier);
        }
    }
}
