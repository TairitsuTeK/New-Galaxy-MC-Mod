package com.tairitsutek.newgc.Planets.venus.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class VenusSkyEffects extends DimensionSpecialEffects {

    public VenusSkyEffects() {
        // cloudHeight = Float.NaN → 禁用原版天空的云（金星用自己的酸雨）
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        // 金星：明显黄，但不荧光
        float dayR = 0.84f, dayG = 0.74f, dayB = 0.36f;
        float nightR = 0.18f, nightG = 0.15f, nightB = 0.08f;

        float t = Mth.clamp(brightness, 0f, 1f);
        t = t * t * (3f - 2f * t);

        float r = nightR + (dayR - nightR) * t;
        float g = nightG + (dayG - nightG) * t;
        float b = nightB + (dayB - nightB) * t;
        return new Vec3(r, g, b);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;   // 开启地形雾
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        // 柔和的黄褐日落
        return new float[]{0.88f, 0.62f, 0.28f, 0.40f};
    }
}