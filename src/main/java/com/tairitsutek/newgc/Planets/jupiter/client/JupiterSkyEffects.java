package com.tairitsutek.newgc.Planets.jupiter.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class JupiterSkyEffects extends DimensionSpecialEffects {

    public JupiterSkyEffects() {
        super(192.0f, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        float r = 0.30f + 0.50f * brightness;
        float g = 0.22f + 0.38f * brightness;
        float b = 0.14f + 0.22f * brightness;
        return new Vec3(r, g, b);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;    // ★ 从 false 改成 true
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        return null;
    }
}