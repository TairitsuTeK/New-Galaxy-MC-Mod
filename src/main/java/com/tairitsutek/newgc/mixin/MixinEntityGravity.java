package com.tairitsutek.newgc.mixin;

import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntityGravity {

    // 火星：0.38
    @Unique
    private static final double MARS_MULT    = 0.38;
    // 水星：0.4
    @Unique
    private static final double MERCURY_MULT = 0.4;
    // 金星：1.1
    @Unique
    private static final double VENUS_MULT   = 1.1;
    // 谷神星：0.15
    @Unique
    private static final double CERES_MULT   = 0.15;

    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void newgcGravity(CallbackInfoReturnable<Double> cir) {
        Entity self = (Entity) (Object) this;

        double mult = 1.0;

        if (self.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) {
            mult = MARS_MULT;
        } else if (self.level().dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) {
            mult = MERCURY_MULT;
        } else if (self.level().dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) {
            mult = VENUS_MULT;
        } else if (self.level().dimension().equals(ModDimensions.CERES_LEVEL_KEY)) {
            mult = CERES_MULT;
        } else {
            return;
        }

        double original = cir.getReturnValue();
        if (original > 0) {
            cir.setReturnValue(original * mult);
        }
    }
}