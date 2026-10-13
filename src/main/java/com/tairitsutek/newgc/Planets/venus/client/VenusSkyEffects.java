package com.tairitsutek.newgc.Planets.venus.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class VenusSkyEffects extends DimensionSpecialEffects {

    public VenusSkyEffects() {
        // cloudHeight = Float.NaN → 禁用原版天空的云（金星用自己的云）
        super(Float.NaN, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        float dayR = 0.84f, dayG = 0.81f, dayB = 0.32f;
        float nightR = 0.16f, nightG = 0.14f, nightB = 0.06f;

        float t = Mth.clamp(brightness, 0f, 1f);
        t = t * t * (3f - 2f * t);

        float r = nightR + (dayR - nightR) * t;
        float g = nightG + (dayG - nightG) * t;
        float b = nightB + (dayB - nightB) * t;
        return new Vec3(r, g, b);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        return new float[]{0.95f, 0.52f, 0.18f, 0.55f};
    }
}
