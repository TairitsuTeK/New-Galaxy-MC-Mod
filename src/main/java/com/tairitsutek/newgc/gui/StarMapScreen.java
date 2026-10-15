package com.tairitsutek.newgc.gui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.network.TeleportRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class StarMapScreen extends Screen {

    private static final RenderType STAR_MAP_LINES = RenderType.create(
            "newgc_star_map_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            262144,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false)
    );

    private static final int COLOR_BG_TOP    = 0xFF03050C;
    private static final int COLOR_BG_BOTTOM = 0xFF0B1422;

    private static final float[] C_ORBIT        = {0.32f, 0.55f, 0.82f, 0.72f};
    private static final float[] C_MOON_ORBIT   = {0.26f, 0.48f, 0.76f, 0.72f};
    private static final float[] C_ORBIT_SELECT = {0.38f, 0.72f, 1.00f, 0.95f};
    private static final float[] C_ORBIT_FOCUS  = {0.45f, 0.90f, 0.55f, 0.95f};
    private static final float[] C_ORBIT_GLOW   = {0.30f, 0.55f, 0.85f, 0.18f};
    private static final float[] C_ASTEROID     = {0.54f, 0.48f, 0.42f, 1.0f};
    private static final float[] C_ASTEROID_2   = {0.42f, 0.35f, 0.29f, 1.0f};
    private static final float[] C_KUIPER       = {0.42f, 0.54f, 0.60f, 1.0f};
    private static final float[] C_KUIPER_2     = {0.29f, 0.42f, 0.48f, 1.0f};
    private static final float[] C_OORT         = {0.35f, 0.48f, 0.60f, 1.0f};
    private static final float[] C_OORT_2       = {0.23f, 0.35f, 0.48f, 1.0f};
    private static final float[] C_HILLS        = {0.48f, 0.42f, 0.54f, 1.0f};
    private static final float[] C_HILLS_2      = {0.35f, 0.29f, 0.42f, 1.0f};

    private static final int COLOR_SELECT       = 0xFF66FF66;
    private static final int COLOR_MOON_SELECT  = 0xFF80C8FF;

    private static final int COLOR_LIST_BG_TOP     = 0xC0050A14;
    private static final int COLOR_LIST_BG_BOTTOM  = 0xC0081420;
    private static final int COLOR_LIST_BORDER     = 0xFF3AA0C8;
    private static final int COLOR_LIST_ACCENT     = 0xFF4AD8FF;
    private static final int COLOR_LIST_GLOW       = 0x403AA0C8;
    private static final int COLOR_LIST_HOVER      = 0x28FFFFFF;
    private static final int COLOR_LIST_HOVER_BAR  = 0x604AD8FF;
    private static final int COLOR_LIST_SELECT     = 0x4040FF80;
    private static final int COLOR_LIST_MOON_SELECT= 0x4080C8FF;
    private static final int COLOR_LIST_SCROLLBAR  = 0x60A0E0FF;
    private static final int COLOR_LIST_SCROLL_BG  = 0x30FFFFFF;

    private static final int COLOR_ROW_GREEN_TOP    = 0x4000E050;
    private static final int COLOR_ROW_GREEN_BOTTOM = 0x26007030;
    private static final int COLOR_ROW_RED_TOP      = 0x40E03030;
    private static final int COLOR_ROW_RED_BOTTOM   = 0x26802020;
    private static final int COLOR_ROW_TEXT         = 0xFFFFFFFF;

    private static final int COLOR_BTN_BG       = 0x88050812;
    private static final int COLOR_BTN_BORDER   = 0xCC2A7090;
    private static final int COLOR_BTN_BORDER_HOVER = 0xFF4AC8F0;
    private static final int COLOR_BTN_BORDER_ACTIVE = 0xFF40FF80;
    private static final int COLOR_BTN_TEXT     = 0xFFE0F8FF;
    private static final int COLOR_BTN_TEXT_OFF = 0xFF6A6A6A;

    private static final int LIST_X       = 6;
    private static final int LIST_Y       = 6;
    private static final int LIST_W       = 96;
    private static final int LIST_ROW_H   = 16;
    private static final int LIST_PAD     = 4;
    private static final int MOON_ROW_H   = 13;
    private static final int MOON_INDENT  = 10;
    private static final int LIST_TITLE_H = 14;
    private static final int LIST_SCROLLBAR_W = 3;

    private static final int BTN_W        = 80;
    private static final int BTN_H        = 22;
    private static final int BTN_Y        = 10;
    private static final int BTN_MARGIN_R = 12;

    private static final float LERP_SCALE = 0.012f;
    private static final float LERP_PAN   = 0.018f;
    private static final float LERP_MOON  = 0.10f;
    private static final float LERP_SCROLL= 0.25f;
    private static final float LERP_ROT   = 0.08f;

    private static final int PLANET_ICON_DISPLAY = 10;
    private static final int SUN_ICON_DISPLAY    = 14;

    private static final float MIN_SCALE = 0.15f;
    private static final float MAX_SCALE = 4.0f;
    private static final float FOCUS_MIN_SCALE = 0.4f;
    private static final float FOCUS_MAX_SCALE = 2.5f;

    private static final float MAX_TILT = 1.15f;
    private static final float PERSP = 1200f;

    private String toastMessage = null;
    private long toastShowTime = 0;
    private static final long TOAST_DURATION_MS = 2500;

    private int viewW, viewH;

    private float mapScale = 0.8f, targetScale = 0.8f;
    private float panX = 0f, targetPanX = 0f;
    private float panY = 0f, targetPanY = 0f;

    private float rotYaw = 0f, targetRotYaw = 0f;
    private float rotPitch = 0f, targetRotPitch = 0f;
    private float rotRoll = 0f, targetRotRoll = 0f;
    private float rotA = 1f, rotB = 0f, rotC = 0f, rotD = 1f, rotE = 0f, rotF = 0f;

    private final List<float[]> asteroidBelt = new ArrayList<>();
    private final List<float[]> kuiperBelt   = new ArrayList<>();
    private final List<float[]> oortCloud    = new ArrayList<>();
    private final List<float[]> hillsCloud   = new ArrayList<>();

    private final float[] tmpColor = new float[4];
    private final float[] tmpPos = new float[2];
    private final List<float[]> stars = new ArrayList<>();
    private final List<float[]> nebulas = new ArrayList<>();

    private long openedAt;

    private long lastListClickTime = 0;
    private Planet lastListClickPlanet = null;
    private static final long DOUBLE_CLICK_MS = 350;

    private long focusAnimStart = 0;

    private float moonExpand = 0f;
    private float targetMoonExpand = 0f;
    private Planet expandedPlanet = null;

    private float listScroll = 0f;
    private float targetListScroll = 0f;
    private int listContentHeight = 0;

    private static class Moon {
        final String nameKey;
        final int orbitA, orbitB;
        final float speed;
        final float phase;
        final int iconSize;
        final int displaySize;
        final ResourceLocation iconTexture;
        final ResourceKey<Level> dimension;
        final String cachedName;

        Moon(String nameKey, int orbitA, int orbitB, float speed, float phase,
             int iconSize, int displaySize, ResourceLocation iconTexture,
             ResourceKey<Level> dimension) {
            this.nameKey = nameKey;
            this.orbitA = orbitA;
            this.orbitB = orbitB;
            this.speed = speed;
            this.phase = phase;
            this.iconSize = iconSize;
            this.displaySize = displaySize;
            this.iconTexture = iconTexture;
            this.dimension = dimension;
            this.cachedName = Component.translatable(nameKey).getString();
        }

        String displayName() { return cachedName; }
        boolean isTeleportable() { return dimension != null; }

        float relX(long openedAt) {
            long elapsed = System.currentTimeMillis() - openedAt;
            float angle = (elapsed / 8000.0f) * speed * (float) (Math.PI * 2) + phase;
            return orbitA * (float) Math.cos(angle);
        }
        float relY(long openedAt) {
            long elapsed = System.currentTimeMillis() - openedAt;
            float angle = (elapsed / 8000.0f) * speed * (float) (Math.PI * 2) + phase;
            return orbitB * (float) Math.sin(angle);
        }
    }

    private static class Planet {
        final String nameKey;
        final String dimId;
        final int orbitA, orbitB;
        final float speed;
        final float phase;
        final int radiusPx;
        final ResourceKey<Level> dimension;
        final ResourceLocation iconTexture;
        final int iconSize;
        final int displaySize;
        final boolean isDwarf;
        final List<Moon> moons;
        final String cachedName;

        Planet(String nameKey, String dimId, int orbitA, int orbitB, float speed, float phase,
               int radiusPx, ResourceKey<Level> dimension,
               ResourceLocation iconTexture, int iconSize, int displaySize,
               boolean isDwarf, List<Moon> moons) {
            this.nameKey = nameKey;
            this.dimId = dimId;
            this.orbitA = orbitA;
            this.orbitB = orbitB;
            this.speed = speed;
            this.phase = phase;
            this.radiusPx = radiusPx;
            this.dimension = dimension;
            this.iconTexture = iconTexture;
            this.iconSize = iconSize;
            this.displaySize = displaySize;
            this.isDwarf = isDwarf;
            this.moons = moons;
            this.cachedName = Component.translatable(nameKey).getString();
        }

        String displayName() { return cachedName; }
        boolean isTeleportable() { return dimension != null; }

        float worldX(long openedAt) {
            long elapsed = System.currentTimeMillis() - openedAt;
            float angle = (elapsed / 8000.0f) * speed * (float) (Math.PI * 2) + phase;
            return orbitA * (float) Math.cos(angle);
        }
        float worldY(long openedAt) {
            long elapsed = System.currentTimeMillis() - openedAt;
            float angle = (elapsed / 8000.0f) * speed * (float) (Math.PI * 2) + phase;
            return orbitB * (float) Math.sin(angle);
        }
    }

    private final List<Planet> planets = new ArrayList<>();

    private static final ResourceLocation SUN_ICON =
            ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/sun_icon.png");
    private static final int SUN_ICON_SIZE = 16;

    private static class Selection {
        Planet planet;
        int moonIndex;
        boolean isMoon() { return moonIndex >= 0; }

        ResourceKey<Level> getDimension() {
            if (isMoon()) return planet.moons.get(moonIndex).dimension;
            return planet.dimension;
        }

        boolean isTeleportable() { return getDimension() != null; }

        String displayName() {
            if (isMoon()) return planet.moons.get(moonIndex).displayName();
            return planet.displayName();
        }
    }

    private enum Mode { MAP, FOCUS_PLANET, FOCUS_MOON }
    private Mode mode = Mode.MAP;

    private Selection selected = null;
    private Planet focusedPlanet = null;
    private int focusedMoonIndex = -1;

    private boolean dragging = false;
    private double dragStartX, dragStartY;
    private float dragStartTargetPanX, dragStartTargetPanY;

    private boolean teleportBtnPressed = false;
    private boolean teleporting = false;

    public StarMapScreen() {
        super(Component.translatable("gui.newgc.star_map"));
    }

    private int teleportBtnX() { return this.width - BTN_W - BTN_MARGIN_R; }
    private int teleportBtnY() { return BTN_Y; }

    private boolean isTeleportBtnActive() {
        if (toastMessage != null) return false;
        if (selected == null) return false;
        if (!selected.isTeleportable()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        return !mc.level.dimension().equals(selected.getDimension());
    }

    private Selection getTeleportTarget() {
        if (selected == null) return null;
        if (!selected.isTeleportable()) return null;
        return selected;
    }

    private static List<Moon> noMoons() { return List.of(); }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        viewW = this.width;
        viewH = this.height;

        planets.clear();

        planets.add(new Planet("gui.newgc.planet.mercury", "newgc:mercury",
                56, 31, 2.0f, 0f, 8,
                ModDimensions.MERCURY_LEVEL_KEY,
                tex("mercury_icon.png"), 16, PLANET_ICON_DISPLAY,
                false, noMoons()));
        planets.add(new Planet("gui.newgc.planet.venus", "newgc:venus",
                77, 42, 0.8f, (float) (Math.PI * 2 / 3), 10,
                ModDimensions.VENUS_LEVEL_KEY,
                tex("venus_icon.png"), 16, PLANET_ICON_DISPLAY,
                false, noMoons()));
        planets.add(new Planet("gui.newgc.planet.earth", "minecraft:overworld",
                90, 50, 0.5f, (float) (Math.PI * 4 / 3), 10,
                Level.OVERWORLD,
                tex("earth_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.moon", 18, 12, 2.5f, 0f,
                        16, 6, tex("moon_icon.png"),
                        ModDimensions.MOON_LEVEL_KEY))));

        planets.add(new Planet("gui.newgc.planet.mars", "newgc:mars",
                111, 61, 0.27f, (float) (Math.PI / 3), 9,
                ModDimensions.MARS_LEVEL_KEY,
                tex("mars_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.phobos", 15, 10, 3.5f, 0f,
                                16, 5, tex("phobos_icon.png"), ModDimensions.PHOBOS_LEVEL_KEY),
                        new Moon("gui.newgc.moon.deimos", 22, 14, 2.0f, (float) Math.PI,
                                16, 5, tex("deimos_icon.png"), ModDimensions.DEIMOS_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.ceres", "newgc:ceres",
                152, 84, 0.21f, (float) (Math.PI * 0.8), 6,
                ModDimensions.CERES_LEVEL_KEY,
                tex("ceres_icon.png"), 16, PLANET_ICON_DISPLAY,
                true, noMoons()));
        planets.add(new Planet("gui.newgc.planet.jupiter", "newgc:jupiter",
                205, 113, 0.084f, (float) (Math.PI * 0.5), 12,
                ModDimensions.JUPITER_LEVEL_KEY,
                tex("jupiter_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.io", 16, 11, 3.0f, 0f,
                                16, 6, tex("io_icon.png"), null),
                        new Moon("gui.newgc.moon.europa", 24, 16, 2.2f, (float) (Math.PI / 2),
                                16, 6, tex("europa_icon.png"), null),
                        new Moon("gui.newgc.moon.ganymede", 33, 22, 1.6f, (float) Math.PI,
                                16, 7, tex("ganymede_icon.png"), null),
                        new Moon("gui.newgc.moon.callisto", 44, 29, 1.1f, (float) (Math.PI * 1.5),
                                16, 7, tex("callisto_icon.png"), null))));
        planets.add(new Planet("gui.newgc.planet.saturn", "newgc:saturn",
                279, 153, 0.034f, (float) (Math.PI * 1.1), 11,
                null,
                tex("saturn_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.titan", 24, 16, 1.8f, 0f,
                        16, 7, tex("titan_icon.png"), null))));
        planets.add(new Planet("gui.newgc.planet.uranus", "newgc:uranus",
                394, 217, 0.012f, (float) (Math.PI * 1.6), 10,
                null,
                tex("uranus_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.titania", 18, 12, 2.4f, 0f,
                                16, 6, tex("titania_icon.png"), null),
                        new Moon("gui.newgc.moon.oberon", 27, 18, 1.7f, (float) Math.PI,
                                16, 6, tex("oberon_icon.png"), null))));
        planets.add(new Planet("gui.newgc.planet.neptune", "newgc:neptune",
                494, 272, 0.006f, (float) (Math.PI * 0.2), 10,
                null,
                tex("neptune_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.triton", 20, 13, 1.5f, 0f,
                        16, 6, tex("triton_icon.png"), null))));
        planets.add(new Planet("gui.newgc.planet.pluto", "newgc:pluto",
                560, 308, 0.004f, (float) (Math.PI * 1.4), 7,
                null,
                tex("pluto_icon.png"), 16, PLANET_ICON_DISPLAY,
                true,
                List.of(new Moon("gui.newgc.moon.charon", 14, 9, 2.0f, 0f,
                        16, 6, tex("charon_icon.png"), null))));
        planets.add(new Planet("gui.newgc.planet.makemake", "newgc:makemake",
                600, 330, 0.0032f, (float) (Math.PI * 0.7), 6,
                null,
                tex("makemake_icon.png"), 16, PLANET_ICON_DISPLAY,
                true, noMoons()));
        planets.add(new Planet("gui.newgc.planet.haumea", "newgc:haumea",
                580, 319, 0.0036f, (float) (Math.PI * 1.9), 6,
                null,
                tex("haumea_icon.png"), 16, PLANET_ICON_DISPLAY,
                true,
                List.of(new Moon("gui.newgc.moon.hiiaka", 16, 11, 2.6f, 0f,
                        16, 5, tex("hiiaka_icon.png"), null))));
        planets.add(new Planet("gui.newgc.planet.eris", "newgc:eris",
                660, 363, 0.0027f, (float) (Math.PI * 0.4), 6,
                null,
                tex("eris_icon.png"), 16, PLANET_ICON_DISPLAY,
                true, noMoons()));

        asteroidBelt.clear();
        RandomSource beltRng = RandomSource.create(99999L);
        for (int i = 0; i < 240; i++) {
            asteroidBelt.add(new float[]{
                    (float) (beltRng.nextDouble() * Math.PI * 2),
                    (float) (beltRng.nextDouble() * 40 - 20),
                    1 + beltRng.nextFloat() * 1.2f,
                    0.5f + beltRng.nextFloat() * 0.5f
            });
        }
        kuiperBelt.clear();
        RandomSource kuiperRng = RandomSource.create(11111L);
        for (int i = 0; i < 220; i++) {
            kuiperBelt.add(new float[]{
                    (float) (kuiperRng.nextDouble() * Math.PI * 2),
                    (float) (kuiperRng.nextDouble() * 70 - 35),
                    1 + kuiperRng.nextFloat() * 1.0f,
                    0.3f + kuiperRng.nextFloat() * 0.7f
            });
        }
        hillsCloud.clear();
        RandomSource hillsRng = RandomSource.create(22222L);
        for (int i = 0; i < 180; i++) {
            hillsCloud.add(new float[]{
                    (float) (hillsRng.nextDouble() * Math.PI * 2),
                    (float) (hillsRng.nextDouble() * 80 - 40),
                    1 + hillsRng.nextFloat(),
                    0.3f + hillsRng.nextFloat() * 0.5f
            });
        }
        oortCloud.clear();
        RandomSource oortRng = RandomSource.create(33333L);
        for (int i = 0; i < 150; i++) {
            oortCloud.add(new float[]{
                    (float) (oortRng.nextDouble() * Math.PI * 2),
                    (float) (oortRng.nextDouble() * 100 - 50),
                    1 + oortRng.nextFloat() * 0.8f,
                    0.2f + oortRng.nextFloat() * 0.4f
            });
        }

        stars.clear();
        RandomSource starRng = RandomSource.create(424242L);
        for (int i = 0; i < 200; i++) {
            stars.add(new float[]{
                    starRng.nextFloat(),
                    starRng.nextFloat(),
                    0.25f + starRng.nextFloat() * 0.6f,
                    1 + starRng.nextInt(2)
            });
        }

        nebulas.clear();
        RandomSource nebRng = RandomSource.create(77777L);
        for (int i = 0; i < 5; i++) {
            nebulas.add(new float[]{
                    nebRng.nextFloat(),
                    nebRng.nextFloat(),
                    40f + nebRng.nextFloat() * 60f,
                    0.05f + nebRng.nextFloat() * 0.05f,
                    i % 3
            });
        }

        mapScale = targetScale = 0.8f;
        panX = targetPanX = 0f;
        panY = targetPanY = 0f;

        rotYaw = targetRotYaw = 0f;
        rotPitch = targetRotPitch = 0f;
        rotRoll = targetRotRoll = 0f;
        updateRotationMatrix();

        moonExpand = 0f;
        targetMoonExpand = 0f;
        expandedPlanet = null;
        selected = null;

        listScroll = 0f;
        targetListScroll = 0f;
        listContentHeight = 0;

        teleporting = false;
        teleportBtnPressed = false;
    }

    private static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/" + name);
    }

    private void updateRotationMatrix() {
        float cosR = Mth.cos(rotRoll), sinR = Mth.sin(rotRoll);
        float cosP = Mth.cos(rotPitch), sinP = Mth.sin(rotPitch);
        float cosY = Mth.cos(rotYaw), sinY = Mth.sin(rotYaw);
        rotA = cosR * cosY + sinR * sinP * sinY;
        rotB = -sinR * cosY + cosR * sinP * sinY;
        rotC = sinR * cosP;
        rotD = cosR * cosP;
        rotE = -cosR * sinY + sinR * sinP * cosY;
        rotF = sinR * sinY + cosR * sinP * cosY;
    }

    private float project(float wx, float wy, float[] out) {
        float X = rotA * wx + rotB * wy;
        float Y = rotC * wx + rotD * wy;
        float Z = rotE * wx + rotF * wy;
        float denom = PERSP - Z;
        if (denom < 30f) denom = 30f;
        float w = PERSP / denom;
        out[0] = viewW / 2f + X * mapScale * w + panX;
        out[1] = viewH / 2f + Y * mapScale * w + panY;
        return w;
    }

    private void worldToScreenOffset(float wx, float wy, float[] out) {
        float X = rotA * wx + rotB * wy;
        float Y = rotC * wx + rotD * wy;
        float Z = rotE * wx + rotF * wy;
        float denom = PERSP - Z;
        if (denom < 30f) denom = 30f;
        float w = PERSP / denom;
        out[0] = X * w;
        out[1] = Y * w;
    }

    private float depthFade(float w) {
        return Mth.clamp(w, 0.35f, 1.0f);
    }

    private void showToast(String msg) {
        this.toastMessage = msg;
        this.toastShowTime = System.currentTimeMillis();
    }

    private int listViewH() {
        return this.height - LIST_Y - LIST_TITLE_H - LIST_PAD - 8;
    }

    private int computeContentHeight() {
        int h = LIST_PAD;
        for (Planet p : planets) {
            h += LIST_ROW_H;
            if (expandedPlanet == p && !p.moons.isEmpty()) {
                h += (int) (p.moons.size() * MOON_ROW_H * moonExpand);
            }
        }
        return h;
    }

    private void clampListScroll() {
        int view = listViewH();
        int max = Math.max(0, listContentHeight - view);
        targetListScroll = Mth.clamp(targetListScroll, 0, max);
        listScroll = Mth.clamp(listScroll, 0, max);
    }

    private void ensureSelectedVisible() {
        if (selected == null) return;
        Planet p = selected.planet;
        float wx = p.worldX(openedAt);
        float wy = p.worldY(openedAt);
        worldToScreenOffset(wx, wy, tmpPos);
        float offX = tmpPos[0];
        float offY = tmpPos[1];
        targetPanX = -offX * targetScale;
        targetPanY = -offY * targetScale;
        float sx = viewW / 2f + offX * mapScale + panX;
        float sy = viewH / 2f + offY * mapScale + panY;
        float margin = 60;
        if (sx < -margin || sx > this.width + margin || sy < -margin || sy > this.height + margin) {
            float halfW = (this.width / 2f) - margin;
            float halfH = (this.height / 2f) - margin;
            float needScale = Math.min(halfW / Math.max(1f, Math.abs(offX)),
                    halfH / Math.max(1f, Math.abs(offY)));
            float minS = isFocusMode() ? FOCUS_MIN_SCALE : MIN_SCALE;
            float maxS = isFocusMode() ? FOCUS_MAX_SCALE : MAX_SCALE;
            needScale = Mth.clamp(needScale, minS, maxS);
            if (needScale < targetScale) {
                targetScale = needScale;
                targetPanX = -offX * targetScale;
                targetPanY = -offY * targetScale;
            }
        }
    }

    private void focusWorld(float[] out) {
        if (focusedPlanet == null) {
            out[0] = 0f;
            out[1] = 0f;
            return;
        }
        float wx = focusedPlanet.worldX(openedAt);
        float wy = focusedPlanet.worldY(openedAt);
        if (mode == Mode.FOCUS_MOON && focusedMoonIndex >= 0
                && focusedMoonIndex < focusedPlanet.moons.size()) {
            Moon m = focusedPlanet.moons.get(focusedMoonIndex);
            wx += m.relX(openedAt);
            wy += m.relY(openedAt);
        }
        out[0] = wx;
        out[1] = wy;
    }

    private boolean isFocusMode() {
        return mode == Mode.FOCUS_PLANET || mode == Mode.FOCUS_MOON;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isFocusMode() && focusedPlanet != null) {
            focusWorld(tmpPos);
            float fwx = tmpPos[0], fwy = tmpPos[1];
            worldToScreenOffset(fwx, fwy, tmpPos);
            targetPanX = -tmpPos[0] * targetScale;
            targetPanY = -tmpPos[1] * targetScale;
        }

        float t = Math.min(1f, partialTick * 20f);
        float kScale = 1f - (float) Math.pow(1f - LERP_SCALE, t * 2.0);
        float kPan   = 1f - (float) Math.pow(1f - LERP_PAN,   t * 2.0);
        float kMoon  = 1f - (float) Math.pow(1f - LERP_MOON,  t * 2.0);
        float kScroll= 1f - (float) Math.pow(1f - LERP_SCROLL, t * 2.0);
        float kRot   = 1f - (float) Math.pow(1f - LERP_ROT,   t * 2.0);

        mapScale += (targetScale - mapScale) * kScale;
        panX     += (targetPanX - panX)     * kPan;
        panY     += (targetPanY - panY)     * kPan;
        moonExpand += (targetMoonExpand - moonExpand) * kMoon;
        listScroll += (targetListScroll - listScroll) * kScroll;

        rotYaw   += (targetRotYaw   - rotYaw)   * kRot;
        rotPitch += (targetRotPitch - rotPitch) * kRot;
        rotRoll  += (targetRotRoll  - rotRoll)  * kRot;
        updateRotationMatrix();

        drawBackground(graphics);

        renderLines(graphics);

        graphics.enableScissor(0, 0, this.width, this.height);
        renderSprites(graphics);
        graphics.disableScissor();

        graphics.fillGradient(0, 0, this.width, 48, 0x50000000, 0x00000000);
        graphics.fillGradient(0, this.height - 48, this.width, this.height, 0x00000000, 0x50000000);

        listContentHeight = computeContentHeight();
        clampListScroll();
        drawPlanetList(graphics, mouseX, mouseY);
        drawTeleportButton(graphics, mouseX, mouseY);

        graphics.drawCenteredString(this.font, this.title,
                this.width / 2, 12, 0xFFFFFF);

        String hint;
        if (isFocusMode()) {
            hint = Component.translatable("gui.newgc.star_map.hint.focus").getString();
        } else if (selected != null) {
            if (selected.isTeleportable()) {
                hint = Component.translatable("gui.newgc.star_map.hint.zoom",
                        selected.displayName()).getString();
            } else {
                hint = Component.translatable("gui.newgc.star_map.hint.locked",
                        selected.displayName()).getString();
            }
        } else {
            hint = Component.translatable("gui.newgc.star_map.hint.select").getString();
        }
        graphics.drawCenteredString(this.font, hint,
                this.width / 2, this.height - 20, 0xFF8888AA);

        graphics.drawCenteredString(this.font, "方向键/QE 旋转 · R 复位",
                this.width / 2, this.height - 34, 0xFF4A6B88);

        if (toastMessage != null) {
            long elapsed = System.currentTimeMillis() - toastShowTime;
            if (elapsed > TOAST_DURATION_MS) {
                toastMessage = null;
            } else {
                float alpha = 1.0f;
                long fadeStart = TOAST_DURATION_MS - 500;
                if (elapsed > fadeStart) alpha = 1.0f - (elapsed - fadeStart) / 500f;
                int a = (int) (alpha * 0xFF);
                int centerX = this.width / 2;
                int y = this.height - 60;
                int textW = this.font.width(toastMessage);
                int pad = 8;
                int bgColor = (0xC0000000 & 0x00FFFFFF) | (a << 24);
                int textColor = (0xFFFFEE88 & 0x00FFFFFF) | (a << 24);
                graphics.fill(centerX - textW / 2 - pad, y - 4,
                        centerX + textW / 2 + pad, y + 12, bgColor);
                graphics.drawCenteredString(this.font, toastMessage, centerX, y, textColor);
            }
        }
    }

    private void drawBackground(GuiGraphics graphics) {
        graphics.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);
    }

    private void renderLines(GuiGraphics graphics) {
        Matrix4f mat = graphics.pose().last().pose();

        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(STAR_MAP_LINES);
        long now = System.currentTimeMillis();

        renderNebulas(vc, mat);
        renderStars(vc, mat, now);
        renderSunGlow(vc, mat, now);

        int orbitSegments = mapScale < 0.15f ? 240
                : mapScale < 0.3f ? 360
                  : mapScale < 0.8f ? 480 : 640;
        for (Planet p : planets) {
            boolean focused = isFocusMode() && focusedPlanet == p;
            boolean selectedPlanet = !isFocusMode() && selected != null
                    && !selected.isMoon() && selected.planet == p;
            float[] color = focused ? C_ORBIT_FOCUS
                    : selectedPlanet ? C_ORBIT_SELECT : C_ORBIT;
            appendEllipse(vc, mat, p.orbitA, p.orbitB, C_ORBIT_GLOW, orbitSegments, 3.5f);
            appendEllipse(vc, mat, p.orbitA, p.orbitB, color, orbitSegments, 1.0f);
        }

        if (isFocusMode() && focusedPlanet != null && !focusedPlanet.moons.isEmpty()) {
            Planet p = focusedPlanet;
            float pwx = p.worldX(openedAt);
            float pwy = p.worldY(openedAt);
            for (Moon m : p.moons) {
                appendMoonEllipse(vc, mat, pwx, pwy, m.orbitA, m.orbitB, C_MOON_ORBIT);
            }
        }

        int beltStep = mapScale < 0.15f ? 3 : (mapScale < 0.3f ? 2 : 1);
        appendBelt(vc, mat, asteroidBelt, 130f, 90f, C_ASTEROID_2, C_ASTEROID, beltStep);
        appendBelt(vc, mat, kuiperBelt,   560f, 308f, C_KUIPER_2,  C_KUIPER, beltStep);
        appendBelt(vc, mat, hillsCloud,   720f, 396f, C_HILLS_2,   C_HILLS, beltStep);
        appendBelt(vc, mat, oortCloud,    860f, 473f, C_OORT_2,    C_OORT, beltStep);

        graphics.flush();
    }

    private void renderNebulas(VertexConsumer vc, Matrix4f mat) {
        for (float[] n : nebulas) {
            float cx = n[0] * this.width;
            float cy = n[1] * this.height;
            float r  = n[2];
            float a  = n[3];
            int hue = (int) n[4];

            float[] rs = {0f, r * 0.4f, r * 0.7f, r};
            float[] as = {a, a * 0.6f, a * 0.3f};
            for (int i = 0; i < 3; i++) {
                setNebulaColor(hue, as[i]);
                appendEllipseRing(vc, mat, cx, cy, rs[i], rs[i + 1], tmpColor, 48);
            }
        }
    }

    private void setNebulaColor(int hue, float alpha) {
        if (hue == 0)      { tmpColor[0]=0.50f; tmpColor[1]=0.35f; tmpColor[2]=0.75f; }
        else if (hue == 1) { tmpColor[0]=0.30f; tmpColor[1]=0.60f; tmpColor[2]=0.80f; }
        else               { tmpColor[0]=0.40f; tmpColor[1]=0.50f; tmpColor[2]=0.90f; }
        tmpColor[3] = alpha;
    }

    private void renderStars(VertexConsumer vc, Matrix4f mat, long now) {
        for (float[] s : stars) {
            float x = s[0] * this.width;
            float y = s[1] * this.height;
            float size = s[3];
            float twinkle = 0.55f + 0.45f * (float) Math.sin(now / 900.0 + s[0] * 40f + s[1] * 70f);
            float bright = s[2] * twinkle;
            int a = (int) (bright * 255f);
            if (a < 10) continue;
            int c = (int) (150f * bright + 40f);
            int cb = Math.min(255, c + 30);
            appendPoint4(vc, mat, x, y, size, c, c, cb, a);
        }
    }

    private void renderSunGlow(VertexConsumer vc, Matrix4f mat, long now) {
        project(0f, 0f, tmpPos);
        int cx = (int) tmpPos[0];
        int cy = (int) tmpPos[1];
        float pulse = 1f + 0.08f * (float) Math.sin(now / 1200.0);
        float baseR = (36f * mapScale + 8f) * pulse;
        int rings = 6;
        for (int i = 0; i < rings; i++) {
            float r0 = baseR * (i / (float) rings);
            float r1 = baseR * ((i + 1) / (float) rings);
            float alpha = (1f - (i + 1) / (float) rings) * 0.30f;
            tmpColor[0] = 1.0f;
            tmpColor[1] = 0.78f;
            tmpColor[2] = 0.45f;
            tmpColor[3] = alpha;
            appendEllipseRing(vc, mat, cx, cy, r0, r1, tmpColor, 80);
        }
    }

    private void renderSprites(GuiGraphics graphics) {
        long now = System.currentTimeMillis();

        project(0f, 0f, tmpPos);
        int sunX = (int) tmpPos[0];
        int sunY = (int) tmpPos[1];
        int sunSize = SUN_ICON_DISPLAY;
        graphics.blit(SUN_ICON,
                sunX - sunSize / 2, sunY - sunSize / 2,
                sunSize, sunSize, 0f, 0f,
                SUN_ICON_SIZE, SUN_ICON_SIZE, SUN_ICON_SIZE, SUN_ICON_SIZE);

        for (Planet p : planets) {
            float pwx = p.worldX(openedAt);
            float pwy = p.worldY(openedAt);
            float w = project(pwx, pwy, tmpPos);
            int px = (int) tmpPos[0];
            int py = (int) tmpPos[1];
            if (px < -32 || px > this.width + 32) continue;
            if (py < -32 || py > this.height + 32) continue;

            int size = (int) (p.displaySize * Mth.clamp(w, 0.75f, 1.5f));
            graphics.blit(p.iconTexture,
                    px - size / 2, py - size / 2, size, size, 0f, 0f,
                    p.iconSize, p.iconSize, p.iconSize, p.iconSize);
            int r = size / 2;

            if (isFocusMode() && focusedPlanet == p && !p.moons.isEmpty()) {
                for (int mi = 0; mi < p.moons.size(); mi++) {
                    Moon m = p.moons.get(mi);
                    float mwx = pwx + m.relX(openedAt);
                    float mwy = pwy + m.relY(openedAt);
                    float mw = project(mwx, mwy, tmpPos);
                    int mx = (int) tmpPos[0];
                    int my = (int) tmpPos[1];
                    int msize = (int) (Math.max(4, m.displaySize) * Mth.clamp(mw, 0.75f, 1.5f));
                    graphics.blit(m.iconTexture,
                            mx - msize / 2, my - msize / 2, msize, msize, 0f, 0f,
                            m.iconSize, m.iconSize, m.iconSize, m.iconSize);

                    boolean isSelectedMoon = selected != null && selected.isMoon()
                            && selected.planet == p && selected.moonIndex == mi;
                    if (isSelectedMoon) {
                        int pad = msize / 2 + 3;
                        drawSelectionFrame(graphics, mx - pad, my - pad, mx + pad, my + pad,
                                COLOR_MOON_SELECT);
                    }
                    if (mapScale > 1.2f) {
                        graphics.drawCenteredString(this.font, m.displayName(),
                                mx, my + msize / 2 + 2, 0xC0FFFFFF);
                    }
                }
            }

            if (selected != null && !selected.isMoon() && selected.planet == p && !isFocusMode()) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(now / 400.0);
                float scaleFactor = 1.0f + 0.15f * pulse;
                int pad = (int) ((r + 4) * scaleFactor);
                int alpha = 0x80 + (int) (0x7F * pulse);
                int color = (COLOR_SELECT & 0x00FFFFFF) | (alpha << 24);
                drawSelectionFrame(graphics, px - pad, py - pad, px + pad, py + pad, color);
            }
            if (mode == Mode.FOCUS_PLANET && focusedPlanet == p) {
                float appearT = Math.min(1f, (now - focusAnimStart) / 800f);
                float easedT = 1f - (float) Math.pow(1f - appearT, 3);
                float pulse = 0.5f + 0.5f * (float) Math.sin(now / 500.0);
                float scaleFactor = 1.0f + (1.0f - easedT) * 1.5f + 0.12f * pulse;
                int pad = (int) ((r + 6) * scaleFactor);
                int alpha = (int) (0x40 + 0xBF * easedT * (0.7f + 0.3f * pulse));
                int color = (COLOR_SELECT & 0x00FFFFFF) | (alpha << 24);
                drawSelectionFrame(graphics, px - pad, py - pad, px + pad, py + pad, color);
            }
        }

        if (mode == Mode.FOCUS_MOON && focusedPlanet != null
                && focusedMoonIndex >= 0 && focusedMoonIndex < focusedPlanet.moons.size()) {
            Planet p = focusedPlanet;
            Moon m = p.moons.get(focusedMoonIndex);
            float pwx = p.worldX(openedAt);
            float pwy = p.worldY(openedAt);
            float mwx = pwx + m.relX(openedAt);
            float mwy = pwy + m.relY(openedAt);
            float mw = project(mwx, mwy, tmpPos);
            int mx = (int) tmpPos[0];
            int my = (int) tmpPos[1];
            int msize = (int) (Math.max(4, m.displaySize) * Mth.clamp(mw, 0.75f, 1.5f));

            float appearT = Math.min(1f, (now - focusAnimStart) / 800f);
            float easedT = 1f - (float) Math.pow(1f - appearT, 3);
            float pulse = 0.5f + 0.5f * (float) Math.sin(now / 500.0);
            float scaleFactor = 1.0f + (1.0f - easedT) * 1.2f + 0.15f * pulse;
            int pad = (int) ((msize / 2f + 5) * scaleFactor);
            int alpha = (int) (0x60 + 0x9F * easedT * (0.7f + 0.3f * pulse));
            int color = (COLOR_MOON_SELECT & 0x00FFFFFF) | (alpha << 24);
            drawSelectionFrame(graphics, mx - pad, my - pad, mx + pad, my + pad, color);
        }
    }

    private void appendPoint4(VertexConsumer vc, Matrix4f mat, float x, float y,
                              float size, int r, int g, int b, int a) {
        float x1 = x + size;
        float y1 = y + size;
        vc.addVertex(mat, x,  y,  0f).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y,  0f).setColor(r, g, b, a);
        vc.addVertex(mat, x1, y1, 0f).setColor(r, g, b, a);
        vc.addVertex(mat, x,  y1, 0f).setColor(r, g, b, a);
    }

    private void appendSegment(VertexConsumer vc, Matrix4f mat,
                               float x1, float y1, float x2, float y2,
                               float thickness, float[] c) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return;
        float hw = thickness * 0.5f;
        float nx = -dy / len * hw;
        float ny =  dx / len * hw;
        int r = (int) (c[0] * 255f);
        int g = (int) (c[1] * 255f);
        int b = (int) (c[2] * 255f);
        int a = (int) (c[3] * 255f);
        vc.addVertex(mat, x1 + nx, y1 + ny, 0f).setColor(r, g, b, a);
        vc.addVertex(mat, x2 + nx, y2 + ny, 0f).setColor(r, g, b, a);
        vc.addVertex(mat, x2 - nx, y2 - ny, 0f).setColor(r, g, b, a);
        vc.addVertex(mat, x1 - nx, y1 - ny, 0f).setColor(r, g, b, a);
    }

    private void appendEllipse(VertexConsumer vc, Matrix4f mat, int a, int b,
                               float[] c, int segments, float thicknessMul) {
        if (a <= 0 || b <= 0) return;

        project(0f, 0f, tmpPos);
        float cx = tmpPos[0];
        float cy = tmpPos[1];
        float rMax = Math.max(a, b) * mapScale * 3f;
        if (cx + rMax < -10 || cx - rMax > this.width + 10) return;
        if (cy + rMax < -10 || cy - rMax > this.height + 10) return;

        float thickness = Math.max(1.0f, Math.min(2.2f, 1.4f * mapScale + 0.4f)) * thicknessMul;
        float prevX = 0f, prevY = 0f;
        boolean first = true;
        for (int i = 0; i <= segments; i++) {
            double t = (i / (double) segments) * Math.PI * 2;
            float wx = (float) (a * Math.cos(t));
            float wy = (float) (b * Math.sin(t));
            float w = project(wx, wy, tmpPos);
            float sx = tmpPos[0];
            float sy = tmpPos[1];
            if (!first) {
                float fade = depthFade(w);
                tmpColor[0] = c[0] * fade;
                tmpColor[1] = c[1] * fade;
                tmpColor[2] = c[2] * fade;
                tmpColor[3] = c[3];
                appendSegment(vc, mat, prevX, prevY, sx, sy, thickness, tmpColor);
            }
            prevX = sx;
            prevY = sy;
            first = false;
        }
    }

    private void appendMoonEllipse(VertexConsumer vc, Matrix4f mat,
                                   float pwx, float pwy, int a, int b, float[] c) {
        if (a <= 0 || b <= 0) return;
        int segments = Math.max(48, Math.min(120, (int) (Math.max(a, b) * 1.2f)));
        float thickness = Math.max(1.0f, Math.min(1.8f, 1.2f * mapScale + 0.4f));

        float prevX = 0f, prevY = 0f;
        boolean first = true;
        for (int i = 0; i <= segments; i++) {
            double t = (i / (double) segments) * Math.PI * 2;
            float wx = pwx + (float) (a * Math.cos(t));
            float wy = pwy + (float) (b * Math.sin(t));
            float w = project(wx, wy, tmpPos);
            float sx = tmpPos[0];
            float sy = tmpPos[1];
            if (!first) {
                float fade = depthFade(w);
                tmpColor[0] = c[0] * fade;
                tmpColor[1] = c[1] * fade;
                tmpColor[2] = c[2] * fade;
                tmpColor[3] = c[3];
                appendSegment(vc, mat, prevX, prevY, sx, sy, thickness, tmpColor);
            }
            prevX = sx;
            prevY = sy;
            first = false;
        }
    }

    private void appendEllipseRing(VertexConsumer vc, Matrix4f mat,
                                   float cx, float cy, float r0, float r1, float[] c, int segments) {
        int r = (int) (c[0] * 255f);
        int g = (int) (c[1] * 255f);
        int b = (int) (c[2] * 255f);
        int a = (int) (c[3] * 255f);
        for (int i = 0; i < segments; i++) {
            double t0 = (i / (double) segments) * Math.PI * 2;
            double t1 = ((i + 1) / (double) segments) * Math.PI * 2;
            float x0 = cx + r0 * (float) Math.cos(t0);
            float y0 = cy + r0 * (float) Math.sin(t0);
            float x1 = cx + r1 * (float) Math.cos(t0);
            float y1 = cy + r1 * (float) Math.sin(t0);
            float x2 = cx + r1 * (float) Math.cos(t1);
            float y2 = cy + r1 * (float) Math.sin(t1);
            float x3 = cx + r0 * (float) Math.cos(t1);
            float y3 = cy + r0 * (float) Math.sin(t1);
            vc.addVertex(mat, x0, y0, 0f).setColor(r, g, b, a);
            vc.addVertex(mat, x1, y1, 0f).setColor(r, g, b, a);
            vc.addVertex(mat, x2, y2, 0f).setColor(r, g, b, a);
            vc.addVertex(mat, x3, y3, 0f).setColor(r, g, b, a);
        }
    }

    private void appendBelt(VertexConsumer vc, Matrix4f mat, List<float[]> belt,
                            float baseA, float baseB,
                            float[] colorDim, float[] colorBright, int step) {
        int i = 0;
        for (float[] a : belt) {
            if (step > 1) {
                i++;
                if (i % step != 0) continue;
            }
            float angle = a[0];
            float offset = a[1];
            float bright = a[3];
            float rA = baseA + offset;
            float rB = baseB + offset * 0.55f;
            float wx = rA * (float) Math.cos(angle);
            float wy = rB * (float) Math.sin(angle);
            float w = project(wx, wy, tmpPos);
            float sx = tmpPos[0];
            float sy = tmpPos[1];
            if (sx < 0 || sx >= this.width) continue;
            if (sy < 0 || sy >= this.height) continue;

            float fade = depthFade(w);
            tmpColor[0] = (colorDim[0] + (colorBright[0] - colorDim[0]) * bright) * fade;
            tmpColor[1] = (colorDim[1] + (colorBright[1] - colorDim[1]) * bright) * fade;
            tmpColor[2] = (colorDim[2] + (colorBright[2] - colorDim[2]) * bright) * fade;
            int r = (int) (tmpColor[0] * 255f);
            int g = (int) (tmpColor[1] * 255f);
            int b = (int) (tmpColor[2] * 255f);
            int alpha = (int) (255f * Mth.clamp(fade, 0.35f, 1f));
            int size = a[2] >= 1.8f ? 2 : 1;
            appendPoint4(vc, mat, sx, sy, size, r, g, b, alpha);
        }
    }

    private void drawTeleportButton(GuiGraphics g, int mouseX, int mouseY) {
        int bx = teleportBtnX();
        int by = teleportBtnY();
        int bw = BTN_W;
        int bh = BTN_H;
        boolean active = isTeleportBtnActive();
        boolean hover = active && mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;

        g.fill(bx, by, bx + bw, by + bh, COLOR_BTN_BG);
        int borderColor;
        if (!active) borderColor = COLOR_BTN_BORDER & 0x60FFFFFF;
        else if (teleportBtnPressed) borderColor = 0xFFFFFFFF;
        else if (hover) borderColor = COLOR_BTN_BORDER_HOVER;
        else borderColor = COLOR_BTN_BORDER_ACTIVE;
        drawRect2(g, bx, by, bx + bw, by + bh, borderColor);

        if (teleportBtnPressed && active) {
            drawRect2(g, bx + 2, by + 2, bx + bw - 2, by + bh - 2, 0x60FFFFFF);
        }

        String label = Component.translatable("gui.newgc.star_map.teleport").getString();
        int textColor = active ? COLOR_BTN_TEXT : COLOR_BTN_TEXT_OFF;
        int textY = by + (bh - 8) / 2;
        g.drawCenteredString(this.font, label, bx + bw / 2, textY, textColor);
    }

    private void drawPlanetList(GuiGraphics g, int mouseX, int mouseY) {
        int viewH = listViewH();
        int totalH = LIST_TITLE_H + viewH + LIST_PAD;
        int x0 = LIST_X, y0 = LIST_Y;
        int x1 = LIST_X + LIST_W, y1 = LIST_Y + totalH;

        g.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, COLOR_LIST_GLOW);

        g.fillGradient(x0, y0, x1, y1, COLOR_LIST_BG_TOP, COLOR_LIST_BG_BOTTOM);

        drawRect2(g, x0, y0, x1, y1, COLOR_LIST_BORDER);
        drawCornerBrackets(g, x0, y0, x1, y1, COLOR_LIST_ACCENT);

        g.fillGradient(x0 + 1, y0 + 1, x1 - 1, y0 + LIST_TITLE_H, 0xFF123048, 0xFF071520);
        g.fill(x0 + 1, y0 + LIST_TITLE_H - 1, x1 - 1, y0 + LIST_TITLE_H, 0x804AD8FF);
        g.drawString(this.font,
                "◆ " + Component.translatable("gui.newgc.star_map.list_title").getString(),
                x0 + 5, y0 + 3, 0xFFA0F0FF, false);

        int contentTop = y0 + LIST_TITLE_H;
        int contentBottom = y0 + LIST_TITLE_H + viewH;
        g.enableScissor(x0, contentTop, x1, contentBottom);

        int rowX = x0 + 3;
        int rowW = LIST_W - 6 - (listContentHeight > viewH ? LIST_SCROLLBAR_W + 2 : 0);
        int curY = contentTop + LIST_PAD - (int) listScroll;

        for (int i = 0; i < planets.size(); i++) {
            Planet p = planets.get(i);
            if (curY + LIST_ROW_H >= contentTop - LIST_ROW_H && curY <= contentBottom) {
                drawListPlanetRow(g, p, rowX, curY, rowW, mouseX, mouseY, contentTop, contentBottom);
            }
            curY += LIST_ROW_H;
            if (expandedPlanet == p && !p.moons.isEmpty() && moonExpand > 0.01f) {
                int visibleMoons = (int) Math.ceil(p.moons.size() * moonExpand);
                for (int mi = 0; mi < visibleMoons; mi++) {
                    if (mi >= p.moons.size()) break;
                    if (curY + MOON_ROW_H >= contentTop - MOON_ROW_H && curY <= contentBottom) {
                        drawListMoonRow(g, p, mi, rowX, curY, rowW, mouseX, mouseY, contentTop, contentBottom);
                    }
                    curY += MOON_ROW_H;
                }
            }
        }

        g.disableScissor();

        if (listContentHeight > viewH) {
            int scrollBarX = x1 - LIST_SCROLLBAR_W - 2;
            int scrollBarY = contentTop;
            int scrollBarH = viewH;
            g.fill(scrollBarX, scrollBarY, scrollBarX + LIST_SCROLLBAR_W, scrollBarY + scrollBarH, COLOR_LIST_SCROLL_BG);
            float ratio = (float) viewH / listContentHeight;
            int thumbH = Math.max(20, (int) (scrollBarH * ratio));
            int maxScroll = listContentHeight - viewH;
            float scrollRatio = maxScroll > 0 ? listScroll / maxScroll : 0f;
            int thumbY = scrollBarY + (int) ((scrollBarH - thumbH) * scrollRatio);
            g.fill(scrollBarX, thumbY, scrollBarX + LIST_SCROLLBAR_W, thumbY + thumbH, COLOR_LIST_ACCENT);
            g.fill(scrollBarX, thumbY, scrollBarX + LIST_SCROLLBAR_W, thumbY + 1, 0xFFFFFFFF);
        }
    }

    private void drawListPlanetRow(GuiGraphics g, Planet p, int rowX, int rowY, int rowW,
                                   int mouseX, int mouseY, int clipTop, int clipBottom) {
        boolean hover = mouseX >= rowX && mouseX < rowX + rowW
                && mouseY >= rowY && mouseY < rowY + LIST_ROW_H
                && mouseY >= clipTop && mouseY < clipBottom;
        boolean isSelected = selected != null && !selected.isMoon() && selected.planet == p;

        if (p.isTeleportable()) {
            g.fillGradient(rowX, rowY, rowX + rowW, rowY + LIST_ROW_H, COLOR_ROW_GREEN_TOP, COLOR_ROW_GREEN_BOTTOM);
        } else {
            g.fillGradient(rowX, rowY, rowX + rowW, rowY + LIST_ROW_H, COLOR_ROW_RED_TOP, COLOR_ROW_RED_BOTTOM);
        }

        if (hover) {
            g.fill(rowX, rowY, rowX + rowW, rowY + LIST_ROW_H, COLOR_LIST_HOVER);
            g.fill(rowX, rowY, rowX + 2, rowY + LIST_ROW_H, COLOR_LIST_HOVER_BAR);
        }
        if (isSelected) {
            g.fill(rowX, rowY, rowX + 2, rowY + LIST_ROW_H, COLOR_SELECT);
        }

        g.drawString(this.font, p.displayName(), rowX + 7, rowY + 4, COLOR_ROW_TEXT, false);
        if (expandedPlanet == p && !p.moons.isEmpty()) {
            g.drawString(this.font, "▾", rowX + rowW - 10, rowY + 4, 0xFFFFFFFF, false);
        } else if (!p.moons.isEmpty()) {
            g.drawString(this.font, "▸", rowX + rowW - 10, rowY + 4, 0xFFD0F0FF, false);
        }
    }

    private void drawListMoonRow(GuiGraphics g, Planet parent, int moonIndex,
                                 int rowX, int rowY, int rowW,
                                 int mouseX, int mouseY, int clipTop, int clipBottom) {
        Moon m = parent.moons.get(moonIndex);
        int moonX = rowX + MOON_INDENT;
        boolean hover = mouseX >= moonX && mouseX < rowX + rowW
                && mouseY >= rowY && mouseY < rowY + MOON_ROW_H
                && mouseY >= clipTop && mouseY < clipBottom;
        boolean isSelected = selected != null && selected.isMoon()
                && selected.planet == parent && selected.moonIndex == moonIndex;

        if (m.isTeleportable()) {
            g.fillGradient(moonX, rowY, rowX + rowW, rowY + MOON_ROW_H, COLOR_ROW_GREEN_TOP, COLOR_ROW_GREEN_BOTTOM);
        } else {
            g.fillGradient(moonX, rowY, rowX + rowW, rowY + MOON_ROW_H, COLOR_ROW_RED_TOP, COLOR_ROW_RED_BOTTOM);
        }

        if (hover) {
            g.fill(moonX, rowY, rowX + rowW, rowY + MOON_ROW_H, COLOR_LIST_HOVER);
            g.fill(moonX, rowY, moonX + 2, rowY + MOON_ROW_H, COLOR_LIST_HOVER_BAR);
        }
        if (isSelected) {
            g.fill(moonX, rowY, moonX + 2, rowY + MOON_ROW_H, COLOR_MOON_SELECT);
        }

        g.drawString(this.font, "· " + m.displayName(), moonX + 5, rowY + 2, COLOR_ROW_TEXT, false);
    }

    private void drawRect2(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 2, color);
        g.fill(x1, y2 - 2, x2, y2, color);
        g.fill(x1, y1, x1 + 2, y2, color);
        g.fill(x2 - 2, y1, x2, y2, color);
    }

    private void drawCornerBrackets(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int s = 6, t = 2;
        g.fill(x1, y1, x1 + s, y1 + t, color);
        g.fill(x1, y1, x1 + t, y1 + s, color);
        g.fill(x2 - s, y1, x2, y1 + t, color);
        g.fill(x2 - t, y1, x2, y1 + s, color);
        g.fill(x1, y2 - t, x1 + s, y2, color);
        g.fill(x1, y2 - s, x1 + t, y2, color);
        g.fill(x2 - s, y2 - t, x2, y2, color);
        g.fill(x2 - t, y2 - s, x2, y2, color);
    }

    private void drawSelectionFrame(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int len = 6;
        int t = 2;
        g.fill(x1, y1, x1 + len, y1 + t, color);
        g.fill(x1, y1, x1 + t, y1 + len, color);
        g.fill(x2 - len, y1, x2, y1 + t, color);
        g.fill(x2 - t, y1, x2, y1 + len, color);
        g.fill(x1, y2 - t, x1 + len, y2, color);
        g.fill(x1, y2 - len, x1 + t, y2, color);
        g.fill(x2 - len, y2 - t, x2, y2, color);
        g.fill(x2 - t, y2 - len, x2, y2, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        float step = 0.12f;
        boolean handled = false;
        if (keyCode == GLFW.GLFW_KEY_R) {
            targetRotYaw = 0f;
            targetRotPitch = 0f;
            targetRotRoll = 0f;
            handled = true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT)  { targetRotYaw   += step; handled = true; }
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) { targetRotYaw   -= step; handled = true; }
        else if (keyCode == GLFW.GLFW_KEY_UP)    { targetRotPitch += step; handled = true; }
        else if (keyCode == GLFW.GLFW_KEY_DOWN)  { targetRotPitch -= step; handled = true; }
        else if (keyCode == GLFW.GLFW_KEY_Q)     { targetRotRoll  -= step; handled = true; }
        else if (keyCode == GLFW.GLFW_KEY_E)     { targetRotRoll  += step; handled = true; }

        if (handled) {
            clampRotations();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clampRotations() {
        targetRotYaw   = Mth.clamp(targetRotYaw, -MAX_TILT, MAX_TILT);
        targetRotPitch = Mth.clamp(targetRotPitch, -MAX_TILT, MAX_TILT);
        targetRotRoll  = Mth.clamp(targetRotRoll, -(float) Math.PI, (float) Math.PI);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (teleporting) return false;

        if (button == 1 && isFocusMode()) {
            exitFocusAndCollapse();
            return true;
        }

        if (button == 0) {
            int bx = teleportBtnX();
            int by = teleportBtnY();
            if (mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H) {
                if (isTeleportBtnActive() && !teleporting) {
                    teleportBtnPressed = true;
                    Selection t = getTeleportTarget();
                    if (t != null) teleportTo(t);
                }
                return true;
            }

            if (handleListHit(mouseX, mouseY)) return true;

            MoonHit moonHit = hitMoon(mouseX, mouseY);
            if (moonHit != null) {
                selected = new Selection();
                selected.planet = moonHit.planet;
                selected.moonIndex = moonHit.moonIndex;
                if (isFocusMode()) {
                    focusMoon(moonHit.planet, moonHit.moonIndex);
                }
                ensureSelectedVisible();
                return true;
            }

            Planet hit = null;
            double hitR = Math.max(14, 12 * mapScale);
            double bestDist = hitR * hitR;
            for (Planet p : planets) {
                float pwx = p.worldX(openedAt);
                float pwy = p.worldY(openedAt);
                project(pwx, pwy, tmpPos);
                float px = tmpPos[0];
                float py = tmpPos[1];
                double dx = mouseX - px;
                double dy = mouseY - py;
                double d2 = dx * dx + dy * dy;
                if (d2 <= bestDist) { bestDist = d2; hit = p; }
            }
            if (hit != null) {
                if (isFocusMode()) {
                    if (focusedPlanet != hit) {
                        enterFocus(hit);
                    } else if (mode == Mode.FOCUS_MOON) {
                        focusPlanet(hit);
                    }
                    return true;
                }
                if (selected == null || selected.isMoon() || selected.planet != hit) {
                    setSelectedPlanet(hit);
                } else {
                    enterFocus(hit);
                }
                return true;
            }

            if (isFocusMode()) {
                exitFocusAndCollapse();
                return true;
            }
            if (selected != null) { setSelectedPlanet(null); return true; }

            dragging = true;
            dragStartX = mouseX;
            dragStartY = mouseY;
            dragStartTargetPanX = targetPanX;
            dragStartTargetPanY = targetPanY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static class MoonHit {
        Planet planet;
        int moonIndex;
    }

    private MoonHit hitMoon(double mouseX, double mouseY) {
        if (!isFocusMode() || focusedPlanet == null) return null;
        Planet p = focusedPlanet;
        if (p.moons.isEmpty()) return null;
        float pwx = p.worldX(openedAt);
        float pwy = p.worldY(openedAt);

        MoonHit best = null;
        double bestDist = Double.MAX_VALUE;

        for (int mi = 0; mi < p.moons.size(); mi++) {
            Moon m = p.moons.get(mi);
            float mwx = pwx + m.relX(openedAt);
            float mwy = pwy + m.relY(openedAt);
            project(mwx, mwy, tmpPos);
            int mx = (int) tmpPos[0];
            int my = (int) tmpPos[1];
            double dx = mouseX - mx;
            double dy = mouseY - my;
            double d2 = dx * dx + dy * dy;
            double hitR = Math.max(12, m.displaySize * mapScale);
            if (d2 <= hitR * hitR && d2 < bestDist) {
                bestDist = d2;
                MoonHit res = new MoonHit();
                res.planet = p;
                res.moonIndex = mi;
                best = res;
            }
        }
        return best;
    }

    private boolean handleListHit(double mouseX, double mouseY) {
        int viewH = listViewH();
        int contentTop = LIST_Y + LIST_TITLE_H;
        int contentBottom = contentTop + viewH;
        if (mouseX < LIST_X || mouseX >= LIST_X + LIST_W) return false;
        if (mouseY < LIST_Y || mouseY >= contentBottom) return false;
        if (mouseY < contentTop) return true;
        int rowX = LIST_X + 3;
        int rowW = LIST_W - 6 - (listContentHeight > viewH ? LIST_SCROLLBAR_W + 2 : 0);
        int curY = contentTop + LIST_PAD - (int) listScroll;
        for (int i = 0; i < planets.size(); i++) {
            Planet p = planets.get(i);
            if (mouseY >= curY && mouseY < curY + LIST_ROW_H
                    && mouseX >= rowX && mouseX < rowX + rowW) {
                handleListClick(p);
                return true;
            }
            curY += LIST_ROW_H;
            if (expandedPlanet == p && !p.moons.isEmpty() && moonExpand > 0.01f) {
                int visibleMoons = (int) Math.ceil(p.moons.size() * moonExpand);
                for (int mi = 0; mi < visibleMoons; mi++) {
                    if (mi >= p.moons.size()) break;
                    int moonX = rowX + MOON_INDENT;
                    if (mouseY >= curY && mouseY < curY + MOON_ROW_H
                            && mouseX >= moonX && mouseX < rowX + rowW) {
                        handleMoonClick(p, mi);
                        return true;
                    }
                    curY += MOON_ROW_H;
                }
            }
        }
        return true;
    }

    private void handleMoonClick(Planet parent, int moonIndex) {
        selected = new Selection();
        selected.planet = parent;
        selected.moonIndex = moonIndex;
        expandedPlanet = parent;
        targetMoonExpand = 1f;

        if (isFocusMode() && focusedPlanet == parent) {
            focusMoon(parent, moonIndex);
        } else {
            enterFocus(parent);
            focusMoon(parent, moonIndex);
        }
        ensureSelectedVisible();
    }

    private void setSelectedPlanet(Planet p) {
        if (p == null) { selected = null; return; }
        selected = new Selection();
        selected.planet = p;
        selected.moonIndex = -1;
        ensureSelectedVisible();
    }

    private void handleListClick(Planet p) {
        long now = System.currentTimeMillis();
        boolean isDouble = (lastListClickPlanet == p) && (now - lastListClickTime <= DOUBLE_CLICK_MS);

        if (isDouble) {
            if (selected == null || selected.isMoon() || selected.planet != p) {
                setSelectedPlanet(p);
            }
            if (!p.moons.isEmpty()) {
                expandedPlanet = p;
                targetMoonExpand = 1f;
            }
            enterFocus(p);
            lastListClickPlanet = null;
            lastListClickTime = 0;
        } else {
            if (isFocusMode()) {
                exitFocusAndCollapse();
            }
            setSelectedPlanet(p);
            lastListClickPlanet = p;
            lastListClickTime = now;
        }
        ensureSelectedVisible();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) teleportBtnPressed = false;
        if (button == 0 && dragging) { dragging = false; return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && dragging) {
            targetPanX = dragStartTargetPanX + (float) (mouseX - dragStartX);
            targetPanY = dragStartTargetPanY + (float) (mouseY - dragStartY);
            clampTargetPan();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        int viewH = listViewH();
        int contentTop = LIST_Y + LIST_TITLE_H;
        int contentBottom = contentTop + viewH;
        if (mouseX >= LIST_X && mouseX < LIST_X + LIST_W && mouseY >= LIST_Y && mouseY < contentBottom) {
            int max = Math.max(0, listContentHeight - viewH);
            if (max > 0) targetListScroll = Mth.clamp(targetListScroll - (float) deltaY * 20f, 0, max);
            return true;
        }
        float minS = isFocusMode() ? FOCUS_MIN_SCALE : MIN_SCALE;
        float maxS = isFocusMode() ? FOCUS_MAX_SCALE : MAX_SCALE;
        float factor = deltaY > 0 ? 1.1f : 1f / 1.1f;
        float newScale = Mth.clamp(targetScale * factor, minS, maxS);

        float ux = (float) ((mouseX - viewW / 2f - targetPanX) / targetScale);
        float uy = (float) ((mouseY - viewH / 2f - targetPanY) / targetScale);
        float m00 = PERSP * rotA + ux * rotE;
        float m01 = PERSP * rotB + ux * rotF;
        float m10 = PERSP * rotC + uy * rotE;
        float m11 = PERSP * rotD + uy * rotF;
        float rhs0 = ux * PERSP;
        float rhs1 = uy * PERSP;
        float det = m00 * m11 - m01 * m10;
        float anchorX = 0f, anchorY = 0f;
        if (Math.abs(det) > 0.001f) {
            anchorX = (rhs0 * m11 - m01 * rhs1) / det;
            anchorY = (m00 * rhs1 - rhs0 * m10) / det;
        }
        targetScale = newScale;
        float AX = rotA * anchorX + rotB * anchorY;
        float AY = rotC * anchorX + rotD * anchorY;
        float AZ = rotE * anchorX + rotF * anchorY;
        float denom = PERSP - AZ;
        if (denom < 30f) denom = 30f;
        float Aw = PERSP / denom;
        targetPanX = (float) (mouseX - viewW / 2f - AX * targetScale * Aw);
        targetPanY = (float) (mouseY - viewH / 2f - AY * targetScale * Aw);
        clampTargetPan();
        return true;
    }

    private void clampTargetPan() {
        float limit = 1200f * targetScale;
        targetPanX = Math.max(-limit, Math.min(limit, targetPanX));
        targetPanY = Math.max(-limit, Math.min(limit, targetPanY));
    }

    private void enterFocus(Planet p) {
        mode = Mode.FOCUS_PLANET;
        focusedPlanet = p;
        focusedMoonIndex = -1;
        targetScale = 2.0f;
        float wx = p.worldX(openedAt);
        float wy = p.worldY(openedAt);
        worldToScreenOffset(wx, wy, tmpPos);
        targetPanX = -tmpPos[0] * targetScale;
        targetPanY = -tmpPos[1] * targetScale;
        focusAnimStart = System.currentTimeMillis();
    }

    private void focusMoon(Planet p, int moonIndex) {
        mode = Mode.FOCUS_MOON;
        focusedPlanet = p;
        focusedMoonIndex = moonIndex;
        targetScale = 2.5f;
        float wx = p.worldX(openedAt) + p.moons.get(moonIndex).relX(openedAt);
        float wy = p.worldY(openedAt) + p.moons.get(moonIndex).relY(openedAt);
        worldToScreenOffset(wx, wy, tmpPos);
        targetPanX = -tmpPos[0] * targetScale;
        targetPanY = -tmpPos[1] * targetScale;
        focusAnimStart = System.currentTimeMillis();
    }

    private void focusPlanet(Planet p) {
        mode = Mode.FOCUS_PLANET;
        focusedPlanet = p;
        focusedMoonIndex = -1;
        targetScale = 2.0f;
        float wx = p.worldX(openedAt);
        float wy = p.worldY(openedAt);
        worldToScreenOffset(wx, wy, tmpPos);
        targetPanX = -tmpPos[0] * targetScale;
        targetPanY = -tmpPos[1] * targetScale;
        focusAnimStart = System.currentTimeMillis();
    }

    private void exitFocusAndCollapse() {
        mode = Mode.MAP;
        focusedPlanet = null;
        focusedMoonIndex = -1;
        targetScale = 0.8f;
        targetPanX = 0f;
        targetPanY = 0f;
        targetMoonExpand = 0f;
        expandedPlanet = null;
    }

    private void teleportTo(Selection sel) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (teleporting) return;

        ResourceKey<Level> targetDim = sel.getDimension();
        if (targetDim == null) {
            showToast("§c" + Component.translatable(
                    "gui.newgc.star_map.toast.locked", sel.displayName()).getString());
            return;
        }
        if (mc.level.dimension().equals(targetDim)) {
            showToast("§e" + Component.translatable(
                    "gui.newgc.star_map.toast.already", sel.displayName()).getString());
            return;
        }

        teleporting = true;
        PacketDistributor.sendToServer(new TeleportRequestPacket(
                targetDim,
                BlockPos.containing(mc.player.getX(), 100, mc.player.getZ())
        ));

        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}