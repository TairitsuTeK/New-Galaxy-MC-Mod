package com.tairitsutek.newgc.gui;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 星图使用的全部独立 GUI 纹理。
 * <p>
 * 这些图标不是方块/物品图集的一部分，第一次 blit 时才会从磁盘加载并上传。
 * 列表集中在这里，便于火箭上升阶段逐个预加载，避免轨道界面打开时集中卡顿。
 */
public final class StarMapAssets {

    public static final ResourceLocation SUN_ICON = tex("sun_icon.png");
    public static final ResourceLocation ALPHA_A_STAR_ICON = tex("alpha_centauri_a_star.png");
    public static final ResourceLocation ALPHA_B_STAR_ICON = tex("alpha_centauri_b_star.png");

    public static final List<ResourceLocation> ICON_TEXTURES = List.of(
            SUN_ICON,
            ALPHA_A_STAR_ICON,
            ALPHA_B_STAR_ICON,
            tex("mercury_icon.png"),
            tex("venus_icon.png"),
            tex("earth_icon.png"),
            tex("mars_icon.png"),
            tex("ceres_icon.png"),
            tex("jupiter_icon.png"),
            tex("saturn_icon.png"),
            tex("uranus_icon.png"),
            tex("neptune_icon.png"),
            tex("pluto_icon.png"),
            tex("makemake_icon.png"),
            tex("haumea_icon.png"),
            tex("eris_icon.png"),
            tex("proxima_b_icon.png"),
            tex("moon_icon.png"),
            tex("phobos_icon.png"),
            tex("deimos_icon.png"),
            tex("io_icon.png"),
            tex("europa_icon.png"),
            tex("ganymede_icon.png"),
            tex("callisto_icon.png"),
            tex("titan_icon.png"),
            tex("titania_icon.png"),
            tex("oberon_icon.png"),
            tex("triton_icon.png"),
            tex("charon_icon.png"),
            tex("hiiaka_icon.png")
    );

    private StarMapAssets() {
    }

    public static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath("newgc", "textures/gui/" + name);
    }
}
