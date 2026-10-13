package com.tairitsutek.newgc.Moons.moon.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MoonSkyEffects extends DimensionSpecialEffects {

    public MoonSkyEffects() {
        super(0.0f, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        float r = 0.00f + 0.06f * brightness;
        float g = 0.00f + 0.06f * brightness;
        float b = 0.00f + 0.07f * brightness;
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