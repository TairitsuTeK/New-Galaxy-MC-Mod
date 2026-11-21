package com.tairitsutek.newgc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.gui.StarMapAssets;
import com.tairitsutek.newgc.gui.StarMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 火箭上升阶段分 tick 预热星图资源。
 * <p>
 * 星图图标是独立 PNG，第一次绘制时会同步读盘并上传纹理；把它们分散到上升阶段，
 * 可以避免到达轨道的那一帧同时加载几十张纹理而卡顿。
 * 预加载只发生在客户端，且所有操作都在渲染线程的 client tick 中执行。
 */
public final class StarMapPrewarm {

    /** 两张纹理之间至少间隔的 tick 数，避免在同一个 tick 集中读盘。 */
    private static final int TEXTURE_INTERVAL_TICKS = 2;

    private static final List<String> FONT_KEYS = List.of(
            "gui.newgc.star_map",
            "gui.newgc.star_map.subtitle",
            "gui.newgc.star_map.alpha_centauri.subtitle",
            "gui.newgc.star_map.controls",
            "gui.newgc.star_map.teleport",
            "gui.newgc.star_map.list_title",
            "gui.newgc.system.sol",
            "gui.newgc.system.alpha_centauri",
            "gui.newgc.star_map.info.title",
            "gui.newgc.star_map.info.planet",
            "gui.newgc.star_map.info.moon",
            "gui.newgc.star_map.info.dwarf",
            "gui.newgc.star_map.info.footer",
            "gui.newgc.star_map.info.empty",
            "gui.newgc.star_map.info.status",
            "gui.newgc.star_map.info.gravity",
            "gui.newgc.star_map.info.oxygen",
            "gui.newgc.star_map.info.environment",
            "gui.newgc.star_map.info.dimension",
            "gui.newgc.star_map.info.current",
            "gui.newgc.star_map.info.unlocked",
            "gui.newgc.star_map.info.out_of_range",
            "gui.newgc.star_map.info.locked",
            "gui.newgc.star_map.info.breathable",
            "gui.newgc.star_map.info.require_oxygen",
            "gui.newgc.star_map.info.unknown",
            "gui.newgc.star_map.hint.select",
            "gui.newgc.star_map.hint.zoom",
            "gui.newgc.star_map.hint.focus",
            "gui.newgc.star_map.hint.moon",
            "gui.newgc.star_map.hint.locked",
            "gui.newgc.star_map.hint.no_targets",
            "gui.newgc.star_map.hint.tier1",
            "gui.newgc.star_map.hint.tier2",
            "gui.newgc.star_map.hint.tier3",
            "gui.newgc.star_map.toast.no_targets",
            "gui.newgc.star_map.env.ceres",
            "gui.newgc.star_map.env.default",
            "gui.newgc.star_map.env.dwarf",
            "gui.newgc.star_map.env.gas_giant",
            "gui.newgc.star_map.env.icy_moon",
            "gui.newgc.star_map.env.io",
            "gui.newgc.star_map.env.jupiter",
            "gui.newgc.star_map.env.mars",
            "gui.newgc.star_map.env.mercury",
            "gui.newgc.star_map.env.moon",
            "gui.newgc.star_map.env.overworld",
            "gui.newgc.star_map.env.proxima_b",
            "gui.newgc.star_map.env.small_moon",
            "gui.newgc.star_map.env.unknown",
            "gui.newgc.star_map.env.venus",
            "gui.newgc.rocket.tier1",
            "gui.newgc.rocket.tier2",
            "gui.newgc.rocket.tier3",
            "gui.newgc.rocket.tier_label",
            "gui.newgc.planet.mercury",
            "gui.newgc.planet.venus",
            "gui.newgc.planet.earth",
            "gui.newgc.planet.mars",
            "gui.newgc.planet.ceres",
            "gui.newgc.planet.jupiter",
            "gui.newgc.planet.saturn",
            "gui.newgc.planet.uranus",
            "gui.newgc.planet.neptune",
            "gui.newgc.planet.pluto",
            "gui.newgc.planet.makemake",
            "gui.newgc.planet.haumea",
            "gui.newgc.planet.eris",
            "gui.newgc.planet.proxima_b",
            "gui.newgc.moon.moon",
            "gui.newgc.moon.phobos",
            "gui.newgc.moon.deimos",
            "gui.newgc.moon.io",
            "gui.newgc.moon.europa",
            "gui.newgc.moon.ganymede",
            "gui.newgc.moon.callisto",
            "gui.newgc.moon.titan",
            "gui.newgc.moon.titania",
            "gui.newgc.moon.oberon",
            "gui.newgc.moon.triton",
            "gui.newgc.moon.charon",
            "gui.newgc.moon.hiiaka"
    );

    private static int textureIndex;
    private static int fontIndex;
    private static int textureCooldown;
    private static boolean renderTypeWarmed;

    private StarMapPrewarm() {
    }

    /** 由客户端 tick 调用；点火倒计时与上升阶段都可以推进预加载。 */
    public static void tick(RocketEntity rocket) {
        int state = rocket.getState();
        if (state != RocketEntity.STATE_COUNTDOWN
                && state != RocketEntity.STATE_ASCENT) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (textureIndex < StarMapAssets.ICON_TEXTURES.size()) {
            if (textureCooldown > 0) {
                textureCooldown--;
                return;
            }
            mc.getTextureManager().getTexture(
                    StarMapAssets.ICON_TEXTURES.get(textureIndex++));
            textureCooldown = TEXTURE_INTERVAL_TICKS;
            return;
        }

        if (!renderTypeWarmed) {
            StarMapScreen.prewarmRenderType();
            renderTypeWarmed = true;
            return;
        }

        if (fontIndex < FONT_KEYS.size()) {
            prewarmFont(mc, Component.translatable(FONT_KEYS.get(fontIndex++)).getString());
        }
    }

    /**
     * 把文字绘制到屏幕外的透明顶点中并立即 flush。
     * <p>
     * {@code font.width()} 只会查询字形信息，不会把中文字形烘焙进字体纹理；
     * 真正调用 {@code drawInBatch} 才会生成 BakedGlyph 并上传字形图集。
     * 这里使用全透明颜色和屏幕外坐标，因此不会在游戏画面中显示任何内容。
     */
    private static void prewarmFont(Minecraft mc, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        PoseStack pose = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        mc.font.drawInBatch(text, -20000.0F, -20000.0F, 0x00FFFFFF, false,
                pose.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        bufferSource.endBatch();
    }
}
