package com.tairitsutek.newgc.Moons.moon.client;

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
public class MoonSkyRenderer {

    private static final float ROTATION_SPEED = 1.0f;
    private static final int STAR_COUNT = 3500;
    private static final float STAR_DISTANCE = 100.0f;

    // ==================== 贴图 ====================
    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");
    private static final ResourceLocation EARTH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/earth_icon.png");
    private static final ResourceLocation VENUS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/venus_icon.png");
    private static final ResourceLocation MARS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/mars_icon.png");
    private static final ResourceLocation JUPITER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/jupiter_icon.png");

    // ==================== 天球距离 ====================
    // 全部设为相同距离 100，避免深度问题
    private static final float SKY_DISTANCE = 100.0f;

    // ==================== 现实视角直径（度）====================
    // 按真实比例：地球 1.9° 是太阳 0.53° 的约 3.6 倍
    // 但真实太小，游戏里看不见，统一乘以一个"可视化放大系数"
    private static final float ANGULAR_SCALE = 8.0f;   // 放大 8 倍

    private static final float SUN_ANGULAR_REAL     = 1.7f;
    private static final float EARTH_ANGULAR_REAL   = 2.5f;
    private static final float VENUS_ANGULAR_REAL   = 0.05f;
    private static final float MARS_ANGULAR_REAL    = 0.02f;
    private static final float JUPITER_ANGULAR_REAL = 0.015f;

    // 放大后的视角直径
    private static final float SUN_ANGULAR     = SUN_ANGULAR_REAL     * ANGULAR_SCALE;
    private static final float EARTH_ANGULAR   = EARTH_ANGULAR_REAL   * ANGULAR_SCALE;
    private static final float VENUS_ANGULAR   = VENUS_ANGULAR_REAL   * ANGULAR_SCALE;
    private static final float MARS_ANGULAR    = MARS_ANGULAR_REAL    * ANGULAR_SCALE;
    private static final float JUPITER_ANGULAR = JUPITER_ANGULAR_REAL * ANGULAR_SCALE;

    private static final float SUN_SIZE     = sizeFromAngularDiameter(SUN_ANGULAR,     SKY_DISTANCE);
    private static final float EARTH_SIZE   = sizeFromAngularDiameter(EARTH_ANGULAR,   SKY_DISTANCE);
    private static final float VENUS_SIZE   = sizeFromAngularDiameter(VENUS_ANGULAR,   SKY_DISTANCE);
    private static final float MARS_SIZE    = sizeFromAngularDiameter(MARS_ANGULAR,    SKY_DISTANCE);
    private static final float JUPITER_SIZE = sizeFromAngularDiameter(JUPITER_ANGULAR, SKY_DISTANCE);

    /**
     * 视角直径 → sprite 半宽
     */
    private static float sizeFromAngularDiameter(float angularDiameterDeg, float distance) {
        float radiusRad = (float) Math.toRadians(angularDiameterDeg * 0.5f);
        return distance * (float) Math.tan(radiusRad);
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MOON_LEVEL_KEY)) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float timeOfDay = mc.level.getTimeOfDay(partialTick);
        float skyRotRad = (float) Math.toRadians(timeOfDay * 360.0f * ROTATION_SPEED);

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.mul(event.getModelViewMatrix());
        mvStack.rotateY((float) Math.toRadians(-90.0f));

        // ========== 第一层：旋转天球（恒星 + 太阳 + 行星）==========
        mvStack.pushMatrix();
        mvStack.rotateX(-skyRotRad);
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

        // 1. 恒星背景
        renderStars();

        // 2. 太阳
        renderCelestialBody(SUN_TEXTURE, SUN_SIZE,
                180.0f, 45.0f,
                1.0f, 1.0f, 1.0f);

        // 3. 金星：永远在太阳 47° 以内，这里取 40°
        renderCelestialBody(VENUS_TEXTURE, VENUS_SIZE,
                140.0f, 48.0f,
                0.95f, 0.95f, 0.85f);

        // 4. 火星：外侧行星，可以出现在任意方向
        renderCelestialBody(MARS_TEXTURE, MARS_SIZE,
                60.0f, 30.0f,
                1.0f, 0.85f, 0.75f);

        // 5. 木星：外侧行星
        renderCelestialBody(JUPITER_TEXTURE, JUPITER_SIZE,
                320.0f, 25.0f,
                1.0f, 0.95f, 0.8f);

        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();

        // ========== 第二层：潮汐锁定地球 ==========
        // 地球位置随月相变化：地球与太阳的角距 = 月相角
        // 新月（timeOfDay=0）：地球和太阳同方向
        // 满月（timeOfDay=0.5）：地球在太阳对面
        // 月球自转周期 = 公转周期，所以地球在月球天上位置几乎固定
        // 我们用一个"地球固定方位角"，让它在观察者坐标系里不随天球转
        // 但为了月相正确，方位角要随 timeOfDay 缓慢改变（周期 8 天里变化一圈）

        // ★ 现实中月球自转周期 27.3 天，地球在月球天空几乎不动
        // 游戏中 timeOfDay 周期是 1 天，为了让玩家能看到月相变化，用更慢的周期
        float earthAzimuth = 90.0f;    // 固定方位角（东方）
        float earthAltitude = 13.0f;   // 固定仰角

        renderCelestialBody(EARTH_TEXTURE, EARTH_SIZE,
                earthAzimuth, earthAltitude,
                1.0f, 1.0f, 1.0f);

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

        RandomSource random = RandomSource.create(20842L);
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
            starBuffer.addVertex(starPos.x + starRight.x - starUp.x, starPos.y + starRight.y - starUp.y, starPos.z - starRight.z - starUp.z).setColor(brightness, brightness, brightness, 1.0f);
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