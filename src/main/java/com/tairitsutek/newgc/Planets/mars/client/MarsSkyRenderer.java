package com.tairitsutek.newgc.Planets.mars.client;

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
public class MarsSkyRenderer {

    private static final float ROTATION_SPEED = 1.0f;

    // 火星有小行星带之外的星空视野，星星数量适中
    private static final int STAR_COUNT = 3000;
    private static final float STAR_DISTANCE = 100.0f;

    // 贴图
    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");
    private static final ResourceLocation EARTH_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/earth_icon.png");
    private static final ResourceLocation VENUS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/venus_icon.png");
    private static final ResourceLocation JUPITER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/jupiter_icon.png");

    // 太阳（火星距太阳 1.52 AU，比地球远，太阳看起来略小）
    private static final float SUN_SIZE = 8.0f;
    private static final float SUN_DISTANCE = 100.0f;

    // 地球（外侧行星，靠近太阳升起/落下的方向）
    // 火星看地球：距角最大约 47°，多数时候在太阳附近
    private static final float EARTH_SIZE = 1.5f;
    private static final float EARTH_DISTANCE = 99.0f;
    private static final float EARTH_OFFSET_X = (float) Math.toRadians(35.0f);
    private static final float EARTH_OFFSET_Y = (float) Math.toRadians(8.0f);

    // 金星（内侧行星，也靠近太阳，与地球反向）
    private static final float VENUS_SIZE = 1.2f;
    private static final float VENUS_DISTANCE = 99.0f;
    private static final float VENUS_OFFSET_X = (float) Math.toRadians(-30.0f);
    private static final float VENUS_OFFSET_Y = (float) Math.toRadians(-6.0f);

    // 木星（外侧行星，位置独立于太阳，出现在"半夜"方向）
    private static final float JUPITER_SIZE = 5.0f;
    private static final float JUPITER_DISTANCE = 98.0f;
    private static final float JUPITER_OFFSET_X = (float) Math.toRadians(150.0f);
    private static final float JUPITER_OFFSET_Y = (float) Math.toRadians(20.0f);

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float timeOfDay = mc.level.getTimeOfDay(partialTick);

        // ===== 相机 + 昼夜自转矩阵 =====
        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.mul(event.getModelViewMatrix());
        mvStack.rotateY((float) Math.toRadians(-90.0f));
        mvStack.rotateX((float) Math.toRadians(timeOfDay * 360.0f * ROTATION_SPEED));
        RenderSystem.applyModelViewMatrix();

        // ===== 渲染状态 =====
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        // ==================== 1. 星星 ====================
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator starTess = Tesselator.getInstance();
        BufferBuilder starBuffer = starTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RandomSource random = RandomSource.create(30721L);   // 火星专属种子

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

            // 火星有稀薄大气，星空略亮
            float brightness = 0.20f + random.nextFloat() * 0.40f;

            starBuffer.addVertex(starPos.x - starRight.x - starUp.x, starPos.y - starRight.y - starUp.y, starPos.z - starRight.z - starUp.z).setColor(brightness, brightness, brightness, 1.0f);
            starBuffer.addVertex(starPos.x + starRight.x - starUp.x, starPos.y + starRight.y - starUp.y, starPos.z - starRight.z - starUp.z).setColor(brightness, brightness, brightness, 1.0f);
            starBuffer.addVertex(starPos.x + starRight.x + starUp.x, starPos.y + starRight.y + starUp.y, starPos.z + starRight.z + starUp.z).setColor(brightness, brightness, brightness, 1.0f);
            starBuffer.addVertex(starPos.x - starRight.x + starUp.x, starPos.y - starRight.y + starUp.y, starPos.z - starRight.z + starUp.z).setColor(brightness, brightness, brightness, 1.0f);
        }

        BufferUploader.drawWithShader(starBuffer.buildOrThrow());

        // ==================== 2. 太阳 ====================
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

        // ==================== 3. 地球 ====================
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        RenderSystem.setShaderColor(0.85f, 0.85f, 0.85f, 1.0f);

        Tesselator earthTess = Tesselator.getInstance();
        BufferBuilder earthBuffer = earthTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        drawCelestialQuad(earthBuffer, EARTH_DISTANCE, EARTH_SIZE, EARTH_OFFSET_X, EARTH_OFFSET_Y);

        BufferUploader.drawWithShader(earthBuffer.buildOrThrow());

        // ==================== 4. 金星 ====================
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VENUS_TEXTURE);
        RenderSystem.setShaderColor(0.9f, 0.85f, 0.75f, 1.0f);

        Tesselator venusTess = Tesselator.getInstance();
        BufferBuilder venusBuffer = venusTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        drawCelestialQuad(venusBuffer, VENUS_DISTANCE, VENUS_SIZE, VENUS_OFFSET_X, VENUS_OFFSET_Y);

        BufferUploader.drawWithShader(venusBuffer.buildOrThrow());

        // ==================== 5. 木星 ====================
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, JUPITER_TEXTURE);
        RenderSystem.setShaderColor(0.9f, 0.85f, 0.75f, 1.0f);

        Tesselator jupiterTess = Tesselator.getInstance();
        BufferBuilder jupiterBuffer = jupiterTess.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        drawCelestialQuad(jupiterBuffer, JUPITER_DISTANCE, JUPITER_SIZE, JUPITER_OFFSET_X, JUPITER_OFFSET_Y);

        BufferUploader.drawWithShader(jupiterBuffer.buildOrThrow());

        // ===== 恢复 =====
        mvStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);   // 关键：恢复默认颜色
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** 在天顶平面上，按角度偏移画一个天体四边形 */
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

    /** 把一个天顶平面上的点，按水平/垂直角度偏移旋转 */
    private static Vector3f rotateOffset(float x, float y, float z,
                                         float offsetX, float offsetY) {
        Vector3f v = new Vector3f(x, y, z);
        if (offsetY != 0) v.rotateZ(offsetY);
        if (offsetX != 0) v.rotateX(offsetX);
        return v;
    }
}