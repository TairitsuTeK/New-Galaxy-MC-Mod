package com.tairitsutek.newgc.mixin;

import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityGravity {

    @Unique
    private static final double MARS_MULT    = 0.38;
    @Unique
    private static final double MERCURY_MULT = 0.4;
    @Unique
    private static final double VENUS_MULT   = 1.1;
    @Unique
    private static final double CERES_MULT   = 0.15;

    @Unique
    private static final float MARS_SAFE       = 3.0f / 0.38f;   // ≈7.895
    @Unique
    private static final float MARS_DMG_MUL    = 0.38f;
    @Unique
    private static final float MERCURY_SAFE    = 7.5f;
    @Unique
    private static final float MERCURY_DMG_MUL = 0.4f;
    @Unique
    private static final float VENUS_SAFE      = 3.0f / 1.1f;    // ≈2.727
    @Unique
    private static final float VENUS_DMG_MUL   = 1.1f;
    @Unique
    private static final float CERES_SAFE      = 3.0f / 0.15f;   // = 20.0
    @Unique
    private static final float CERES_DMG_MUL   = 0.15f;

    @Redirect(
            method = "travel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;")
    )
    private Vec3 newgcGravity(Vec3 original, double x, double y, double z) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (y == -0.08) {
            double mult = 1.0;
            if (self.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) {
                mult = MARS_MULT;
            } else if (self.level().dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) {
                mult = MERCURY_MULT;
            } else if (self.level().dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) {
                mult = VENUS_MULT;
            } else if (self.level().dimension().equals(ModDimensions.CERES_LEVEL_KEY)) {
                mult = CERES_MULT;
            }
            if (mult != 1.0) {
                return original.add(x, y * mult, z);
            }
        }
        return original.add(x, y, z);
    }

    @Inject(
            method = "calculateFallDamage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void newgcFallDamage(float fallDistance, float damageMultiplier,
                                 CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        float safeHeight;
        float dmgMul;

        if (self.level().dimension().equals(ModDimensions.MARS_LEVEL_KEY)) {
            safeHeight = MARS_SAFE;
            dmgMul = MARS_DMG_MUL;
        } else if (self.level().dimension().equals(ModDimensions.MERCURY_LEVEL_KEY)) {
            safeHeight = MERCURY_SAFE;
            dmgMul = MERCURY_DMG_MUL;
        } else if (self.level().dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) {
            safeHeight = VENUS_SAFE;
            dmgMul = VENUS_DMG_MUL;
        } else if (self.level().dimension().equals(ModDimensions.CERES_LEVEL_KEY)) {
            safeHeight = CERES_SAFE;
            dmgMul = CERES_DMG_MUL;
        } else {
            return;
        }

        float jumpBoost = 0f;
        var jump = self.getEffect(MobEffects.JUMP);
        if (jump != null) {
            jumpBoost = (jump.getAmplifier() + 1) * 0.5f;
        }

        float effective = fallDistance - safeHeight - jumpBoost;
        if (effective <= 0f) {
            cir.setReturnValue(0);
            return;
        }

        int dmg = Mth.ceil(effective * damageMultiplier * dmgMul);
        cir.setReturnValue(Math.max(0, dmg));
    }
}