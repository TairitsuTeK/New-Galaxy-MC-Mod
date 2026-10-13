package com.tairitsutek.newgc.Planets.venus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class VenusSkyRenderer {

    private static final ResourceLocation CLOUDS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/clouds.png");

    // ===== 高空维加酸雨（半空蒸发，不落地） =====
    private static final float RAIN_SPEED  = 85f;
    private static final float RAIN_RANGE  = 22f;
    private static final float RAIN_HEIGHT = 38f;    // 酸雨下移（云层下方）
    private static final float RAIN_RADIUS = 28f;
    private static final float RAIN_GRID   = 1.15f;
    private static final float RAIN_LENGTH = 1.8f;
    private static final float RAIN_WIDTH  = 0.06f;
    private static final float RAIN_WIND_X = 0.50f;
    private static final float RAIN_WIND_Z = 0.12f;
    private static final float RAIN_ALPHA  = 0.50f;
    private static final float RAIN_FADE   = 0.15f;
    private static final float RAIN_R      = 0.58f;
    private static final float RAIN_G      = 0.85f;
    private static final float RAIN_B      = 0.35f;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.VENUS_LEVEL_KEY)) return;

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            renderClouds(event, mc);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            renderAcidRain(event, mc);
        }
    }

    // ===== 云层（天空远景，跟随相机） =====
    private static void renderClouds(RenderLevelStageEvent event, Minecraft mc) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float gameTime = (float) mc.level.getGameTime() + partialTick;

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        Matrix4f skyMat = new Matrix4f(event.getModelViewMatrix());
        skyMat.setTranslation(0f, 0f, 0f);
        mvStack.mul(skyMat);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CLOUDS_TEXTURE);

        Tesselator tess = Tesselator.getInstance();

        // 云层降低（75/62/50）+ 变大（尺寸/纹理重复度同步增大）
        drawCloudLayer(tess, gameTime * 0.0008f, gameTime * 0.00030f, 75f, 420f, 18f, 0.88f, 0.85f, 0.32f, 0.80f);
        drawCloudLayer(tess, gameTime * 0.0012f, gameTime * 0.00045f, 62f, 380f, 14f, 0.84f, 0.80f, 0.26f, 0.62f);
        drawCloudLayer(tess, gameTime * 0.0016f, gameTime * 0.00060f, 50f, 340f, 10f, 0.78f, 0.74f, 0.20f, 0.45f);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void drawCloudLayer(Tesselator tess, float u, float v,
                                       float y, float size, float uvRepeat,
                                       float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r, g, b, a);
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.addVertex(-size, y, -size).setUv(u, v);
        buf.addVertex(-size, y,  size).setUv(u, v + uvRepeat);
        buf.addVertex( size, y,  size).setUv(u + uvRepeat, v + uvRepeat);
        buf.addVertex( size, y, -size).setUv(u + uvRepeat, v);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    // ===== 高空酸雨（蒸发渐隐） =====
    private static void renderAcidRain(RenderLevelStageEvent event, Minecraft mc) {
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) return;
        Vec3 cam = camera.getPosition();

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float gameTime = (float) mc.level.getGameTime() + partialTick;

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float camX = (float) cam.x;
        float camY = (float) cam.y;
        float camZ = (float) cam.z;

        int minGX = Mth.floor((camX - RAIN_RADIUS) / RAIN_GRID);
        int maxGX = Mth.floor((camX + RAIN_RADIUS) / RAIN_GRID);
        int minGZ = Mth.floor((camZ - RAIN_RADIUS) / RAIN_GRID);
        int maxGZ = Mth.floor((camZ + RAIN_RADIUS) / RAIN_GRID);

        int cr = (int) (RAIN_R * 255f);
        int cg = (int) (RAIN_G * 255f);
        int cb = (int) (RAIN_B * 255f);
        int ca = (int) (RAIN_ALPHA * 255f);
        int caFade = (int) (ca * RAIN_FADE);

        for (int gx = minGX; gx <= maxGX; gx++) {
            for (int gz = minGZ; gz <= maxGZ; gz++) {
                float h1 = hash01(gx, gz, 1);
                float h2 = hash01(gx, gz, 2);
                float h3 = hash01(gx, gz, 3);
                float h4 = hash01(gx, gz, 4);

                float px = gx * RAIN_GRID + (h1 - 0.5f) * RAIN_GRID * 0.8f;
                float pz = gz * RAIN_GRID + (h2 - 0.5f) * RAIN_GRID * 0.8f;

                float ddx = px - camX;
                float ddz = pz - camZ;
                if (ddx * ddx + ddz * ddz > RAIN_RADIUS * RAIN_RADIUS) continue;

                float length = RAIN_LENGTH * (0.7f + h4 * 0.8f);
                float fall = (gameTime * RAIN_SPEED + h3 * RAIN_RANGE) % RAIN_RANGE;
                float py = camY + RAIN_HEIGHT + RAIN_RANGE * 0.5f - fall;

                float bx = px + RAIN_WIND_X * length;
                float bz = pz + RAIN_WIND_Z * length;
                float by = py - length;

                float len = (float) Math.sqrt(ddx * ddx + ddz * ddz);
                if (len < 1e-3f) continue;
                float rx = ddz / len;
                float rz = -ddx / len;
                float hw = RAIN_WIDTH * 0.5f;

                float tlX = px - rx * hw, tlZ = pz - rz * hw;
                float trX = px + rx * hw, trZ = pz + rz * hw;
                float blX = bx - rx * hw, blZ = bz - rz * hw;
                float brX = bx + rx * hw, brZ = bz + rz * hw;

                buf.addVertex(tlX, py, tlZ).setColor(cr, cg, cb, ca);
                buf.addVertex(blX, by, blZ).setColor(cr, cg, cb, caFade);
                buf.addVertex(brX, by, brZ).setColor(cr, cg, cb, caFade);
                buf.addVertex(trX, py, trZ).setColor(cr, cg, cb, ca);
            }
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());

        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static float hash01(int x, int z, int salt) {
        long h = (long) x * 0x27d4eb2dL ^ (long) z * 0x165667b1L ^ salt * 0x9e3779b9L;
        h = (h ^ (h >>> 15)) * 0x45d9f3bL;
        h = (h ^ (h >>> 15)) * 0x45d9f3bL;
        h ^= h >>> 15;
        return (h & 0xFFFFFFL) / 16777216.0f;
    }
}
