package com.tairitsutek.newgc.Planets.jupiter.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class JupiterSkyRenderer {

    private static final float ROTATION_SPEED = 1.0f;

    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");
    private static final ResourceLocation IO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/io_icon.png");
    private static final ResourceLocation EUROPA_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/europa_icon.png");
    private static final ResourceLocation GANYMEDE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/ganymede_icon.png");
    private static final ResourceLocation CALLISTO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/callisto_icon.png");

    private static final float SUN_SIZE = 1.0f;
    private static final float SUN_DISTANCE = 100.0f;

    private static final float IO_SIZE = 1.5f;
    private static final float IO_DISTANCE = 99.0f;
    private static final float IO_OFFSET_X = (float) Math.toRadians(30.0f);
    private static final float IO_OFFSET_Y = (float) Math.toRadians(8.0f);

    private static final float EUROPA_SIZE = 1.0f;
    private static final float EUROPA_DISTANCE = 99.5f;
    private static final float EUROPA_OFFSET_X = (float) Math.toRadians(-40.0f);
    private static final float EUROPA_OFFSET_Y = (float) Math.toRadians(-8.0f);

    private static final float GANYMEDE_SIZE = 1.2f;
    private static final float GANYMEDE_DISTANCE = 100.0f;
    private static final float GANYMEDE_OFFSET_X = (float) Math.toRadians(150.0f);
    private static final float GANYMEDE_OFFSET_Y = (float) Math.toRadians(-6.0f);

    private static final float CALLISTO_SIZE = 0.8f;
    private static final float CALLISTO_DISTANCE = 100.5f;
    private static final float CALLISTO_OFFSET_X = (float) Math.toRadians(-150.0f);
    private static final float CALLISTO_OFFSET_Y = (float) Math.toRadians(12.0f);

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float timeOfDay = mc.level.getTimeOfDay(partialTick);

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.mul(event.getModelViewMatrix());
        mvStack.rotateY((float) Math.toRadians(-90.0f));
        mvStack.rotateX((float) Math.toRadians(timeOfDay * 360.0f * ROTATION_SPEED));
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

        // 太阳
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator sunTess = Tesselator.getInstance();
        BufferBuilder sunBuffer = sunTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        sunBuffer.addVertex(-SUN_SIZE, SUN_DISTANCE, -SUN_SIZE).setUv(0.0f, 0.0f);
        sunBuffer.addVertex(-SUN_SIZE, SUN_DISTANCE,  SUN_SIZE).setUv(0.0f, 1.0f);
        sunBuffer.addVertex( SUN_SIZE, SUN_DISTANCE,  SUN_SIZE).setUv(1.0f, 1.0f);
        sunBuffer.addVertex( SUN_SIZE, SUN_DISTANCE, -SUN_SIZE).setUv(1.0f, 0.0f);

        BufferUploader.drawWithShader(sunBuffer.buildOrThrow());

        // Io
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, IO_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 0.85f, 0.5f, 1.0f);

        Tesselator ioTess = Tesselator.getInstance();
        BufferBuilder ioBuffer = ioTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawCelestialQuad(ioBuffer, IO_DISTANCE, IO_SIZE, IO_OFFSET_X, IO_OFFSET_Y);
        BufferUploader.drawWithShader(ioBuffer.buildOrThrow());

        // Europa
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EUROPA_TEXTURE);
        RenderSystem.setShaderColor(0.95f, 0.95f, 0.9f, 1.0f);

        Tesselator europaTess = Tesselator.getInstance();
        BufferBuilder europaBuffer = europaTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawCelestialQuad(europaBuffer, EUROPA_DISTANCE, EUROPA_SIZE, EUROPA_OFFSET_X, EUROPA_OFFSET_Y);
        BufferUploader.drawWithShader(europaBuffer.buildOrThrow());

        // Ganymede
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GANYMEDE_TEXTURE);
        RenderSystem.setShaderColor(0.85f, 0.85f, 0.85f, 1.0f);

        Tesselator ganymedeTess = Tesselator.getInstance();
        BufferBuilder ganymedeBuffer = ganymedeTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawCelestialQuad(ganymedeBuffer, GANYMEDE_DISTANCE, GANYMEDE_SIZE, GANYMEDE_OFFSET_X, GANYMEDE_OFFSET_Y);
        BufferUploader.drawWithShader(ganymedeBuffer.buildOrThrow());

        // Callisto
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CALLISTO_TEXTURE);
        RenderSystem.setShaderColor(0.7f, 0.68f, 0.6f, 1.0f);

        Tesselator callistoTess = Tesselator.getInstance();
        BufferBuilder callistoBuffer = callistoTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawCelestialQuad(callistoBuffer, CALLISTO_DISTANCE, CALLISTO_SIZE, CALLISTO_OFFSET_X, CALLISTO_OFFSET_Y);
        BufferUploader.drawWithShader(callistoBuffer.buildOrThrow());

        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void drawCelestialQuad(BufferBuilder buffer,
                                          float distance,
                                          float size,
                                          float offsetX,
                                          float offsetY) {
        Vector3f topLeft     = rotateOffset(-size, distance, -size, offsetX, offsetY);
        Vector3f bottomLeft  = rotateOffset(-size, distance,  size, offsetX, offsetY);
        Vector3f bottomRight = rotateOffset( size, distance,  size, offsetX, offsetY);
        Vector3f topRight    = rotateOffset( size, distance, -size, offsetX, offsetY);

        buffer.addVertex(topLeft.x,     topLeft.y,     topLeft.z).setUv(0.0f, 0.0f);
        buffer.addVertex(bottomLeft.x,  bottomLeft.y,  bottomLeft.z).setUv(0.0f, 1.0f);
        buffer.addVertex(bottomRight.x, bottomRight.y, bottomRight.z).setUv(1.0f, 1.0f);
        buffer.addVertex(topRight.x,    topRight.y,    topRight.z).setUv(1.0f, 0.0f);
    }

    private static Vector3f rotateOffset(float x, float y, float z,
                                         float offsetX, float offsetY) {
        Vector3f v = new Vector3f(x, y, z);
        if (offsetY != 0) v.rotateZ(offsetY);
        if (offsetX != 0) v.rotateX(offsetX);
        return v;
    }
}