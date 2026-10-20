package com.tairitsutek.newgc.Moons.io.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class IoSkyRenderer {

    private static final int STAR_COUNT = 3500;
    private static final float STAR_DISTANCE = 100.0f;

    // ==================== 贴图 ====================
    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");
    private static final ResourceLocation EARTH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/earth_icon.png");
    private static final ResourceLocation MARS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/mars_icon.png");
    private static final ResourceLocation JUPITER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/jupiter_icon.png");
    private static final ResourceLocation SATURN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/saturn_icon.png");

    // ==================== 天球距离 ====================
    private static final float SKY_DISTANCE = 100.0f;

    // ==================== 视角直径 ====================
    private static final float ANGULAR_SCALE = 8.0f;

    // 木卫一距太阳 5.2 AU，太阳看起来比地球小
    private static final float SUN_ANGULAR_REAL     = 1.4f / 5.2f;   // ≈ 0.27°
    private static final float EARTH_ANGULAR_REAL   = 0.10f / 4.2f;  // 地球在木星附近视角更小
    private static final float MARS_ANGULAR_REAL    = 0.10f / 2.2f;
    private static final float SATURN_ANGULAR_REAL  = 0.12f / 1.5f;

    // ★ 木星：木卫一看木星视角直径 ≈ 19.5°（满月约 0.5°，39 倍）
    // 这是木卫一天空最震撼的景观
    private static final float JUPITER_ANGULAR_REAL = 26.0f;

    private static final float SUN_SIZE     = sizeFromAngularDiameter(SUN_ANGULAR_REAL * ANGULAR_SCALE, SKY_DISTANCE);
    private static final float EARTH_SIZE   = sizeFromAngularDiameter(EARTH_ANGULAR_REAL * ANGULAR_SCALE, SKY_DISTANCE);
    private static final float MARS_SIZE    = sizeFromAngularDiameter(MARS_ANGULAR_REAL * ANGULAR_SCALE, SKY_DISTANCE);
    private static final float SATURN_SIZE  = sizeFromAngularDiameter(SATURN_ANGULAR_REAL * ANGULAR_SCALE, SKY_DISTANCE);
    // 木星不用 ANGULAR_SCALE 放大，因为它本来就已经很大了
    private static final float JUPITER_SIZE = sizeFromAngularDiameter(JUPITER_ANGULAR_REAL, SKY_DISTANCE);

    // ==================== 太阳初始位置（正午位置）====================
    // 木卫一轨道在木星赤道面附近，太阳正午高挂
    private static final float SUN_INIT_AZIMUTH  = 180.0f;   // 正南
    private static final float SUN_INIT_ALTITUDE = 65.0f;    // 高挂天空

    private static float sizeFromAngularDiameter(float angularDiameterDeg, float distance) {
        float radiusRad = (float) Math.toRadians(angularDiameterDeg * 0.5f);
        return distance * (float) Math.tan(radiusRad);
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        long dayTime = mc.level.getDayTime();
        float dayFrac = ((dayTime % 24000L) + partialTick) / 24000.0f;

        // 木卫一轨道周期约 42.5 小时，但这里按 MC 标准 20 分钟日循环即可
        float skyRotFrac = dayFrac - 0.25f;
        float skyRotRad = skyRotFrac * 2.0f * (float) Math.PI;

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.mul(event.getModelViewMatrix());
        mvStack.rotateY((float) Math.toRadians(-90.0f));

        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        // ========== 第一层：随天球旋转（恒星 + 太阳 + 其它行星）==========
        mvStack.pushMatrix();
        mvStack.rotateX(skyRotRad);
        RenderSystem.applyModelViewMatrix();

        // 恒星背景
        renderStars();

        // 太阳：初始位置 = 正午位置
        renderCelestialBody(SUN_TEXTURE, SUN_SIZE,
                SUN_INIT_AZIMUTH, SUN_INIT_ALTITUDE,
                1.0f, 1.0f, 1.0f);

        // 火星：外侧行星，偏红
        renderCelestialBody(MARS_TEXTURE, MARS_SIZE,
                120.0f, 40.0f,
                1.0f, 0.8f, 0.7f);

        // 土星：更外侧行星，带环
        renderCelestialBody(SATURN_TEXTURE, SATURN_SIZE,
                300.0f, 20.0f,
                1.0f, 0.9f, 0.7f);

        // 地球：内侧行星，偏蓝
        renderCelestialBody(EARTH_TEXTURE, EARTH_SIZE,
                40.0f, 25.0f,
                0.55f, 0.7f, 1.0f);

        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();

        // ========== 第二层：潮汐锁定（木星不随天球旋转）==========
        // 木卫一被木星潮汐锁定，木星在天空几乎不动
        // 木星视角直径约 19.5°，非常巨大
        renderCelestialBody(JUPITER_TEXTURE, JUPITER_SIZE,
                90.0f, 30.0f,
                1.0f, 0.85f, 0.75f);

        // ===== 恢复 =====
        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** 渲染恒星背景 */
    private static void renderStars() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator starTess = Tesselator.getInstance();
        BufferBuilder starBuffer = starTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RandomSource random = RandomSource.create(31415L);
        for (int i = 0; i < STAR_COUNT; i++) {
            float cosTheta = random.nextFloat() * 2.0f - 1.0f;
            float sinTheta = Mth.sqrt(1.0f - cosTheta * cosTheta);
            float phi = random.nextFloat() * Mth.TWO_PI;

            float x = sinTheta * Mth.cos(phi);
            float y = cosTheta;
            float z = sinTheta * Mth.sin(phi);

            Vector3f starPos = new Vector3f(x, y, z).mul(STAR_DISTANCE);
            Quaternionf starRot = new Quaternionf().rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), starPos);
            float size = 0.06f + random.nextFloat() * 0.05f;
            Vector3f starRight = new Vector3f(1.0F, 0.0F, 0.0F).rotate(starRot).mul(size);
            Vector3f starUp    = new Vector3f(0.0F, 1.0F, 0.0F).rotate(starRot).mul(size);

            float brightness = 0.18f + random.nextFloat() * 0.45f;

            starBuffer.addVertex(starPos.x - starRight.x - starUp.x, starPos.y - starRight.y - starUp.y, starPos.z - starRight.z - starUp.z).setColor(brightness, brightness, brightness, 1.0f);
            starBuffer.addVertex(starPos.x + starRight.x - starUp.x, starPos.y + starRight.y - starUp.y, starPos.z + starRight.z - starUp.z).setColor(brightness, brightness, brightness, 1.0f);
            starBuffer.addVertex(starPos.x + starRight.x + starUp.x, starPos.y + starRight.y + starUp.y, starPos.z + starRight.z + starUp.z).setColor(brightness, brightness, brightness, 1.0f);
            starBuffer.addVertex(starPos.x - starRight.x + starUp.x, starPos.y - starRight.y + starUp.y, starPos.z - starRight.z + starUp.z).setColor(brightness, brightness, brightness, 1.0f);
        }

        BufferUploader.drawWithShader(starBuffer.buildOrThrow());
    }

    /** 渲染单个天体 */
    private static void renderCelestialBody(ResourceLocation texture, float size,
                                            float azimuthDeg, float altitudeDeg,
                                            float r, float g, float b) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(r, g, b, 1.0f);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        drawCelestialQuadAt(buffer, SKY_DISTANCE, size, azimuthDeg, altitudeDeg);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /** 根据方位角 + 仰角计算三维位置 */
    private static Vector3f skyPosition(float distance, float azimuthDeg, float altitudeDeg) {
        float az = (float) Math.toRadians(azimuthDeg);
        float al = (float) Math.toRadians(altitudeDeg);
        float cosAl = (float) Math.cos(al);
        float x = distance * (float) Math.sin(az) * cosAl;
        float y = distance * (float) Math.sin(al);
        float z = distance * (float) Math.cos(az) * cosAl;
        return new Vector3f(x, y, z);
    }

    /** 绘制始终面向观察者的四边形 */
    private static void drawCelestialQuadAt(BufferBuilder buffer,
                                            float distance, float size,
                                            float azimuthDeg, float altitudeDeg) {
        Vector3f center = skyPosition(distance, azimuthDeg, altitudeDeg);

        Vector3f dir = new Vector3f(center).normalize();
        Vector3f worldUp = new Vector3f(0f, 1f, 0f);
        if (Math.abs(dir.y) > 0.99f) worldUp.set(0f, 0f, 1f);

        Vector3f right = new Vector3f(dir).cross(worldUp).normalize().mul(size);
        Vector3f up    = new Vector3f(dir).cross(right).normalize().mul(size);

        Vector3f tl = new Vector3f(center).sub(right).add(up);
        Vector3f bl = new Vector3f(center).sub(right).sub(up);
        Vector3f br = new Vector3f(center).add(right).sub(up);
        Vector3f tr = new Vector3f(center).add(right).add(up);

        buffer.addVertex(tl.x, tl.y, tl.z).setUv(0.0f, 0.0f);
        buffer.addVertex(bl.x, bl.y, bl.z).setUv(0.0f, 1.0f);
        buffer.addVertex(br.x, br.y, br.z).setUv(1.0f, 1.0f);
        buffer.addVertex(tr.x, tr.y, tr.z).setUv(1.0f, 0.0f);
    }
}