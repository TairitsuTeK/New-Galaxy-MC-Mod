package com.tairitsutek.newgc.Moons.europa.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
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

import java.util.List;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class EuropaSky {

    public static final ResourceLocation EFFECT =
            ResourceLocation.fromNamespaceAndPath("newgc", "europa_sky");

    private static final double ORBITAL_AU = 5.203;
    private static final int STAR_COUNT = 2200;
    private static final float SKY_DISTANCE = 100.0f;
    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");

    private record Nearby(ResourceLocation texture, double radius, double separation) {
    }

    private static final List<Nearby> NEARBY = List.of(
            new Nearby(icon("jupiter"), 11.21000, 0.004490),
            new Nearby(icon("io"), 0.28600, 0.001670),
            new Nearby(icon("ganymede"), 0.41300, 0.002670),
            new Nearby(icon("callisto"), 0.37800, 0.008110)
    );

    private EuropaSky() {
    }

    private static ResourceLocation icon(String id) {
        return ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/" + id + "_icon.png");
    }

    @SubscribeEvent
    public static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(EFFECT, new Effects());
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.EUROPA_LEVEL_KEY)) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float timeOfDay = mc.level.getTimeOfDay(partialTick);

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.mul(event.getModelViewMatrix());
        mvStack.rotateY((float) Math.toRadians(-90.0f));
        mvStack.rotateX((float) Math.toRadians(timeOfDay * 360.0f));
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

        renderStars("europa".hashCode());
        renderSun();

        for (int i = 0; i < NEARBY.size(); i++) {
            renderBody(NEARBY.get(i), i);
        }

        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void renderStars(int seed) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RandomSource random = RandomSource.create(seed ^ 0x51A7C0DEL);

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
            float brightness = 0.15f + random.nextFloat() * 0.55f;

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

    private static void renderSun() {
        double angularDegrees = Mth.clamp(
                0.53 * 8.0 / Math.max(0.15, ORBITAL_AU), 0.10, 12.0);
        float size = sizeFromAngular(angularDegrees);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 0.94f, 0.78f, 1.0f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        addQuad(buffer, size, 180.0f, 65.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderBody(Nearby body, int index) {
        double angularDegrees = Math.toDegrees(
                2.0 * Math.atan(body.radius() * 0.002 / Math.max(0.0001, body.separation())));
        float size = sizeFromAngular(Mth.clamp(angularDegrees, 0.2, 40.0));
        size = Mth.clamp(size, 0.6f, 18.0f);

        int hash = body.texture().hashCode();
        float azimuth = Math.floorMod(hash, 360);
        float altitude = 18.0f + Math.floorMod(hash / 360, 48);
        if (index % 2 == 1) azimuth = (azimuth + 180.0f) % 360.0f;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, body.texture());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        addQuad(buffer, size, azimuth, altitude);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static float sizeFromAngular(double angularDegrees) {
        double radius = Math.toRadians(angularDegrees) * 0.5;
        return (float) (SKY_DISTANCE * Math.tan(radius));
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
        if (Math.abs(dir.y) > 0.99f) up.set(0.0f, 0.0f, 1.0f);
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
            return fogColor;
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }
    }

}
