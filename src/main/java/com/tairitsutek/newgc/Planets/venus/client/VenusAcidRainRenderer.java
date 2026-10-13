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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class VenusAcidRainRenderer {

    private VenusAcidRainRenderer() {}

    public static final class Config {
        private Config() {}

        public static final float RADIUS = 56f;
        public static final float GRID = 0.9f;
        public static final float RANGE = 24f;
        public static final float WIND_X = 1.2f;
        public static final float WIND_Z = 0.35f;
        public static final float FADE = 0.08f;

        public static final float[] LAYER_SPEED = {0.55f, 0.85f, 1.25f};
        public static final float[] LAYER_WIDTH = {2.4f, 1.6f, 1.0f};
        public static final float[] LAYER_ALPHA = {0.22f, 0.30f, 0.42f};
        public static final float[] LAYER_GREEN = {0.15f, 0.10f, 0.05f};

        public static final int BASE_R = 0xC8;
        public static final int BASE_G = 0xD4;
        public static final int BASE_B = 0x5A;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;

        Minecraft mc = Minecraft.getInstance();
        if (!isVenus(mc)) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) return;

        render(event, mc, camera);
    }

    private static void render(RenderLevelStageEvent event, Minecraft mc, Camera camera) {
        Vec3 cam = camera.getPosition();
        float time = currentTime(mc, event);

        Matrix4fStack stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        beginRainState();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int layer = 0; layer < Config.LAYER_SPEED.length; layer++) {
            emitLayer(buf, layer, cam, time);
        }

        BufferUploader.drawWithShader(buf.buildOrThrow());

        endRainState(stack);
    }

    private static void emitLayer(BufferBuilder buf, int layer, Vec3 cam, float time) {
        float camX = (float) cam.x;
        float camY = (float) cam.y;
        float camZ = (float) cam.z;

        float grid = Config.GRID * (1.0f + layer * 0.35f);
        float radius = Config.RADIUS * (1.0f + layer * 0.15f);

        int minGX = Mth.floor((camX - radius) / grid);
        int maxGX = Mth.floor((camX + radius) / grid);
        int minGZ = Mth.floor((camZ - radius) / grid);
        int maxGZ = Mth.floor((camZ + radius) / grid);

        float speed = Config.LAYER_SPEED[layer];
        float widthMul = Config.LAYER_WIDTH[layer];
        int alpha = (int) (Config.LAYER_ALPHA[layer] * 255f);
        int alphaFade = (int) (alpha * Config.FADE);
        float greenShift = Config.LAYER_GREEN[layer];

        for (int gx = minGX; gx <= maxGX; gx++) {
            for (int gz = minGZ; gz <= maxGZ; gz++) {
                emitCurtain(buf, layer, gx, gz,
                        camX, camY, camZ,
                        grid, radius,
                        speed, widthMul,
                        alpha, alphaFade,
                        greenShift,
                        time);
            }
        }
    }

    private static void emitCurtain(BufferBuilder buf, int layer,
                                    int gx, int gz,
                                    float camX, float camY, float camZ,
                                    float grid, float radius,
                                    float speed, float widthMul,
                                    int alphaTop, int alphaBottom,
                                    float greenShift,
                                    float time) {
        float h1 = hash01(gx, gz, layer * 4 + 1);
        float h2 = hash01(gx, gz, layer * 4 + 2);
        float h3 = hash01(gx, gz, layer * 4 + 3);
        float h4 = hash01(gx, gz, layer * 4 + 4);

        float px = gx * grid + (h1 - 0.5f) * grid * 0.9f;
        float pz = gz * grid + (h2 - 0.5f) * grid * 0.9f;

        float dx = px - camX;
        float dz = pz - camZ;
        float distSq = dx * dx + dz * dz;
        if (distSq > radius * radius) return;

        float curtainWidth = grid * 2.5f * widthMul * (0.6f + h3 * 0.8f);

        float curtainHeight = Config.RANGE * (0.5f + h4 * 0.5f);
        float fall = (time * speed * 20f + h3 * Config.RANGE) % Config.RANGE;
        float topY = camY + Config.RANGE * 0.5f - fall;
        float bottomY = topY - curtainHeight;

        float bottomX = px + Config.WIND_X * curtainHeight;
        float bottomZ = pz + Config.WIND_Z * curtainHeight;

        float dist = (float) Math.sqrt(distSq);
        if (dist < 1e-3f) return;
        float perpX = dz / dist;
        float perpZ = -dx / dist;
        float halfW = curtainWidth * 0.5f;

        float topHalfW = halfW;
        float bottomHalfW = halfW * 0.6f;

        float tlX = px - perpX * topHalfW, tlZ = pz - perpZ * topHalfW;
        float trX = px + perpX * topHalfW, trZ = pz + perpZ * topHalfW;
        float blX = bottomX - perpX * bottomHalfW, blZ = bottomZ - perpZ * bottomHalfW;
        float brX = bottomX + perpX * bottomHalfW, brZ = bottomZ + perpZ * bottomHalfW;

        int rTop = Config.BASE_R;
        int gTop = Config.BASE_G;
        int bTop = (int) (Config.BASE_B * (1f - greenShift));

        int rBot = (int) (Config.BASE_R * 0.6f);
        int gBot = (int) (Config.BASE_G * 0.8f);
        int bBot = (int) (Config.BASE_B * 0.4f);

        buf.addVertex(tlX, topY, tlZ).setColor(rTop, gTop, bTop, alphaTop);
        buf.addVertex(blX, bottomY, blZ).setColor(rBot, gBot, bBot, alphaBottom);
        buf.addVertex(brX, bottomY, brZ).setColor(rBot, gBot, bBot, alphaBottom);
        buf.addVertex(trX, topY, trZ).setColor(rTop, gTop, bTop, alphaTop);
    }

    private static void beginRainState() {
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }

    private static void endRainState(Matrix4fStack stack) {
        stack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static boolean isVenus(Minecraft mc) {
        return mc.level != null && mc.level.dimension().equals(ModDimensions.VENUS_LEVEL_KEY);
    }

    private static float currentTime(Minecraft mc, RenderLevelStageEvent event) {
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        return (float) mc.level.getGameTime() + partial;
    }

    private static float hash01(int x, int z, int salt) {
        long h = (long) x * 0x27d4eb2dL ^ (long) z * 0x165667b1L ^ salt * 0x9e3779b9L;
        h = (h ^ (h >>> 15)) * 0x45d9f3bL;
        h = (h ^ (h >>> 15)) * 0x45d9f3bL;
        h ^= h >>> 15;
        return (h & 0xFFFFFFL) / 16777216.0f;
    }
}