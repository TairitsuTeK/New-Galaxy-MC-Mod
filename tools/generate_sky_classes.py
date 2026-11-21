#!/usr/bin/env python3
"""Generate self-contained sky classes; no shared sky package is used."""

from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "src/main/java/com/tairitsutek/newgc"

BODIES = [
    dict(base="Planets", id="saturn", cls="SaturnSky", key="SATURN", au=9.537,
         nearby=[("titan", 0.404, 0.00817), ("jupiter", 11.210, 4.334)], own_effect=True),
    dict(base="Planets", id="uranus", cls="UranusSky", key="URANUS", au=19.190,
         nearby=[("titania", 0.121, 0.00291), ("oberon", 0.117, 0.00389)], own_effect=True),
    dict(base="Planets", id="neptune", cls="NeptuneSky", key="NEPTUNE", au=30.070,
         nearby=[("triton", 0.212, 0.00237)], own_effect=True),
    dict(base="Planets", id="pluto", cls="PlutoSky", key="PLUTO", au=39.480,
         nearby=[("charon", 0.095, 0.00013)], own_effect=True),
    dict(base="Planets", id="makemake", cls="MakemakeSky", key="MAKEMAKE", au=45.790,
         nearby=[("haumea", 0.078, 2.660)], own_effect=True),
    dict(base="Planets", id="haumea", cls="HaumeaSky", key="HAUMEA", au=43.130,
         nearby=[("hiiaka", 0.030, 0.00013), ("pluto", 0.187, 3.650)], own_effect=True),
    dict(base="Planets", id="eris", cls="ErisSky", key="ERIS", au=67.670,
         nearby=[], own_effect=True),
    dict(base="Moons", id="europa", cls="EuropaSky", key="EUROPA", au=5.203,
         nearby=[("jupiter", 11.210, 0.00449), ("io", 0.286, 0.00167),
                 ("ganymede", 0.413, 0.00267), ("callisto", 0.378, 0.00811)], own_effect=True),
    dict(base="Moons", id="ganymede", cls="GanymedeSky", key="GANYMEDE", au=5.203,
         nearby=[("jupiter", 11.210, 0.00716), ("io", 0.286, 0.00434),
                 ("europa", 0.245, 0.00267), ("callisto", 0.378, 0.00544)], own_effect=True),
    dict(base="Moons", id="callisto", cls="CallistoSky", key="CALLISTO", au=5.203,
         nearby=[("jupiter", 11.210, 0.01260), ("io", 0.286, 0.00978),
                 ("europa", 0.245, 0.00811), ("ganymede", 0.413, 0.00544)], own_effect=True),
    dict(base="Moons", id="titan", cls="TitanSky", key="TITAN", au=9.537,
         nearby=[("saturn", 9.450, 0.00817)], own_effect=True),
    dict(base="Moons", id="titania", cls="TitaniaSky", key="TITANIA", au=19.190,
         nearby=[("uranus", 4.010, 0.00291), ("oberon", 0.117, 0.00098)], own_effect=True),
    dict(base="Moons", id="oberon", cls="OberonSky", key="OBERON", au=19.190,
         nearby=[("uranus", 4.010, 0.00389), ("titania", 0.121, 0.00098)], own_effect=True),
    dict(base="Moons", id="triton", cls="TritonSky", key="TRITON", au=30.070,
         nearby=[("neptune", 3.880, 0.00237)], own_effect=True),
    dict(base="Moons", id="charon", cls="CharonSky", key="CHARON", au=39.480,
         nearby=[("pluto", 0.187, 0.00013)], own_effect=True),
    dict(base="Moons", id="hiiaka", cls="HiiakaSky", key="HIIAKA", au=43.130,
         nearby=[("haumea", 0.078, 0.00013)], own_effect=True),
    dict(base="Planets", id="jupiter", cls="JupiterSkyRenderer", key="JUPITER", au=5.203,
         nearby=[("io", 0.286, 0.00282), ("europa", 0.245, 0.00449),
                 ("ganymede", 0.413, 0.00716), ("callisto", 0.378, 0.01260)],
         own_effect=False),
]

TEMPLATE_1 = '''package __PACKAGE__;

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
public final class __CLASS__ {

    public static final ResourceLocation EFFECT =
            ResourceLocation.fromNamespaceAndPath("newgc", "__EFFECT__");

    private static final double ORBITAL_AU = __AU__;
    private static final int STAR_COUNT = 2200;
    private static final float SKY_DISTANCE = 100.0f;
    private static final ResourceLocation SUN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/sun.png");

    private record Nearby(ResourceLocation texture, double radius, double separation) {
    }

    private static final List<Nearby> NEARBY = List.of(
__NEARBY__
    );

    private __CLASS__() {
    }

    private static ResourceLocation icon(String id) {
        return ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/" + id + "_icon.png");
    }

    @SubscribeEvent
    public static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
__EFFECT_REGISTER__
    }

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.__KEY___LEVEL_KEY)) {
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

        renderStars("__ID__".hashCode());
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

__EFFECT_CLASS__
}
'''

EFFECT_CLASS = '''    private static final class Effects extends DimensionSpecialEffects {
        private Effects() {
            super(Float.NaN, false, SkyType.NONE, false, false);
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
'''

OWN_EFFECT_REGISTER = "        event.register(EFFECT, new Effects());"
JUPITER_EFFECT_REGISTER = "        event.register(EFFECT, new JupiterSkyEffects());"


def main():
    for body in BODIES:
        package = f"com.tairitsutek.newgc.{body['base']}.{body['id']}.client"
        path = ROOT / body["base"] / body["id"] / "client" / f"{body['cls']}.java"
        path.parent.mkdir(parents=True, exist_ok=True)

        nearby_lines = []
        for icon, radius, separation in body["nearby"]:
            nearby_lines.append(
                f'            new Nearby(icon("{icon}"), {radius:.5f}, {separation:.6f})'
            )
        nearby = ",\n".join(nearby_lines) if nearby_lines else "            // No nearby bodies."

        own = body["own_effect"]
        text = TEMPLATE_1
        text = text.replace("__PACKAGE__", package)
        text = text.replace("__CLASS__", body["cls"])
        text = text.replace("__EFFECT__", f"{body['id']}_sky")
        text = text.replace("__AU__", f"{body['au']:.3f}")
        text = text.replace("__ID__", body["id"])
        text = text.replace("__KEY__", body["key"])
        text = text.replace("__NEARBY__", nearby)
        text = text.replace("__EFFECT_REGISTER__",
                            OWN_EFFECT_REGISTER if own else JUPITER_EFFECT_REGISTER)
        text = text.replace("__EFFECT_CLASS__", EFFECT_CLASS if own else "")
        path.write_text(text, encoding="utf-8")
        print(path)


if __name__ == "__main__":
    main()