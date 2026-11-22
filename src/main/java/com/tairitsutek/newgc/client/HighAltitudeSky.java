package com.tairitsutek.newgc.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.entity.RocketDestinations;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.gui.StarMapAssets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * 火箭上升时的高空天空。
 * <p>
 * 天空不依赖昼夜时间：火箭从大气层内爬升到一定高度后，天幕逐渐变黑，
 * 星星和出发星球附近的天体图标逐渐出现。只有乘坐上升/轨道阶段的火箭时生效，
 * 因此不会影响玩家在地面上的正常天空。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class HighAltitudeSky {

    /** 开始离开大气层的高度（相对发射点）。 */
    private static final double ATMOSPHERE_START = 160.0D;
    /** 完全进入太空的高度（相对发射点）。 */
    private static final double ATMOSPHERE_FULL = 480.0D;

    private static final float SKY_RADIUS = 60.0F;
    private static final float STAR_DISTANCE = 55.0F;
    private static final float BODY_DISTANCE = 50.0F;
    private static final float SUN_DISTANCE = 40.0F;
    private static final float SUN_SIZE = 6.0F;
    private static final ResourceLocation SUN_TEXTURE = ResourceLocation
            .fromNamespaceAndPath("minecraft", "textures/environment/sun.png");
    private static final int STAR_COUNT = 1800;

    private static final float[] BODY_SIZES = {12.0F, 8.0F, 6.0F, 5.0F, 4.0F};
    private static final float[] BODY_AZIMUTHS = {210.0F, 150.0F, 250.0F, 30.0F, 330.0F};
    private static final float[] BODY_ALTITUDES = {24.0F, 15.0F, 12.0F, 30.0F, 18.0F};

    private HighAltitudeSky() {
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float fade = altitudeFade();
        if (fade <= 0.0F) {
            return;
        }
        float keep = 1.0F - fade;
        event.setRed(event.getRed() * keep);
        event.setGreen(event.getGreen() * keep);
        event.setBlue(event.getBlue() * keep);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        float fade = altitudeFade();
        if (fade <= 0.005F) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        renderDome(fade);
        renderStars(fade);
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        renderSun(mc.level.getTimeOfDay(partialTick), fade);
        renderNearbyBodies(mc, fade);

        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** 以火箭相对发射点的高度计算平滑的天空变黑进度。 */
    private static float altitudeFade() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return 0.0F;
        }
        if (mc.level.dimension().equals(ModDimensions.SPACE_LEVEL_KEY)) {
            return 0.0F;
        }
        if (!(mc.player.getVehicle() instanceof RocketEntity rocket)
                || (rocket.getState() != RocketEntity.STATE_ASCENT
                && rocket.getState() != RocketEntity.STATE_ORBIT)) {
            return 0.0F;
        }

        double climbed = rocket.getY() - rocket.getStartY();
        float t = (float) Mth.clamp(
                (climbed - ATMOSPHERE_START) / (ATMOSPHERE_FULL - ATMOSPHERE_START),
                0.0D, 1.0D);
        return t * t * (3.0F - 2.0F * t);
    }

    /**
     * 一个位于所有原版/模组天空天体前面的黑色天空盒。
     * <p>
     * AFTER_SKY 之后地形尚未绘制，因此黑色只覆盖天空；随后星星和天体图标
     * 再画在天空盒内侧，保证无论当前维度原本是什么天空，都能看到太空效果。
     */
    private static void renderDome(float fade) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int alpha = Mth.clamp((int) (fade * 255.0F), 0, 255);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float r = SKY_RADIUS;
        addColoredQuad(buffer, alpha,
                -r, -r, -r, -r, -r, r, -r, r, r, -r, r, -r);
        addColoredQuad(buffer, alpha,
                r, -r, r, r, -r, -r, r, r, -r, r, r, r);
        addColoredQuad(buffer, alpha,
                -r, -r, r, -r, -r, -r, -r, r, -r, -r, r, r);
        addColoredQuad(buffer, alpha,
                r, -r, -r, r, -r, r, r, r, r, r, r, -r);
        addColoredQuad(buffer, alpha,
                -r, r, -r, -r, r, r, r, r, r, r, r, -r);
        addColoredQuad(buffer, alpha,
                -r, -r, -r, r, -r, -r, r, -r, r, -r, -r, r);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void renderStars(float fade) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RandomSource random = RandomSource.create(0x48494748L);
        int alpha = Mth.clamp((int) (fade * 255.0F), 0, 255);

        for (int i = 0; i < STAR_COUNT; i++) {
            float cosTheta = random.nextFloat() * 2.0F - 1.0F;
            float sinTheta = Mth.sqrt(1.0F - cosTheta * cosTheta);
            float phi = random.nextFloat() * Mth.TWO_PI;
            Vector3f pos = new Vector3f(
                    sinTheta * Mth.cos(phi), cosTheta, sinTheta * Mth.sin(phi)
            ).mul(STAR_DISTANCE);

            Quaternionf rotation = new Quaternionf()
                    .rotateTo(new Vector3f(0.0F, 0.0F, -1.0F), pos);
            float size = 0.04F + random.nextFloat() * 0.055F;
            Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(rotation).mul(size);
            Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(rotation).mul(size);
            int brightness = Mth.clamp((int) ((0.22F + random.nextFloat() * 0.68F)
                    * fade * 255.0F), 0, 255);

            addColorQuad(buffer,
                    pos.x - right.x - up.x, pos.y - right.y - up.y, pos.z - right.z - up.z,
                    pos.x + right.x - up.x, pos.y + right.y - up.y, pos.z + right.z - up.z,
                    pos.x + right.x + up.x, pos.y + right.y + up.y, pos.z + right.z + up.z,
                    pos.x - right.x + up.x, pos.y - right.y + up.y, pos.z - right.z + up.z,
                    brightness, brightness, brightness, alpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /** 太空中的太阳仍直接使用原版 sun.png，不引入任何自定义太阳贴图。 */
    private static void renderSun(float timeOfDay, float fade) {
        if (fade <= 0.12F) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fade);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.rotateY((float) Math.toRadians(-90.0F));
        stack.rotateX((float) Math.toRadians((timeOfDay - 0.25F) * 360.0F));
        RenderSystem.applyModelViewMatrix();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(-SUN_SIZE, SUN_DISTANCE, -SUN_SIZE).setUv(0.0F, 0.0F);
        buffer.addVertex(-SUN_SIZE, SUN_DISTANCE, SUN_SIZE).setUv(0.0F, 1.0F);
        buffer.addVertex(SUN_SIZE, SUN_DISTANCE, SUN_SIZE).setUv(1.0F, 1.0F);
        buffer.addVertex(SUN_SIZE, SUN_DISTANCE, -SUN_SIZE).setUv(1.0F, 0.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderNearbyBodies(Minecraft mc, float fade) {
        List<ResourceLocation> textures = RocketDestinations
                .skyNeighbours(mc.level.dimension())
                .stream()
                .map(HighAltitudeSky::iconFor)
                .toList();
        if (textures.isEmpty() || fade <= 0.15F) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int alpha = Mth.clamp((int) ((fade - 0.15F) / 0.85F * 255.0F), 0, 255);
        for (int i = 0; i < textures.size() && i < BODY_SIZES.length; i++) {
            RenderSystem.setShaderTexture(0, textures.get(i));
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.begin(
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            addTexturedQuad(buffer, BODY_DISTANCE, BODY_SIZES[i],
                    BODY_AZIMUTHS[i], BODY_ALTITUDES[i]);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static ResourceLocation iconFor(ResourceKey<Level> key) {
        String path = key.location().getPath();
        if (key.equals(Level.OVERWORLD)) {
            path = "earth";
        }
        return StarMapAssets.tex(path + "_icon.png");
    }

    private static void addColoredQuad(BufferBuilder buffer, int alpha,
                                       float x0, float y0, float z0,
                                       float x1, float y1, float z1,
                                       float x2, float y2, float z2,
                                       float x3, float y3, float z3) {
        addColorQuad(buffer, x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3,
                0, 0, 0, alpha);
    }

    private static void addColorQuad(BufferBuilder buffer,
                                     float x0, float y0, float z0,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float x3, float y3, float z3,
                                     int r, int g, int b, int a) {
        buffer.addVertex(x0, y0, z0).setColor(r, g, b, a);
        buffer.addVertex(x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(x2, y2, z2).setColor(r, g, b, a);
        buffer.addVertex(x3, y3, z3).setColor(r, g, b, a);
    }

    private static void addTexturedQuad(BufferBuilder buffer, float distance,
                                        float size, float azimuthDeg, float altitudeDeg) {
        float az = (float) Math.toRadians(azimuthDeg);
        float alt = (float) Math.toRadians(altitudeDeg);
        float cosAlt = (float) Math.cos(alt);
        Vector3f center = new Vector3f(
                distance * (float) Math.sin(az) * cosAlt,
                distance * (float) Math.sin(alt),
                distance * (float) Math.cos(az) * cosAlt
        );

        Vector3f dir = new Vector3f(center).normalize();
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        if (Math.abs(dir.y) > 0.99F) {
            up.set(0.0F, 0.0F, 1.0F);
        }
        Vector3f right = new Vector3f(dir).cross(up).normalize().mul(size);
        Vector3f localUp = new Vector3f(dir).cross(right).normalize().mul(size);

        Vector3f tl = new Vector3f(center).sub(right).add(localUp);
        Vector3f bl = new Vector3f(center).sub(right).sub(localUp);
        Vector3f br = new Vector3f(center).add(right).sub(localUp);
        Vector3f tr = new Vector3f(center).add(right).add(localUp);

        buffer.addVertex(tl.x, tl.y, tl.z).setUv(0.0F, 0.0F);
        buffer.addVertex(bl.x, bl.y, bl.z).setUv(0.0F, 1.0F);
        buffer.addVertex(br.x, br.y, br.z).setUv(1.0F, 1.0F);
        buffer.addVertex(tr.x, tr.y, tr.z).setUv(1.0F, 0.0F);
    }
}
