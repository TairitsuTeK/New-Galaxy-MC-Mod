package com.tairitsutek.newgc.Planets.mars.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MarsSkyEffects extends DimensionSpecialEffects {

    public MarsSkyEffects() {
        super(0.0f, false, SkyType.NONE, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        // 火星：暗红棕
        float r = 0.05f + 0.30f * brightness;
        float g = 0.02f + 0.13f * brightness;
        float b = 0.01f + 0.07f * brightness;
        return new Vec3(r, g, b);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTick) {
        // ★ 关键修复：返回 null（不画红晕）或长度 4 的 float[]
        return null;

        // 如果要画淡淡橙红日出红晕：
        // return new float[]{0.6f, 0.25f, 0.12f, 1.0f};
    }
}