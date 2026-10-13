package com.tairitsutek.newgc.Moons.phobos.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PhobosSkyEffects extends DimensionSpecialEffects {

    public PhobosSkyEffects() {
        super(0.0f, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        float t = Mth.clamp(brightness, 0f, 1f);
        t = t * t * (3f - 2f * t);

        float r = 0.07f * t;
        float g = 0.05f * t;
        float b = 0.04f * t;
        return new Vec3(r, g, b);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        return null;
    }
}
