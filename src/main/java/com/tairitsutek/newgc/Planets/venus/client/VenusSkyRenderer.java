package com.tairitsutek.newgc.Planets.venus.client;

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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;

/**
 * Venus has a thick, opaque atmosphere: no stars, a hazy orange dome and a
 * dim, broad sun. This replaces the dark {@code SkyType.NONE} background that
 * otherwise made Venus look like a Nether dimension.
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class VenusSkyRenderer {

    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");
    private static final float SKY_RADIUS = 100.0f;

    private VenusSkyRenderer() {
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) return;

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

        renderAtmosphere();
        renderSun();

        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void renderAtmosphere() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int latBands = 18;
        int lonBands = 36;
        for (int lat = 0; lat < latBands; lat++) {
            float lat0 = (float) Math.toRadians(-90.0f + 180.0f * lat / latBands);
            float lat1 = (float) Math.toRadians(-90.0f + 180.0f * (lat + 1) / latBands);
            for (int lon = 0; lon < lonBands; lon++) {
                float lon0 = (float) Math.toRadians(360.0f * lon / lonBands);
                float lon1 = (float) Math.toRadians(360.0f * (lon + 1) / lonBands);

                // Horizon: bright sulphur yellow. Zenith: dark brown haze.
                float y = (lat0 + lat1) * 0.5f;
                float t = Mth.clamp((y + (float) Math.PI * 0.5f) / (float) Math.PI, 0.0f, 1.0f);
                float r = 0.96f - 0.50f * t;
                float g = 0.65f - 0.42f * t;
                float b = 0.24f - 0.19f * t;

                addSphereVertex(buffer, lat0, lon0, r, g, b);
                addSphereVertex(buffer, lat0, lon1, r, g, b);
                addSphereVertex(buffer, lat1, lon1, r, g, b);
                addSphereVertex(buffer, lat1, lon0, r, g, b);
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addSphereVertex(BufferBuilder buffer, float lat, float lon,
                                       float r, float g, float b) {
        float x = SKY_RADIUS * (float) Math.cos(lat) * (float) Math.cos(lon);
        float y = SKY_RADIUS * (float) Math.sin(lat);
        float z = SKY_RADIUS * (float) Math.cos(lat) * (float) Math.sin(lon);
        buffer.addVertex(x, y, z).setColor(r, g, b, 1.0f);
    }

    private static void renderSun() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 0.82f, 0.42f, 1.0f);

        float size = 18.0f;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(-size, 95.0f, -size).setUv(0.0f, 0.0f);
        buffer.addVertex(-size, 95.0f, size).setUv(0.0f, 1.0f);
        buffer.addVertex(size, 95.0f, size).setUv(1.0f, 1.0f);
        buffer.addVertex(size, 95.0f, -size).setUv(1.0f, 0.0f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
