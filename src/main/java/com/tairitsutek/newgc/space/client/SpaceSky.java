package com.tairitsutek.newgc.space.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.space.SpaceLocations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 太空漂流维度天空。
 * <p>
 * 只绘制星点和按火箭实际 X/Z 位置计算的最近恒星图标；
 * 太阳系显示太阳，Proxima b 空间显示 Proxima Centauri 红矮星。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class SpaceSky {

    public static final ResourceLocation EFFECT =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "space_sky");

    private static final int STAR_COUNT = 2400;
    private static final float SKY_DISTANCE = 100.0f;
    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");

    private SpaceSky() {
    }

    @SubscribeEvent
    public static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(EFFECT, new Effects());
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.SPACE_LEVEL_KEY)) {
            return;
        }

        ResourceLocation star = nearestStarTexture(mc);
        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(event.getModelViewMatrix());
        stack.rotateY((float) Math.toRadians(-90.0f));
        RenderSystem.applyModelViewMatrix();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        renderStars();
        if (star != null) {
            renderStar(star, 18.0f);
        }

        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static ResourceLocation nearestStarTexture(Minecraft mc) {
        if (mc.player == null
                || !(mc.player.getVehicle() instanceof RocketEntity rocket)) {
            return SUN_TEXTURE;
        }
        return SpaceLocations.nearest(rocket.getX(), rocket.getZ()).icon();
    }

    private static void renderStars() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RandomSource random = RandomSource.create(0x5A17C0DEL);

        for (int i = 0; i < STAR_COUNT; i++) {
            float cosTheta = random.nextFloat() * 2.0f - 1.0f;
            float sinTheta = Mth.sqrt(1.0f - cosTheta * cosTheta);
            float phi = random.nextFloat() * Mth.TWO_PI;
            Vector3f pos = new Vector3f(
                    sinTheta * Mth.cos(phi), cosTheta, sinTheta * Mth.sin(phi)
            ).mul(SKY_DISTANCE);

            Quaternionf rotation = new Quaternionf()
                    .rotateTo(new Vector3f(0.0f, 0.0f, -1.0f), pos);
            float size = 0.05f + random.nextFloat() * 0.055f;
            Vector3f right = new Vector3f(1.0f, 0.0f, 0.0f).rotate(rotation).mul(size);
            Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f).rotate(rotation).mul(size);
            float brightness = 0.18f + random.nextFloat() * 0.62f;

            buffer.addVertex(pos.x - right.x - up.x, pos.y - right.y - up.y, pos.z - right.z - up.z)
                    .setColor(brightness, brightness, brightness, 1.0f);
            buffer.addVertex(pos.x + right.x - up.x, pos.y + right.y - up.y, pos.z + right.z - up.z)
                    .setColor(brightness, brightness, brightness, 1.0f);
            buffer.addVertex(pos.x + right.x + up.x, pos.y + right.y + up.y, pos.z + right.z + up.z)
                    .setColor(brightness, brightness, brightness, 1.0f);
            buffer.addVertex(pos.x - right.x + up.x, pos.y - right.y + up.y, pos.z - right.z + up.z)
                    .setColor(brightness, brightness, brightness, 1.0f);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderStar(ResourceLocation texture, float size) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        addQuad(buffer, size, 142.0f, 26.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addQuad(BufferBuilder buffer, float size,
                                float azimuthDeg, float altitudeDeg) {
        float az = (float) Math.toRadians(azimuthDeg);
        float alt = (float) Math.toRadians(altitudeDeg);
        float cosAlt = (float) Math.cos(alt);
        Vector3f center = new Vector3f(
                SKY_DISTANCE * (float) Math.sin(az) * cosAlt,
                SKY_DISTANCE * (float) Math.sin(alt),
                SKY_DISTANCE * (float) Math.cos(az) * cosAlt
        );

        Vector3f dir = new Vector3f(center).normalize();
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        if (Math.abs(dir.y) > 0.99f) {
            up.set(0.0f, 0.0f, 1.0f);
        }
        Vector3f right = new Vector3f(dir).cross(up).normalize().mul(size);
        Vector3f localUp = new Vector3f(dir).cross(right).normalize().mul(size);

        Vector3f tl = new Vector3f(center).sub(right).add(localUp);
        Vector3f bl = new Vector3f(center).sub(right).sub(localUp);
        Vector3f br = new Vector3f(center).add(right).sub(localUp);
        Vector3f tr = new Vector3f(center).add(right).add(localUp);

        buffer.addVertex(tl.x, tl.y, tl.z).setUv(0.0f, 0.0f);
        buffer.addVertex(bl.x, bl.y, bl.z).setUv(0.0f, 1.0f);
        buffer.addVertex(br.x, br.y, br.z).setUv(1.0f, 1.0f);
        buffer.addVertex(tr.x, tr.y, tr.z).setUv(1.0f, 0.0f);
    }

    private static final class Effects extends DimensionSpecialEffects {
        private Effects() {
            super(Float.NaN, false, SkyType.NONE, false, false);
        }

        @Override
        public float[] getSunriseColor(float timeOfDay, float partialTick) {
            return null;
        }

        @Override
        public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
            return Vec3.ZERO;
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }
    }
}
