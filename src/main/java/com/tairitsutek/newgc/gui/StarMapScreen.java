package com.tairitsutek.newgc.gui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.client.gui.UiTheme;
import com.tairitsutek.newgc.entity.RocketDestinations;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.gravity.PlanetGravity;
import com.tairitsutek.newgc.network.RocketDriftRequestPacket;
import com.tairitsutek.newgc.network.RocketTeleportPacket;
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
import java.util.Locale;

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

    private static final int COLOR_BG_TOP    = 0xFF08111C;
    private static final int COLOR_BG_BOTTOM = 0xFF02050A;

    private static final float[] C_ORBIT        = {0.30f, 0.52f, 0.64f, 0.32f};
    private static final float[] C_ORBIT_MID    = {0.34f, 0.62f, 0.76f, 0.07f};
    private static final float[] C_MOON_ORBIT   = {0.26f, 0.46f, 0.56f, 0.38f};
    private static final float[] C_ORBIT_SELECT = {0.45f, 0.88f, 0.98f, 0.95f};
    private static final float[] C_ORBIT_FOCUS  = {0.48f, 0.90f, 0.72f, 0.92f};
    private static final float[] C_ORBIT_GLOW   = {0.32f, 0.58f, 0.72f, 0.035f};
    private static final float[] C_ASTEROID     = {0.54f, 0.48f, 0.42f, 1.0f};
    private static final float[] C_ASTEROID_2   = {0.42f, 0.35f, 0.29f, 1.0f};
    private static final float[] C_KUIPER       = {0.42f, 0.54f, 0.60f, 1.0f};
    private static final float[] C_KUIPER_2     = {0.29f, 0.42f, 0.48f, 1.0f};
    private static final float[] C_OORT         = {0.35f, 0.48f, 0.60f, 1.0f};
    private static final float[] C_OORT_2       = {0.23f, 0.35f, 0.48f, 1.0f};
    private static final float[] C_HILLS        = {0.48f, 0.42f, 0.54f, 1.0f};
    private static final float[] C_HILLS_2      = {0.35f, 0.29f, 0.42f, 1.0f};

    private static final int COLOR_SELECT       = 0xFF66D79B;
    private static final int COLOR_MOON_SELECT  = 0xFF78D8F0;

    private static final int COLOR_LIST_BG_TOP     = UiTheme.PANEL_TOP;
    private static final int COLOR_LIST_BG_BOTTOM  = UiTheme.PANEL_BOTTOM;
    private static final int COLOR_LIST_BORDER     = UiTheme.PANEL_BORDER;
    private static final int COLOR_LIST_ACCENT     = UiTheme.ACCENT;
    private static final int COLOR_LIST_GLOW       = UiTheme.PANEL_GLOW;
    private static final int COLOR_LIST_HOVER      = 0x20FFFFFF;
    private static final int COLOR_LIST_HOVER_BAR  = 0x6078D8F0;
    private static final int COLOR_LIST_SELECT     = 0x4066D79B;
    private static final int COLOR_LIST_MOON_SELECT= 0x4078D8F0;
    private static final int COLOR_LIST_SCROLLBAR  = 0x6078D8F0;
    private static final int COLOR_LIST_SCROLL_BG  = 0x18FFFFFF;

    private static final int COLOR_ROW_GREEN_TOP    = 0x2466D79B;
    private static final int COLOR_ROW_GREEN_BOTTOM = 0x0C66D79B;
    private static final int COLOR_ROW_RED_TOP      = 0x20E2AB62;
    private static final int COLOR_ROW_RED_BOTTOM   = 0x0AE2AB62;
    private static final int COLOR_ROW_TEXT         = UiTheme.TEXT;

    private static final int COLOR_BTN_BG       = UiTheme.PANEL_TOP;
    private static final int COLOR_BTN_BORDER   = UiTheme.PANEL_BORDER;
    private static final int COLOR_BTN_BORDER_HOVER = UiTheme.ACCENT;
    private static final int COLOR_BTN_BORDER_ACTIVE = 0xFF66D79B;
    private static final int COLOR_BTN_TEXT     = UiTheme.TEXT;
    private static final int COLOR_BTN_TEXT_OFF = 0xFF607080;

    private static final int LIST_X       = 14;
    private static final int LIST_Y       = 64;
    private static final int LIST_W       = 122;
    private static final int LIST_ROW_H   = 19;
    private static final int LIST_PAD     = 6;
    private static final int MOON_ROW_H   = 15;
    private static final int MOON_INDENT  = 12;
    private static final int LIST_TITLE_H = 20;
    private static final int LIST_SCROLLBAR_W = 3;

    private static final int BTN_W        = 96;
    private static final int BTN_H        = 24;
    private static final int BTN_Y        = 18;
    private static final int BTN_MARGIN_R = 14;

    private static final int SYSTEM_SWITCH_W = 176;
    private static final int SYSTEM_SWITCH_H = 20;
    private static final int SYSTEM_SWITCH_Y = 20;
    private static final ResourceLocation ALPHA_A_STAR_ICON = StarMapAssets.ALPHA_A_STAR_ICON;
    private static final ResourceLocation ALPHA_B_STAR_ICON = StarMapAssets.ALPHA_B_STAR_ICON;

    private static final int INFO_W = 190;
    private static final int INFO_MARGIN = 14;
    private static final int INFO_GAP = 22;
    private static final int INFO_MIN_SCREEN_W = 560;
    private static final int COLOR_INFO_BG_TOP = UiTheme.PANEL_TOP;
    private static final int COLOR_INFO_BG_BOTTOM = UiTheme.PANEL_BOTTOM;
    private static final int COLOR_INFO_BORDER = UiTheme.PANEL_BORDER;
    private static final int COLOR_INFO_ACCENT = UiTheme.ACCENT;
    private static final int COLOR_INFO_LABEL = UiTheme.MUTED;
    private static final int COLOR_INFO_VALUE = UiTheme.TEXT;
    private static final int COLOR_INFO_LOCKED = 0xFFE2AB62;
    private static final int COLOR_INFO_OK = UiTheme.GOOD;

    private static final float SMOOTH_SCALE_SECONDS  = 0.45f;
    private static final float SMOOTH_PAN_SECONDS    = 0.35f;
    private static final float SMOOTH_MOON_SECONDS   = 0.35f;
    private static final float SMOOTH_SCROLL_SECONDS = 0.12f;
    private static final float SMOOTH_ROT_SECONDS    = 0.10f;
    /** 单帧最大时间步长：相当于把界面动画的更新上限限制在 30fps，避免高帧率下瞬间跳变。 */
    private static final float MAX_ANIM_STEP_SECONDS = 1.0f / 30.0f;

    private static final int PLANET_ICON_DISPLAY = 12;

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
    private final float[] tmpColor2 = new float[4];
    private final float[] tmpPos = new float[2];
    private final List<float[]> stars = new ArrayList<>();

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
    private final List<Planet> solarPlanets = new ArrayList<>();
    private final List<Planet> alphaPlanets = new ArrayList<>();
    private int activeSystem = 0;

    private static final ResourceLocation SUN_ICON = StarMapAssets.SUN_ICON;
    private static final int SUN_ICON_SIZE = 8;

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
    /** true 表示由轨道火箭打开，选择星球后火箭会一起跃迁。 */
    private final boolean rocketMode;

    /** 轨道图展开动画时长（毫秒）：只让中央星图由内向外展开，外围界面保持固定。 */
    private static final long OPEN_ANIM_MS = 1500L;
    /** 动画起始比例：保留一张完整可读的轨道图，不再从接近零的最小尺寸开始。 */
    private static final float OPEN_MIN_MAP_REVEAL = 0.55F;
    /** 界面打开时刻，用于计算展开动画。 */
    private final long openedAtMs = System.currentTimeMillis();
    /** 当前帧缓存的轨道展开比例，避免每个顶点单独读取时间导致同一帧比例不一致。 */
    private float openMapReveal = OPEN_MIN_MAP_REVEAL;
    /** 当前帧缓存的天体图标尺寸比例。 */
    private float openSpriteScale = 0.4F;
    /** 上一帧渲染时刻，用于帧率无关的界面动画。 */
    private long lastRenderNanos;

    private Selection hoveredSelection = null;
    private boolean hoveredOnMap = false;

    public StarMapScreen() {
        this(false);
    }

    /**
     * @param rocketMode true 表示由轨道火箭打开，选择星球后火箭连同玩家一起跃迁
     */
    public StarMapScreen(boolean rocketMode) {
        super(Component.translatable("gui.newgc.star_map"));
        this.rocketMode = rocketMode;
    }

    private int teleportBtnX() { return this.width - BTN_W - BTN_MARGIN_R; }
    private int teleportBtnY() { return BTN_Y; }

    private int infoPanelX() { return this.width - INFO_W - INFO_MARGIN; }
    private int infoPanelY() { return BTN_Y + BTN_H + INFO_GAP; }
    private boolean infoPanelVisible() { return this.width >= INFO_MIN_SCREEN_W; }

    private boolean isOverInfoPanel(double mouseX, double mouseY) {
        if (!infoPanelVisible()) return false;
        int x = infoPanelX();
        int y = infoPanelY();
        int h = Math.min(196, this.height - y - 46);
        return mouseX >= x && mouseX < x + INFO_W && mouseY >= y && mouseY < y + h;
    }

    private boolean isOverTeleportButton(double mouseX, double mouseY) {
        int bx = teleportBtnX();
        int by = teleportBtnY();
        return mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H;
    }

    private int systemSwitchX() {
        return this.width / 2 - SYSTEM_SWITCH_W / 2;
    }

    private boolean isOverSystemSwitcher(double mouseX, double mouseY) {
        int x = systemSwitchX();
        return mouseX >= x && mouseX < x + SYSTEM_SWITCH_W
                && mouseY >= SYSTEM_SWITCH_Y && mouseY < SYSTEM_SWITCH_Y + SYSTEM_SWITCH_H;
    }

    private String activeSystemName() {
        return Component.translatable(activeSystem == 0
                ? "gui.newgc.system.sol"
                : "gui.newgc.system.alpha_centauri").getString();
    }

    private String activeSystemSubtitle() {
        return Component.translatable(activeSystem == 0
                ? "gui.newgc.star_map.subtitle"
                : "gui.newgc.star_map.alpha_centauri.subtitle").getString();
    }

    private void ensureAlphaPlanets() {
        if (!alphaPlanets.isEmpty()) return;
        alphaPlanets.add(new Planet("gui.newgc.planet.proxima_b", "newgc:proxima_b",
                250, 140, 0.052f, 0.0f, 14,
                ModDimensions.PROXIMA_B_LEVEL_KEY,
                tex("proxima_b_icon.png"), 16, PLANET_ICON_DISPLAY + 2,
                false, noMoons()));
    }

    private void switchSystem() {
        activeSystem = activeSystem == 0 ? 1 : 0;
        if (activeSystem == 1) {
            ensureAlphaPlanets();
            planets.clear();
            planets.addAll(alphaPlanets);
        } else {
            planets.clear();
            planets.addAll(solarPlanets);
        }

        selected = null;
        focusedPlanet = null;
        focusedMoonIndex = -1;
        mode = Mode.MAP;
        expandedPlanet = null;
        moonExpand = 0f;
        targetMoonExpand = 0f;
        listScroll = 0f;
        targetListScroll = 0f;
        listContentHeight = 0;
        hoveredSelection = null;
        hoveredOnMap = false;
        mapScale = targetScale = 0.8f;
        panX = targetPanX = 0f;
        panY = targetPanY = 0f;
        rotYaw = targetRotYaw = 0f;
        rotPitch = targetRotPitch = 0f;
        rotRoll = targetRotRoll = 0f;
        updateRotationMatrix();
        openedAt = System.currentTimeMillis();
        teleportBtnPressed = false;
    }

    private void drawSystemSwitcher(GuiGraphics g, int mouseX, int mouseY) {
        int x = systemSwitchX();
        int y = SYSTEM_SWITCH_Y;
        int w = SYSTEM_SWITCH_W;
        int h = SYSTEM_SWITCH_H;
        boolean hover = mouseX >= x && mouseX < x + w
                && mouseY >= y && mouseY < y + h;
        int border = hover ? UiTheme.ACCENT : UiTheme.PANEL_BORDER;

        g.fillGradient(x, y, x + w, y + h,
                hover ? 0xE0163344 : 0xE00C1D2A,
                UiTheme.PANEL_BOTTOM);
        drawRect2(g, x, y, x + w, y + h, border);
        drawCornerBrackets(g, x, y, x + w, y + h, border);
        g.drawString(this.font, "‹", x + 7, y + 6, UiTheme.ACCENT, false);
        g.drawString(this.font, "›", x + w - 13, y + 6, UiTheme.ACCENT, false);
        g.drawCenteredString(this.font,
                trimToWidth(activeSystemName(), w - 34),
                x + w / 2, y + 6, UiTheme.TEXT);
    }

    private boolean isTeleportBtnActive() {
        if (toastMessage != null) return false;
        if (selected == null) return false;
        if (!isReachable(selected.getDimension())) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;
        return !mc.level.dimension().equals(selected.getDimension());
    }

    private Selection getTeleportTarget() {
        if (selected == null) return null;
        if (!isReachable(selected.getDimension())) return null;
        return selected;
    }

    private RocketEntity currentRocket() {
        if (!rocketMode) return null;
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getVehicle() instanceof RocketEntity rocket
                ? rocket : null;
    }

    private int rocketTier() {
        RocketEntity rocket = currentRocket();
        return rocket == null ? RocketDestinations.TIER_1 : rocket.getTier();
    }

    private ResourceKey<Level> currentDimension() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.dimension();
    }

    private boolean rocketNoTargets() {
        return rocketMode && !RocketDestinations.hasAnyTarget(currentDimension(), rocketTier());
    }

    private String restrictedTargets() {
        List<String> names = RocketDestinations.targetNames(currentDimension(), rocketTier());
        return names.isEmpty() ? "—" : String.join(" / ", names);
    }

    private String noTargetsHint() {
        RocketEntity rocket = currentRocket();
        int seconds = rocket == null ? 0
                : Math.max(0, (rocket.getDriftCountdownTicks() + 19) / 20);
        return Component.translatable("gui.newgc.star_map.hint.no_targets", seconds).getString();
    }

    private String restrictionHint() {
        if (rocketNoTargets()) {
            return noTargetsHint();
        }
        String hintKey;
        if (rocketTier() >= RocketDestinations.TIER_3) {
            hintKey = "gui.newgc.star_map.hint.tier3";
        } else if (rocketTier() >= RocketDestinations.TIER_2) {
            hintKey = "gui.newgc.star_map.hint.tier2";
        } else {
            hintKey = "gui.newgc.star_map.hint.tier1";
        }
        return Component.translatable(hintKey, restrictedTargets()).getString();
    }

    /**
     * 当前星图模式能否抵达该维度。
     * <p>
     * 传送器模式（{@code rocketMode == false}）不受限制；火箭模式按当前火箭等级
     * 和所在天体使用 {@link RocketDestinations} 的同一套相邻天体规则。
     */
    private boolean isReachable(ResourceKey<Level> dimension) {
        if (dimension == null) return false;
        if (!rocketMode) return true;
        return RocketDestinations.canReach(dimension, currentDimension(), rocketTier());
    }

    private static List<Moon> noMoons() { return List.of(); }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        viewW = this.width;
        viewH = this.height;

        planets.clear();
        activeSystem = 0;

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
                                16, 6, tex("io_icon.png"), ModDimensions.IO_LEVEL_KEY),
                        new Moon("gui.newgc.moon.europa", 24, 16, 2.2f, (float) (Math.PI / 2),
                                16, 6, tex("europa_icon.png"), ModDimensions.EUROPA_LEVEL_KEY),
                        new Moon("gui.newgc.moon.ganymede", 33, 22, 1.6f, (float) Math.PI,
                                16, 7, tex("ganymede_icon.png"), ModDimensions.GANYMEDE_LEVEL_KEY),
                        new Moon("gui.newgc.moon.callisto", 44, 29, 1.1f, (float) (Math.PI * 1.5),
                                16, 7, tex("callisto_icon.png"), ModDimensions.CALLISTO_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.saturn", "newgc:saturn",
                279, 153, 0.034f, (float) (Math.PI * 1.1), 11,
                ModDimensions.SATURN_LEVEL_KEY,
                tex("saturn_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.titan", 24, 16, 1.8f, 0f,
                        16, 7, tex("titan_icon.png"), ModDimensions.TITAN_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.uranus", "newgc:uranus",
                394, 217, 0.012f, (float) (Math.PI * 1.6), 10,
                ModDimensions.URANUS_LEVEL_KEY,
                tex("uranus_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.titania", 18, 12, 2.4f, 0f,
                                16, 6, tex("titania_icon.png"), ModDimensions.TITANIA_LEVEL_KEY),
                        new Moon("gui.newgc.moon.oberon", 27, 18, 1.7f, (float) Math.PI,
                                16, 6, tex("oberon_icon.png"), ModDimensions.OBERON_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.neptune", "newgc:neptune",
                494, 272, 0.006f, (float) (Math.PI * 0.2), 10,
                ModDimensions.NEPTUNE_LEVEL_KEY,
                tex("neptune_icon.png"), 16, PLANET_ICON_DISPLAY,
                false,
                List.of(new Moon("gui.newgc.moon.triton", 20, 13, 1.5f, 0f,
                        16, 6, tex("triton_icon.png"), ModDimensions.TRITON_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.pluto", "newgc:pluto",
                560, 308, 0.004f, (float) (Math.PI * 1.4), 7,
                ModDimensions.PLUTO_LEVEL_KEY,
                tex("pluto_icon.png"), 16, PLANET_ICON_DISPLAY,
                true,
                List.of(new Moon("gui.newgc.moon.charon", 14, 9, 2.0f, 0f,
                        16, 6, tex("charon_icon.png"), ModDimensions.CHARON_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.makemake", "newgc:makemake",
                600, 330, 0.0032f, (float) (Math.PI * 0.7), 6,
                ModDimensions.MAKEMAKE_LEVEL_KEY,
                tex("makemake_icon.png"), 16, PLANET_ICON_DISPLAY,
                true, noMoons()));
        planets.add(new Planet("gui.newgc.planet.haumea", "newgc:haumea",
                580, 319, 0.0036f, (float) (Math.PI * 1.9), 6,
                ModDimensions.HAUMEA_LEVEL_KEY,
                tex("haumea_icon.png"), 16, PLANET_ICON_DISPLAY,
                true,
                List.of(new Moon("gui.newgc.moon.hiiaka", 16, 11, 2.6f, 0f,
                        16, 5, tex("hiiaka_icon.png"), ModDimensions.HIIAKA_LEVEL_KEY))));
        planets.add(new Planet("gui.newgc.planet.eris", "newgc:eris",
                660, 363, 0.0027f, (float) (Math.PI * 0.4), 6,
                ModDimensions.ERIS_LEVEL_KEY,
                tex("eris_icon.png"), 16, PLANET_ICON_DISPLAY,
                true, noMoons()));

        solarPlanets.clear();
        solarPlanets.addAll(planets);

        // 背景点阵已足够表达带纹；控制数量可显著减少每帧写入的缓冲顶点。
        asteroidBelt.clear();
        RandomSource beltRng = RandomSource.create(99999L);
        for (int i = 0; i < 160; i++) {
            asteroidBelt.add(new float[]{
                    (float) (beltRng.nextDouble() * Math.PI * 2),
                    (float) (beltRng.nextDouble() * 40 - 20),
                    1 + beltRng.nextFloat() * 1.2f,
                    0.5f + beltRng.nextFloat() * 0.5f
            });
        }
        kuiperBelt.clear();
        RandomSource kuiperRng = RandomSource.create(11111L);
        for (int i = 0; i < 140; i++) {
            kuiperBelt.add(new float[]{
                    (float) (kuiperRng.nextDouble() * Math.PI * 2),
                    (float) (kuiperRng.nextDouble() * 70 - 35),
                    1 + kuiperRng.nextFloat() * 1.0f,
                    0.3f + kuiperRng.nextFloat() * 0.7f
            });
        }
        hillsCloud.clear();
        RandomSource hillsRng = RandomSource.create(22222L);
        for (int i = 0; i < 110; i++) {
            hillsCloud.add(new float[]{
                    (float) (hillsRng.nextDouble() * Math.PI * 2),
                    (float) (hillsRng.nextDouble() * 80 - 40),
                    1 + hillsRng.nextFloat(),
                    0.3f + hillsRng.nextFloat() * 0.5f
            });
        }
        oortCloud.clear();
        RandomSource oortRng = RandomSource.create(33333L);
        for (int i = 0; i < 90; i++) {
            oortCloud.add(new float[]{
                    (float) (oortRng.nextDouble() * Math.PI * 2),
                    (float) (oortRng.nextDouble() * 100 - 50),
                    1 + oortRng.nextFloat() * 0.8f,
                    0.2f + oortRng.nextFloat() * 0.4f
            });
        }

        stars.clear();
        RandomSource starRng = RandomSource.create(424242L);
        for (int i = 0; i < 220; i++) {
            stars.add(new float[]{
                    starRng.nextFloat(),
                    starRng.nextFloat(),
                    0.20f + starRng.nextFloat() * 0.75f,
                    1 + starRng.nextInt(2),
                    0.25f + starRng.nextFloat() * 0.75f
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

        // 从半人马座α打开星图时默认显示当前星系，避免玩家误以为目标在太阳系。
        if (ModDimensions.PROXIMA_B_LEVEL_KEY.equals(currentDimension())) {
            activeSystem = 1;
            ensureAlphaPlanets();
            planets.clear();
            planets.addAll(alphaPlanets);
        }
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
        float reveal = mapReveal();
        out[0] = viewW / 2f + X * mapScale * w * reveal + panX;
        out[1] = viewH / 2f + Y * mapScale * w * reveal + panY;
        return w;
    }

    private void worldToScreenOffset(float wx, float wy, float[] out) {
        float X = rotA * wx + rotB * wy;
        float Y = rotC * wx + rotD * wy;
        float Z = rotE * wx + rotF * wy;
        float denom = PERSP - Z;
        if (denom < 30f) denom = 30f;
        float w = PERSP / denom;
        // 这里用于计算聚焦/居中目标，必须使用动画完成后的最终偏移；
        // 展开比例只作用于实际绘制坐标（project）。
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
                h += (int) (p.moons.size() * MOON_ROW_H * Math.max(moonExpand, targetMoonExpand));
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
        // 不要再对整页做缩放：背景、标题和所有面板始终固定在最终位置，
        // 展开动画只通过 project() 作用在中央轨道图上。
        updateOpenAnimation();
        renderStarMap(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * 打开动画进度：0 → 1，使用 smoothstep 缓动。
     * <p>
     * 起点和终点速度都为 0，因此轨道展开会缓慢起步、缓慢收尾，不会产生突兀的缩放跳变。
     */
    private float openProgress() {
        long elapsed = System.currentTimeMillis() - this.openedAtMs;
        float raw = Mth.clamp(elapsed / (float) OPEN_ANIM_MS, 0.0F, 1.0F);
        return raw * raw * (3.0F - 2.0F * raw);
    }

    /** 每帧只更新一次展开比例，保证同一帧内所有轨道和天体使用相同进度。 */
    private void updateOpenAnimation() {
        float progress = openProgress();
        openMapReveal = OPEN_MIN_MAP_REVEAL + (1.0F - OPEN_MIN_MAP_REVEAL) * progress;
        openSpriteScale = 0.65F + 0.35F * progress;
    }

    /** 中央轨道图的展开比例：从很小的初始值平滑增长到 1。 */
    private float mapReveal() {
        return openMapReveal;
    }

    /** 天体图标尺寸的展开比例：让图标跟随轨道从小变大。 */
    private float mapSpriteScale() {
        return openSpriteScale;
    }

    /** 展开动画尚未完成时忽略点击，避免点到还没就位的控件。 */
    private boolean stillOpening() {
        return openProgress() < 0.75F;
    }

    /**
     * 帧率无关的时间步长。
     * <p>
     * 按真实经过时间计算并限制最大步长（{@link #MAX_ANIM_STEP_SECONDS}），
     * 相当于给界面动画一个 30fps 的更新上限：高帧率下不会"一帧就滑到目标位置"，
     * 低帧率下也不会跳变。
     */
    private float nextFrameDeltaSeconds() {
        long now = System.nanoTime();
        float dt = this.lastRenderNanos == 0L
                ? 1.0F / 60.0F
                : (now - this.lastRenderNanos) / 1_000_000_000.0F;
        this.lastRenderNanos = now;
        return Mth.clamp(dt, 0.0F, MAX_ANIM_STEP_SECONDS);
    }

    /** 指数平滑系数：时间常数越大，逼近目标越慢。 */
    private static float smoothingFactor(float dt, float seconds) {
        return 1.0F - (float) Math.exp(-dt / seconds);
    }

    private void renderStarMap(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isFocusMode() && focusedPlanet != null) {
            focusWorld(tmpPos);
            float fwx = tmpPos[0], fwy = tmpPos[1];
            worldToScreenOffset(fwx, fwy, tmpPos);
            targetPanX = -tmpPos[0] * targetScale;
            targetPanY = -tmpPos[1] * targetScale;
        }

        float dt = nextFrameDeltaSeconds();
        float kScale = smoothingFactor(dt, SMOOTH_SCALE_SECONDS);
        float kPan   = smoothingFactor(dt, SMOOTH_PAN_SECONDS);
        float kMoon  = smoothingFactor(dt, SMOOTH_MOON_SECONDS);
        float kScroll= smoothingFactor(dt, SMOOTH_SCROLL_SECONDS);
        float kRot   = smoothingFactor(dt, SMOOTH_ROT_SECONDS);

        mapScale += (targetScale - mapScale) * kScale;
        panX     += (targetPanX - panX)     * kPan;
        panY     += (targetPanY - panY)     * kPan;
        moonExpand += (targetMoonExpand - moonExpand) * kMoon;
        listScroll += (targetListScroll - listScroll) * kScroll;

        rotYaw   += (targetRotYaw   - rotYaw)   * kRot;
        rotPitch += (targetRotPitch - rotPitch) * kRot;
        rotRoll  += (targetRotRoll  - rotRoll)  * kRot;
        updateRotationMatrix();

        listContentHeight = computeContentHeight();
        clampListScroll();
        hoveredSelection = findHoveredSelection(mouseX, mouseY);

        drawBackground(graphics);

        renderLines(graphics);

        graphics.enableScissor(0, 0, this.width, this.height);
        renderSprites(graphics);
        graphics.disableScissor();

        graphics.fillGradient(0, this.height - 48, this.width, this.height,
                0x00000000, 0x50000000);

        int headerMax = Math.max(40,
                teleportBtnX() - (LIST_X + 4) - 10);
        graphics.drawString(this.font,
                trimToWidth(this.title.getString(), headerMax),
                LIST_X + 4, 12, UiTheme.TEXT, true);
        graphics.drawString(this.font,
                trimToWidth(activeSystemSubtitle(), headerMax),
                LIST_X + 4, 26, UiTheme.MUTED, true);

        drawPlanetList(graphics, mouseX, mouseY);
        drawTeleportButton(graphics, mouseX, mouseY);
        drawSystemSwitcher(graphics, mouseX, mouseY);
        drawInfoPanel(graphics);

        String hint;
        if (rocketMode && rocketNoTargets()) {
            hint = noTargetsHint();
        } else if (isFocusMode()) {
            hint = Component.translatable("gui.newgc.star_map.hint.focus").getString();
        } else if (selected != null) {
            if (!isReachable(selected.getDimension())) {
                hint = restrictionHint();
            } else if (selected.isTeleportable()) {
                hint = Component.translatable("gui.newgc.star_map.hint.zoom",
                        selected.displayName()).getString();
            } else {
                hint = Component.translatable("gui.newgc.star_map.hint.locked",
                        selected.displayName()).getString();
            }
        } else {
            hint = Component.translatable("gui.newgc.star_map.hint.select").getString();
        }
        graphics.drawCenteredString(this.font, trimToWidth(hint, this.width - 40),
                this.width / 2, this.height - 22, UiTheme.MUTED);

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

        drawHoverTooltip(graphics, mouseX, mouseY);
    }

    private Selection findHoveredSelection(int mouseX, int mouseY) {
        hoveredOnMap = false;
        if (isOverInfoPanel(mouseX, mouseY) || isOverTeleportButton(mouseX, mouseY)) {
            return null;
        }

        Selection listSelection = listSelectionAt(mouseX, mouseY);
        if (listSelection != null) {
            return listSelection;
        }

        if (isFocusMode()) {
            MoonHit moonHit = hitMoon(mouseX, mouseY);
            if (moonHit != null) {
                hoveredOnMap = true;
                return newSelection(moonHit.planet, moonHit.moonIndex);
            }
        }

        Planet planet = hitPlanet(mouseX, mouseY);
        if (planet != null) {
            hoveredOnMap = true;
            return newSelection(planet, -1);
        }
        return null;
    }

    private Selection listSelectionAt(double mouseX, double mouseY) {
        int viewH = listViewH();
        int contentTop = LIST_Y + LIST_TITLE_H;
        int contentBottom = contentTop + viewH;
        if (mouseX < LIST_X || mouseX >= LIST_X + LIST_W || mouseY < contentTop || mouseY >= contentBottom) {
            return null;
        }

        int rowX = LIST_X + 3;
        int rowW = LIST_W - 6 - (listContentHeight > viewH ? LIST_SCROLLBAR_W + 2 : 0);
        int curY = contentTop + LIST_PAD - (int) listScroll;
        for (Planet p : planets) {
            if (mouseY >= curY && mouseY < curY + LIST_ROW_H && mouseX >= rowX && mouseX < rowX + rowW) {
                return newSelection(p, -1);
            }
            curY += LIST_ROW_H;
            if (expandedPlanet == p && !p.moons.isEmpty() && moonExpand > 0.01f) {
                int visibleMoons = (int) Math.ceil(p.moons.size() * moonExpand);
                for (int mi = 0; mi < visibleMoons && mi < p.moons.size(); mi++) {
                    int moonX = rowX + MOON_INDENT;
                    if (mouseY >= curY && mouseY < curY + MOON_ROW_H && mouseX >= moonX && mouseX < rowX + rowW) {
                        return newSelection(p, mi);
                    }
                    curY += MOON_ROW_H;
                }
            }
        }
        return null;
    }

    private Planet hitPlanet(double mouseX, double mouseY) {
        Planet hit = null;
        double hitR = Math.max(14, 12 * mapScale);
        double bestDist = hitR * hitR;
        for (Planet p : planets) {
            project(p.worldX(openedAt), p.worldY(openedAt), tmpPos);
            double dx = mouseX - tmpPos[0];
            double dy = mouseY - tmpPos[1];
            double d2 = dx * dx + dy * dy;
            if (d2 <= bestDist) {
                bestDist = d2;
                hit = p;
            }
        }
        return hit;
    }

    private Selection newSelection(Planet planet, int moonIndex) {
        Selection selection = new Selection();
        selection.planet = planet;
        selection.moonIndex = moonIndex;
        return selection;
    }

    private void drawInfoPanel(GuiGraphics g) {
        if (!infoPanelVisible()) return;

        int x = infoPanelX();
        int y = infoPanelY();
        int w = INFO_W;
        int h = Math.min(196, this.height - y - 46);
        if (h < 150) return;

        UiTheme.panel(g, x, y, w, h);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + 18,
                0x301D4560, 0x001D4560);
        g.fill(x + 1, y + 18, x + w - 1, y + 19, 0x4078D8F0);
        g.drawString(this.font,
                Component.translatable("gui.newgc.star_map.info.title").getString(),
                x + 8, y + 5, UiTheme.TEXT, true);

        if (selected == null) {
            g.drawString(this.font,
                    trimToWidth(Component.translatable("gui.newgc.star_map.info.empty").getString(), w - 14),
                    x + 7, y + 28, COLOR_INFO_LABEL, false);
            g.drawString(this.font,
                    trimToWidth(Component.translatable("gui.newgc.star_map.info.footer").getString(), w - 14),
                    x + 7, y + h - 12, UiTheme.MUTED, false);
            return;
        }

        Planet planet = selected.planet;
        Moon moon = selected.isMoon() ? planet.moons.get(selected.moonIndex) : null;
        ResourceKey<Level> dimension = selected.getDimension();
        ResourceLocation icon = moon != null ? moon.iconTexture : planet.iconTexture;
        int iconSize = 24;
        g.blit(icon, x + 8, y + 23, iconSize, iconSize, 0f, 0f,
                moon != null ? moon.iconSize : planet.iconSize,
                moon != null ? moon.iconSize : planet.iconSize,
                moon != null ? moon.iconSize : planet.iconSize,
                moon != null ? moon.iconSize : planet.iconSize);

        g.drawString(this.font, trimToWidth(selected.displayName(), w - 44),
                x + 38, y + 24, UiTheme.TEXT, false);
        String typeKey = selected.isMoon()
                ? "gui.newgc.star_map.info.moon"
                : (planet.isDwarf ? "gui.newgc.star_map.info.dwarf" : "gui.newgc.star_map.info.planet");
        g.drawString(this.font, Component.translatable(typeKey).getString(),
                x + 38, y + 36, COLOR_INFO_LABEL, false);

        int lineY = y + 56;
        lineY = drawInfoLine(g, x, lineY, "status", statusText(dimension), statusColor(dimension));
        lineY = drawInfoLine(g, x, lineY, "gravity", gravityText(dimension), COLOR_INFO_VALUE);
        lineY = drawInfoLine(g, x, lineY, "oxygen", oxygenText(dimension), COLOR_INFO_VALUE);
        lineY = drawInfoLine(g, x, lineY, "environment",
                Component.translatable(environmentKey(dimension)).getString(), COLOR_INFO_VALUE);
        drawInfoLine(g, x, lineY, "dimension",
                dimension == null ? "—" : dimension.location().toString(), COLOR_INFO_LABEL);

        g.drawString(this.font,
                trimToWidth(Component.translatable("gui.newgc.star_map.info.footer").getString(), w - 14),
                x + 7, y + h - 12, UiTheme.MUTED, false);
    }

    private int drawInfoLine(GuiGraphics g, int x, int y, String key, String value, int valueColor) {
        g.drawString(this.font,
                Component.translatable("gui.newgc.star_map.info." + key).getString(),
                x + 7, y, COLOR_INFO_LABEL, false);
        g.drawString(this.font, trimToWidth(value, INFO_W - 78),
                x + 70, y, valueColor, false);
        return y + 11;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (text == null) return "";
        if (this.font.width(text) <= maxWidth) return text;
        int ellipsis = this.font.width("…");
        return this.font.plainSubstrByWidth(text, Math.max(0, maxWidth - ellipsis)) + "…";
    }

    private String statusText(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return Component.translatable("gui.newgc.star_map.info.locked").getString();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(dimension)) {
            return Component.translatable("gui.newgc.star_map.info.current").getString();
        }
        if (!isReachable(dimension)) {
            return Component.translatable("gui.newgc.star_map.info.out_of_range").getString();
        }
        return Component.translatable("gui.newgc.star_map.info.unlocked").getString();
    }

    private int statusColor(ResourceKey<Level> dimension) {
        if (dimension == null) return COLOR_INFO_LOCKED;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(dimension)) return UiTheme.ACCENT;
        if (!isReachable(dimension)) return UiTheme.WARN;
        return COLOR_INFO_OK;
    }

    private String gravityText(ResourceKey<Level> dimension) {
        if (dimension == null) return "—";
        return String.format(Locale.ROOT, "%.2f×", PlanetGravity.getMultiplier(dimension));
    }

    private String oxygenText(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return Component.translatable("gui.newgc.star_map.info.unknown").getString();
        }
        if (dimension.equals(Level.OVERWORLD)) {
            return Component.translatable("gui.newgc.star_map.info.breathable").getString();
        }
        return Component.translatable("gui.newgc.star_map.info.require_oxygen").getString();
    }

    private String environmentKey(ResourceKey<Level> dimension) {
        if (dimension == null) return "gui.newgc.star_map.env.unknown";
        if (dimension.equals(Level.OVERWORLD)) return "gui.newgc.star_map.env.overworld";
        if (dimension.equals(ModDimensions.MOON_LEVEL_KEY)) return "gui.newgc.star_map.env.moon";
        if (dimension.equals(ModDimensions.PROXIMA_B_LEVEL_KEY)) return "gui.newgc.star_map.env.proxima_b";
        if (dimension.equals(ModDimensions.MERCURY_LEVEL_KEY)) return "gui.newgc.star_map.env.mercury";
        if (dimension.equals(ModDimensions.VENUS_LEVEL_KEY)) return "gui.newgc.star_map.env.venus";
        if (dimension.equals(ModDimensions.MARS_LEVEL_KEY)) return "gui.newgc.star_map.env.mars";
        if (dimension.equals(ModDimensions.JUPITER_LEVEL_KEY)) return "gui.newgc.star_map.env.jupiter";
        if (dimension.equals(ModDimensions.IO_LEVEL_KEY)) return "gui.newgc.star_map.env.io";
        if (dimension.equals(ModDimensions.CERES_LEVEL_KEY)) return "gui.newgc.star_map.env.ceres";
        if (dimension.equals(ModDimensions.PHOBOS_LEVEL_KEY)
                || dimension.equals(ModDimensions.DEIMOS_LEVEL_KEY)) {
            return "gui.newgc.star_map.env.small_moon";
        }
        if (dimension.equals(ModDimensions.SATURN_LEVEL_KEY)
                || dimension.equals(ModDimensions.URANUS_LEVEL_KEY)
                || dimension.equals(ModDimensions.NEPTUNE_LEVEL_KEY)) {
            return "gui.newgc.star_map.env.gas_giant";
        }
        if (dimension.equals(ModDimensions.PLUTO_LEVEL_KEY)
                || dimension.equals(ModDimensions.MAKEMAKE_LEVEL_KEY)
                || dimension.equals(ModDimensions.HAUMEA_LEVEL_KEY)
                || dimension.equals(ModDimensions.ERIS_LEVEL_KEY)) {
            return "gui.newgc.star_map.env.dwarf";
        }
        if (dimension.equals(ModDimensions.EUROPA_LEVEL_KEY)
                || dimension.equals(ModDimensions.GANYMEDE_LEVEL_KEY)
                || dimension.equals(ModDimensions.CALLISTO_LEVEL_KEY)
                || dimension.equals(ModDimensions.TITAN_LEVEL_KEY)
                || dimension.equals(ModDimensions.TITANIA_LEVEL_KEY)
                || dimension.equals(ModDimensions.OBERON_LEVEL_KEY)
                || dimension.equals(ModDimensions.TRITON_LEVEL_KEY)
                || dimension.equals(ModDimensions.CHARON_LEVEL_KEY)
                || dimension.equals(ModDimensions.HIIAKA_LEVEL_KEY)) {
            return "gui.newgc.star_map.env.icy_moon";
        }
        return "gui.newgc.star_map.env.default";
    }

    private void drawHoverTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (hoveredSelection == null || !hoveredOnMap) return;

        String name = hoveredSelection.displayName();
        ResourceKey<Level> dimension = hoveredSelection.getDimension();
        String second = statusText(dimension) + " · " + gravityText(dimension);

        int width = Math.max(this.font.width(name), this.font.width(second)) + 12;
        int height = 30;
        int x = Math.min(mouseX + 10, this.width - width - 4);
        int y = Math.min(mouseY + 10, this.height - height - 4);
        UiTheme.panel(g, x, y, width, height);
        g.drawString(this.font, name, x + 6, y + 4, UiTheme.TEXT, false);
        g.drawString(this.font, second, x + 6, y + 15, UiTheme.MUTED, false);
    }

    private void drawLockIcon(GuiGraphics g, int x, int y) {
        int metal = 0xFFFFC878;
        int shade = 0xFF8A5A20;
        g.fill(x + 1, y, x + 4, y + 1, shade);
        g.fill(x + 1, y + 1, x + 2, y + 3, shade);
        g.fill(x + 3, y + 1, x + 4, y + 3, shade);
        g.fill(x, y + 3, x + 5, y + 7, metal);
        g.fill(x + 2, y + 4, x + 3, y + 6, shade);
    }

    /**
     * 红色禁止标记：一阶火箭射程之外的天体。
     */
    private void drawBlockedIcon(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 7, y + 7, 0xA06A4A20);
        g.fill(x, y, x + 7, y + 1, 0xFFE2AB62);
        g.fill(x, y + 6, x + 7, y + 7, 0xFFE2AB62);
        for (int i = 1; i < 6; i++) {
            g.fill(x + i, y + i, x + i + 1, y + i + 1, 0xFFFFFFFF);
            g.fill(x + 6 - i, y + i, x + 7 - i, y + i + 1, 0xFFFFFFFF);
        }
    }

    private void drawCurrentMarker(GuiGraphics g, int cx, int cy, int radius) {
        int color = 0xFF66E0FF;
        int x = cx + radius - 1;
        int y = cy - radius + 1;
        g.fill(x, y + 1, x + 3, y + 3, color);
        g.fill(x - 1, y + 1, x + 2, y + 4, color);
        g.fill(x, y, x + 3, y + 1, color);
    }

    private void drawBackground(GuiGraphics graphics) {
        graphics.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);
        graphics.fillGradient(0, 0, this.width, this.height / 2, 0x181C4B63, 0x001C4B63);
        graphics.fillGradient(0, this.height / 3, this.width, this.height, 0x00030A12, 0x40030A12);
        graphics.fillGradient(0, 0, this.width, 58, 0xD008111C, 0x4008111C);
        graphics.fill(0, LIST_Y - 6, this.width, LIST_Y - 5, 0x4078D8F0);
    }

    /**
     * 在上升阶段提前创建星图专用 RenderType 的缓冲区，触发着色器准备。
     * 只提交空缓冲区，不会在游戏画面中绘制任何内容。
     */
    public static void prewarmRenderType() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(STAR_MAP_LINES);
        // 一个全透明、屏幕外的四边形：只为触发着色器编译，不会产生可见内容。
        Matrix4f mat = graphicsMatrix();
        vc.addVertex(mat, -20000.0F, -20000.0F, 0.0F).setColor(0, 0, 0, 0);
        vc.addVertex(mat, -19999.0F, -20000.0F, 0.0F).setColor(0, 0, 0, 0);
        vc.addVertex(mat, -19999.0F, -19999.0F, 0.0F).setColor(0, 0, 0, 0);
        vc.addVertex(mat, -20000.0F, -19999.0F, 0.0F).setColor(0, 0, 0, 0);
        bufferSource.endBatch(STAR_MAP_LINES);
    }

    private static Matrix4f graphicsMatrix() {
        return new Matrix4f();
    }

    private void renderLines(GuiGraphics graphics) {
        Matrix4f mat = graphics.pose().last().pose();

        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(STAR_MAP_LINES);
        long now = System.currentTimeMillis();

        // 只保留背景星空与流星，不再绘制星云、极光、日冕和行星光晕。
        renderStars(vc, mat, now);

        // 椭圆由短线段拼成；320 段在最大缩放下也已足够平滑，避免每帧写入过多顶点。
        int orbitSegments = mapScale < 0.15f ? 160
                : mapScale < 0.3f ? 192
                  : mapScale < 0.8f ? 256 : 320;
        for (Planet p : planets) {
            boolean focused = isFocusMode() && focusedPlanet == p;
            boolean selectedPlanet = !isFocusMode() && selected != null
                    && !selected.isMoon() && selected.planet == p;
            boolean hoveredPlanet = hoveredSelection != null && !hoveredSelection.isMoon()
                    && hoveredSelection.planet == p;
            boolean highlight = focused || selectedPlanet || hoveredPlanet;
            float[] color = focused ? C_ORBIT_FOCUS
                    : selectedPlanet ? C_ORBIT_SELECT
                    : hoveredPlanet ? C_ORBIT_SELECT : C_ORBIT;
            appendEllipse(vc, mat, p.orbitA, p.orbitB, color,
                    orbitSegments, highlight ? 0.95f : 0.45f);
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

        renderShootingStars(vc, mat, now);
        graphics.flush();
    }

    private void renderStars(VertexConsumer vc, Matrix4f mat, long now) {
        for (float[] s : stars) {
            float x = starScreenX(s);
            float y = starScreenY(s);
            float depth = s.length > 4 ? s[4] : 1f;
            float size = s[3] + (depth > 0.82f ? 1 : 0);
            float twinkle = 0.48f + 0.52f * (float) Math.sin(
                    now / (650.0 + depth * 520.0) + s[0] * 53f + s[1] * 79f);
            float bright = s[2] * (0.62f + 0.38f * twinkle);
            int a = (int) (bright * 255f);
            if (a < 8) continue;

            // 少量暖色星和蓝白星混排，避免整片星空像同一种颜色。
            float warmth = (s[0] * 13.7f + s[1] * 7.1f) % 1f;
            int r = (int) Mth.lerp(warmth, 135f, 235f) * (int) (bright * 255f) / 255;
            int g = (int) Mth.lerp(warmth, 165f, 220f) * (int) (bright * 255f) / 255;
            int b = (int) Mth.lerp(warmth, 235f, 195f) * (int) (bright * 255f) / 255;
            appendPoint4(vc, mat, x, y, size, Math.min(255, r + 25),
                    Math.min(255, g + 25), Math.min(255, b + 25), a);
        }
    }

    private float starScreenX(float[] s) {
        float depth = s.length > 4 ? s[4] : 1f;
        float x = s[0] * this.width + panX * 0.085f * depth;
        float w = Math.max(1, this.width);
        return ((x % w) + w) % w;
    }

    private float starScreenY(float[] s) {
        float depth = s.length > 4 ? s[4] : 1f;
        float y = s[1] * this.height + panY * 0.085f * depth;
        float h = Math.max(1, this.height);
        return ((y % h) + h) % h;
    }

    private void renderShootingStars(VertexConsumer vc, Matrix4f mat, long now) {
        for (int layer = 0; layer < 2; layer++) {
            long period = 6400L + layer * 4300L;
            long cycle = now / period;
            float phase = (now % period) / (float) period;
            if (phase > 0.23f) continue;
            float life = phase / 0.23f;
            RandomSource rng = RandomSource.create(0x51A7L + cycle * 97L + layer * 1319L);
            boolean leftToRight = rng.nextBoolean();
            float startY = this.height * (0.08f + rng.nextFloat() * 0.68f);
            float slope = (rng.nextFloat() - 0.5f) * 0.35f;
            float travel = (this.width + 120f) * life;
            float startX = leftToRight ? -40f : this.width + 40f;
            float dir = leftToRight ? 1f : -1f;
            float headX = startX + dir * travel;
            float headY = startY + slope * travel;
            float tailLen = 60f + 55f * life;
            float tailX = headX - dir * tailLen;
            float tailY = headY - slope * tailLen;

            tmpColor[0] = 0.72f; tmpColor[1] = 0.94f; tmpColor[2] = 1.0f;
            tmpColor[3] = 0.55f * (1.0f - life * 0.65f);
            tmpColor2[0] = 0.20f; tmpColor2[1] = 0.48f; tmpColor2[2] = 0.95f;
            tmpColor2[3] = 0.0f;
            appendGradientSegment(vc, mat, tailX, tailY, headX, headY,
                    1.6f + layer * 0.4f, tmpColor2, tmpColor);
            tmpColor[3] = 0.34f;
            appendPoint4(vc, mat, headX, headY, 2, 205, 245, 255,
                    (int) (tmpColor[3] * 255f));
        }
    }

    private void renderSprites(GuiGraphics graphics) {
        long now = System.currentTimeMillis();
        Minecraft mc = Minecraft.getInstance();
        Selection hovered = hoveredSelection;

        boolean alphaSystem = activeSystem == 1;
        ResourceLocation starIcon = alphaSystem ? ALPHA_A_STAR_ICON : SUN_ICON;
        int starTexSize = alphaSystem ? 8 : SUN_ICON_SIZE;
        project(0f, 0f, tmpPos);
        int sunX = (int) tmpPos[0];
        int sunY = (int) tmpPos[1];
        int sunSize = Math.max(2, Math.round((alphaSystem ? 22 : 16) * mapSpriteScale()));
        graphics.blit(starIcon,
                sunX - sunSize / 2, sunY - sunSize / 2,
                sunSize, sunSize, 0f, 0f,
                starTexSize, starTexSize, starTexSize, starTexSize);
        if (alphaSystem) {
            project(30f, -10f, tmpPos);
            int bx = (int) tmpPos[0];
            int by = (int) tmpPos[1];
            int bSize = Math.max(2, Math.round(15 * mapSpriteScale()));
            graphics.blit(ALPHA_B_STAR_ICON,
                    bx - bSize / 2, by - bSize / 2,
                    bSize, bSize, 0f, 0f,
                    8, 8, 8, 8);
        }

        for (Planet p : planets) {
            float pwx = p.worldX(openedAt);
            float pwy = p.worldY(openedAt);
            float w = project(pwx, pwy, tmpPos);
            int px = (int) tmpPos[0];
            int py = (int) tmpPos[1];
            if (px < -32 || px > this.width + 32) continue;
            if (py < -32 || py > this.height + 32) continue;

            int size = Math.max(2, Math.round(p.displaySize
                    * Mth.clamp(w, 0.75f, 1.5f) * mapSpriteScale()));
            graphics.blit(p.iconTexture,
                    px - size / 2, py - size / 2, size, size, 0f, 0f,
                    p.iconSize, p.iconSize, p.iconSize, p.iconSize);
            int r = size / 2;

            boolean isHovered = hovered != null && !hovered.isMoon() && hovered.planet == p;
            boolean isCurrent = p.dimension != null && mc.level != null
                    && mc.level.dimension().equals(p.dimension);

            if (isHovered && !(selected != null && !selected.isMoon() && selected.planet == p)) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(now / 300.0);
                int pad = (int) ((r + 4) * (1.0f + 0.08f * pulse));
                drawSelectionFrame(graphics, px - pad, py - pad, px + pad, py + pad, 0x8066CCFF);
            }
            boolean planetReachable = isReachable(p.dimension);
            if (!p.isTeleportable()) {
                drawLockIcon(graphics, px + size / 2 - 5, py + size / 2 - 2);
            } else if (!planetReachable) {
                drawBlockedIcon(graphics, px + size / 2 - 6, py + size / 2 - 6);
            }
            if (isCurrent) {
                drawCurrentMarker(graphics, px, py, r);
            }
            if (mapScale > 0.85f || isHovered
                    || (selected != null && !selected.isMoon() && selected.planet == p)) {
                graphics.drawCenteredString(this.font, p.displayName(),
                        px, py + size / 2 + 2, planetReachable ? 0xE0FFFFFF : 0xFFFF6060);
            }

            if (isFocusMode() && focusedPlanet == p && !p.moons.isEmpty()) {
                for (int mi = 0; mi < p.moons.size(); mi++) {
                    Moon m = p.moons.get(mi);
                    float mwx = pwx + m.relX(openedAt);
                    float mwy = pwy + m.relY(openedAt);
                    float mw = project(mwx, mwy, tmpPos);
                    int mx = (int) tmpPos[0];
                    int my = (int) tmpPos[1];
                    int msize = Math.max(2, Math.round(Math.max(4, m.displaySize)
                            * Mth.clamp(mw, 0.75f, 1.5f) * mapSpriteScale()));
                    graphics.blit(m.iconTexture,
                            mx - msize / 2, my - msize / 2, msize, msize, 0f, 0f,
                            m.iconSize, m.iconSize, m.iconSize, m.iconSize);

                    boolean isSelectedMoon = selected != null && selected.isMoon()
                            && selected.planet == p && selected.moonIndex == mi;
                    boolean moonHovered = hovered != null && hovered.isMoon()
                            && hovered.planet == p && hovered.moonIndex == mi;
                    boolean moonCurrent = m.dimension != null && mc.level != null
                            && mc.level.dimension().equals(m.dimension);

                    if (moonHovered && !isSelectedMoon) {
                        float pulse = 0.5f + 0.5f * (float) Math.sin(now / 300.0);
                        int pad = (int) ((msize / 2f + 3) * (1.0f + 0.08f * pulse));
                        drawSelectionFrame(graphics, mx - pad, my - pad, mx + pad, my + pad,
                                0x8066CCFF);
                    }
                    if (isSelectedMoon) {
                        int pad = msize / 2 + 3;
                        drawSelectionFrame(graphics, mx - pad, my - pad, mx + pad, my + pad,
                                COLOR_MOON_SELECT);
                    }
                    boolean moonReachable = isReachable(m.dimension);
                    if (!m.isTeleportable()) {
                        drawLockIcon(graphics, mx + msize / 2 - 4, my + msize / 2 - 2);
                    } else if (!moonReachable) {
                        drawBlockedIcon(graphics, mx + msize / 2 - 5, my + msize / 2 - 5);
                    }
                    if (moonCurrent) {
                        drawCurrentMarker(graphics, mx, my, msize / 2);
                    }
                    if (mapScale > 1.2f || moonHovered || isSelectedMoon) {
                        graphics.drawCenteredString(this.font, m.displayName(),
                                mx, my + msize / 2 + 2, moonReachable ? 0xC0FFFFFF : 0xFFFF6060);
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
            int msize = Math.max(2, Math.round(Math.max(4, m.displaySize)
                    * Mth.clamp(mw, 0.75f, 1.5f) * mapSpriteScale()));

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

    private void appendGradientSegment(VertexConsumer vc, Matrix4f mat,
                                       float x1, float y1, float x2, float y2,
                                       float thickness, float[] from, float[] to) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return;
        float hw = thickness * 0.5f;
        float nx = -dy / len * hw;
        float ny = dx / len * hw;
        int fr = (int) (Mth.clamp(from[0], 0f, 1f) * 255f);
        int fg = (int) (Mth.clamp(from[1], 0f, 1f) * 255f);
        int fb = (int) (Mth.clamp(from[2], 0f, 1f) * 255f);
        int fa = (int) (Mth.clamp(from[3], 0f, 1f) * 255f);
        int tr = (int) (Mth.clamp(to[0], 0f, 1f) * 255f);
        int tg = (int) (Mth.clamp(to[1], 0f, 1f) * 255f);
        int tb = (int) (Mth.clamp(to[2], 0f, 1f) * 255f);
        int ta = (int) (Mth.clamp(to[3], 0f, 1f) * 255f);
        vc.addVertex(mat, x1 + nx, y1 + ny, 0f).setColor(fr, fg, fb, fa);
        vc.addVertex(mat, x2 + nx, y2 + ny, 0f).setColor(tr, tg, tb, ta);
        vc.addVertex(mat, x2 - nx, y2 - ny, 0f).setColor(tr, tg, tb, ta);
        vc.addVertex(mat, x1 - nx, y1 - ny, 0f).setColor(fr, fg, fb, fa);
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

        int borderColor;
        if (!active) borderColor = COLOR_BTN_BORDER & 0x60FFFFFF;
        else if (teleportBtnPressed) borderColor = 0xFFFFFFFF;
        else if (hover) borderColor = COLOR_BTN_BORDER_HOVER;
        else borderColor = COLOR_BTN_BORDER_ACTIVE;
        g.fillGradient(bx, by, bx + bw, by + bh,
                active ? UiTheme.PANEL_TOP : 0xA0060B12,
                active ? UiTheme.PANEL_BOTTOM : 0xA004070A);
        drawRect2(g, bx, by, bx + bw, by + bh, borderColor);
        drawCornerBrackets(g, bx, by, bx + bw, by + bh, borderColor);

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

        UiTheme.panel(g, x0, y0, LIST_W, totalH);
        g.fillGradient(x0 + 1, y0 + 1, x1 - 1, y0 + LIST_TITLE_H,
                0x301D4560, 0x001D4560);
        g.fill(x0 + 1, y0 + LIST_TITLE_H - 1, x1 - 1, y0 + LIST_TITLE_H,
                0x4078D8F0);
        g.drawString(this.font,
                Component.translatable("gui.newgc.star_map.list_title").getString(),
                x0 + 8, y0 + 6, UiTheme.TEXT, true);

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

        boolean reachable = p.isTeleportable() && isReachable(p.dimension);
        g.fillGradient(rowX, rowY, rowX + rowW, rowY + LIST_ROW_H,
                0x42101A24, 0x18070D14);
        g.fill(rowX, rowY, rowX + 2, rowY + LIST_ROW_H,
                reachable ? 0xA066D79B : 0x90E2AB62);

        if (hover) {
            g.fill(rowX, rowY, rowX + rowW, rowY + LIST_ROW_H, COLOR_LIST_HOVER);
            g.fill(rowX, rowY, rowX + 2, rowY + LIST_ROW_H, COLOR_LIST_HOVER_BAR);
        }
        if (isSelected) {
            g.fillGradient(rowX, rowY, rowX + rowW, rowY + LIST_ROW_H,
                    0x4066D79B, 0x1066D79B);
            g.fill(rowX, rowY, rowX + 2, rowY + LIST_ROW_H, COLOR_SELECT);
        }

        g.drawString(this.font, trimToWidth(p.displayName(), rowW - 22),
                rowX + 7, rowY + 4, COLOR_ROW_TEXT, true);
        if (expandedPlanet == p && !p.moons.isEmpty()) {
            g.drawString(this.font, "▾", rowX + rowW - 10, rowY + 4, UiTheme.TEXT, false);
        } else if (!p.moons.isEmpty()) {
            g.drawString(this.font, "▸", rowX + rowW - 10, rowY + 4, UiTheme.ACCENT, false);
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

        boolean reachable = m.isTeleportable() && isReachable(m.dimension);
        g.fillGradient(moonX, rowY, rowX + rowW, rowY + MOON_ROW_H,
                0x2E101A24, 0x10070D14);
        g.fill(moonX, rowY, moonX + 2, rowY + MOON_ROW_H,
                reachable ? 0x8066D79B : 0x70E2AB62);

        if (hover) {
            g.fill(moonX, rowY, rowX + rowW, rowY + MOON_ROW_H, COLOR_LIST_HOVER);
            g.fill(moonX, rowY, moonX + 2, rowY + MOON_ROW_H, COLOR_LIST_HOVER_BAR);
        }
        if (isSelected) {
            g.fillGradient(moonX, rowY, rowX + rowW, rowY + MOON_ROW_H,
                    0x2878D8F0, 0x0678D8F0);
            g.fill(moonX, rowY, moonX + 2, rowY + MOON_ROW_H, COLOR_MOON_SELECT);
        }

        g.drawString(this.font, "· " + trimToWidth(m.displayName(),
                        rowW - MOON_INDENT - 12),
                moonX + 5, rowY + 2, COLOR_ROW_TEXT, true);
    }

    private void drawRect2(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void drawCornerBrackets(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int s = 9, t = 1;
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (isFocusMode()) {
                exitFocusAndCollapse();
                return true;
            }
            if (rocketMode) {
                // 轨道火箭必须先选定目的地，ESC 不能关闭界面
                showToast("§e" + Component.translatable(
                        "gui.newgc.star_map.rocket_locked").getString());
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (selected != null && isTeleportBtnActive()) {
                teleportTo(selected);
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            cycleSelection(!hasShiftDown());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F) {
            if (selected != null) {
                expandedPlanet = selected.planet;
                targetMoonExpand = 1f;
                if (selected.isMoon()) {
                    focusMoon(selected.planet, selected.moonIndex);
                } else {
                    focusPlanet(selected.planet);
                }
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD) {
            float minS = isFocusMode() ? FOCUS_MIN_SCALE : MIN_SCALE;
            float maxS = isFocusMode() ? FOCUS_MAX_SCALE : MAX_SCALE;
            targetScale = Mth.clamp(targetScale * 1.1f, minS, maxS);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) {
            float minS = isFocusMode() ? FOCUS_MIN_SCALE : MIN_SCALE;
            float maxS = isFocusMode() ? FOCUS_MAX_SCALE : MAX_SCALE;
            targetScale = Mth.clamp(targetScale / 1.1f, minS, maxS);
            return true;
        }

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
        if (stillOpening()) return true;

        if (button == 1 && isFocusMode()) {
            exitFocusAndCollapse();
            return true;
        }

        if (button == 0) {
            if (isOverSystemSwitcher(mouseX, mouseY)) {
                switchSystem();
                return true;
            }

            int bx = teleportBtnX();
            int by = teleportBtnY();
            if (mouseX >= bx && mouseX < bx + BTN_W && mouseY >= by && mouseY < by + BTN_H) {
                if (isTeleportBtnActive() && !teleporting) {
                    teleportBtnPressed = true;
                    Selection t = getTeleportTarget();
                    if (t != null) teleportTo(t);
                } else if (rocketMode && rocketNoTargets()) {
                    showToast("§c" + Component.translatable(
                            "gui.newgc.star_map.toast.no_targets").getString());
                } else if (selected != null && !isReachable(selected.getDimension())) {
                    showToast("§c" + Component.translatable(
                            "gui.newgc.star_map.toast.out_of_range",
                            restrictedTargets()).getString());
                }
                return true;
            }

            if (isOverInfoPanel(mouseX, mouseY)) return true;
            if (handleListHit(mouseX, mouseY)) return true;

            MoonHit moonHit = hitMoon(mouseX, mouseY);
            if (moonHit != null) {
                selected = new Selection();
                selected.planet = moonHit.planet;
                selected.moonIndex = moonHit.moonIndex;
                expandedPlanet = moonHit.planet;
                targetMoonExpand = 1f;
                if (isFocusMode()) {
                    focusMoon(moonHit.planet, moonHit.moonIndex);
                }
                ensureSelectedVisible();
                ensureSelectedListVisible();
                return true;
            }

            Planet hit = hitPlanet(mouseX, mouseY);
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
        ensureSelectedListVisible();
    }

    private void setSelectedPlanet(Planet p) {
        if (p == null) { selected = null; return; }
        selected = new Selection();
        selected.planet = p;
        selected.moonIndex = -1;
        ensureSelectedVisible();
        ensureSelectedListVisible();
    }

    private void cycleSelection(boolean forward) {
        List<Selection> all = new ArrayList<>();
        for (Planet p : planets) {
            all.add(newSelection(p, -1));
            for (int i = 0; i < p.moons.size(); i++) {
                all.add(newSelection(p, i));
            }
        }
        if (all.isEmpty()) return;

        int current = -1;
        if (selected != null) {
            for (int i = 0; i < all.size(); i++) {
                Selection s = all.get(i);
                if (s.planet == selected.planet && s.moonIndex == selected.moonIndex) {
                    current = i;
                    break;
                }
            }
        }

        int next = current < 0 ? 0 : Math.floorMod(current + (forward ? 1 : -1), all.size());
        selected = all.get(next);
        expandedPlanet = selected.planet;
        targetMoonExpand = 1f;
        listContentHeight = computeContentHeight();
        clampListScroll();
        ensureSelectedVisible();
        ensureSelectedListVisible();

        if (isFocusMode()) {
            if (selected.isMoon()) {
                focusMoon(selected.planet, selected.moonIndex);
            } else {
                focusPlanet(selected.planet);
            }
        }
    }

    private void ensureSelectedListVisible() {
        if (selected == null) return;

        int rowY = LIST_PAD;
        for (Planet p : planets) {
            if (selected.planet == p && !selected.isMoon()) {
                scrollListTo(rowY, LIST_ROW_H);
                return;
            }
            rowY += LIST_ROW_H;
            if (expandedPlanet == p && !p.moons.isEmpty()) {
                for (int i = 0; i < p.moons.size(); i++) {
                    if (selected.planet == p && selected.isMoon() && selected.moonIndex == i) {
                        scrollListTo(rowY, MOON_ROW_H);
                        return;
                    }
                    rowY += MOON_ROW_H;
                }
            }
        }
    }

    private void scrollListTo(int rowY, int rowH) {
        int viewH = listViewH();
        int max = Math.max(0, listContentHeight - viewH);
        float target = targetListScroll;
        if (rowY < target) {
            target = rowY;
        } else if (rowY + rowH > target + viewH) {
            target = rowY + rowH - viewH;
        }
        targetListScroll = Mth.clamp(target, 0, max);
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
        if (isOverInfoPanel(mouseX, mouseY)) return true;

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
        if (!isReachable(targetDim)) {
            if (rocketMode && rocketNoTargets()) {
                showToast("§c" + Component.translatable(
                        "gui.newgc.star_map.toast.no_targets").getString());
            } else {
                showToast("§c" + Component.translatable(
                        "gui.newgc.star_map.toast.out_of_range",
                        restrictedTargets()).getString());
            }
            return;
        }
        if (rocketMode) {
            // 轨道火箭模式：火箭与玩家一起跃迁，不再回到发射平台。
            // 允许选择当前所在星球（从轨道降落回地表）。
            teleporting = true;
            PacketDistributor.sendToServer(new RocketTeleportPacket(targetDim));
            this.forceClose();
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

    /**
     * 轨道模式通常必须选择目的地；只有“当前天体没有可用目标”时允许关闭，
     * 关闭后立即请求服务端进入漂流。服务端倒计时结束时也会自动进入漂流。
     */
    @Override
    public void onClose() {
        if (rocketMode) {
            RocketEntity rocket = currentRocket();
            if (rocket != null && rocket.getState() == RocketEntity.STATE_ORBIT
                    && rocketNoTargets()) {
                PacketDistributor.sendToServer(new RocketDriftRequestPacket());
                super.onClose();
                return;
            }
            if (rocket != null && rocket.getState() == RocketEntity.STATE_DRIFT) {
                super.onClose();
            }
            return;
        }
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (!rocketMode) {
            return true;
        }
        RocketEntity rocket = currentRocket();
        return rocket != null && (rocket.getState() == RocketEntity.STATE_DRIFT
                || rocketNoTargets());
    }

    /** 跃迁提交后强制关闭界面（不受轨道模式锁定影响）。 */
    private void forceClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}