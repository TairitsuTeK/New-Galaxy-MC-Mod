package com.tairitsutek.newgc.Planets;

import com.tairitsutek.newgc.NewGalaxyMain;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ModDimensions {

    // region 月球
    public static final ResourceKey<LevelStem> MOON_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "moon"));

    public static final ResourceKey<Level> MOON_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "moon"));

    public static final ResourceKey<DimensionType> MOON_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "moon_type"));
    // endregion

    // region 水星
    public static final ResourceKey<LevelStem> MERCURY_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mercury"));

    public static final ResourceKey<Level> MERCURY_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mercury"));

    public static final ResourceKey<DimensionType> MERCURY_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mercury_type"));
    // endregion

    // region 金星
    public static final ResourceKey<LevelStem> VENUS_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "venus"));

    public static final ResourceKey<Level> VENUS_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "venus"));

    public static final ResourceKey<DimensionType> VENUS_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "venus_type"));
    // endregion

    // region 谷神星
    public static final ResourceKey<LevelStem> CERES_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "ceres"));

    public static final ResourceKey<Level> CERES_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "ceres"));

    public static final ResourceKey<DimensionType> CERES_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "ceres_type"));
    // endregion

    // region 火星
    public static final ResourceKey<LevelStem> MARS_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars"));

    public static final ResourceKey<Level> MARS_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars"));

    public static final ResourceKey<DimensionType> MARS_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "mars_type"));
    // endregion

    // region 火卫一
    public static final ResourceKey<LevelStem> PHOBOS_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "phobos"));

    public static final ResourceKey<Level> PHOBOS_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "phobos"));

    public static final ResourceKey<DimensionType> PHOBOS_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "phobos_type"));
    // endregion

    // region 火卫二
    public static final ResourceKey<LevelStem> DEIMOS_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "deimos"));

    public static final ResourceKey<Level> DEIMOS_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "deimos"));

    public static final ResourceKey<DimensionType> DEIMOS_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "deimos_type"));
    // endregion

    // region 木星
    public static final ResourceKey<LevelStem> JUPITER_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "jupiter"));

    public static final ResourceKey<Level> JUPITER_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "jupiter"));

    public static final ResourceKey<DimensionType> JUPITER_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "jupiter_type"));
    // endregion

    // region 木卫一
    public static final ResourceKey<LevelStem> IO_KEY =
            ResourceKey.create(Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io"));

    public static final ResourceKey<Level> IO_LEVEL_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io"));

    public static final ResourceKey<DimensionType> IO_TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "io_type"));

    // endregion

    // region 外行星
    public static final ResourceKey<LevelStem> SATURN_KEY = stemKey("saturn");
    public static final ResourceKey<Level> SATURN_LEVEL_KEY = levelKey("saturn");
    public static final ResourceKey<DimensionType> SATURN_TYPE_KEY = typeKey("saturn");

    public static final ResourceKey<LevelStem> URANUS_KEY = stemKey("uranus");
    public static final ResourceKey<Level> URANUS_LEVEL_KEY = levelKey("uranus");
    public static final ResourceKey<DimensionType> URANUS_TYPE_KEY = typeKey("uranus");

    public static final ResourceKey<LevelStem> NEPTUNE_KEY = stemKey("neptune");
    public static final ResourceKey<Level> NEPTUNE_LEVEL_KEY = levelKey("neptune");
    public static final ResourceKey<DimensionType> NEPTUNE_TYPE_KEY = typeKey("neptune");

    public static final ResourceKey<LevelStem> PLUTO_KEY = stemKey("pluto");
    public static final ResourceKey<Level> PLUTO_LEVEL_KEY = levelKey("pluto");
    public static final ResourceKey<DimensionType> PLUTO_TYPE_KEY = typeKey("pluto");

    public static final ResourceKey<LevelStem> MAKEMAKE_KEY = stemKey("makemake");
    public static final ResourceKey<Level> MAKEMAKE_LEVEL_KEY = levelKey("makemake");
    public static final ResourceKey<DimensionType> MAKEMAKE_TYPE_KEY = typeKey("makemake");

    public static final ResourceKey<LevelStem> HAUMEA_KEY = stemKey("haumea");
    public static final ResourceKey<Level> HAUMEA_LEVEL_KEY = levelKey("haumea");
    public static final ResourceKey<DimensionType> HAUMEA_TYPE_KEY = typeKey("haumea");

    public static final ResourceKey<LevelStem> ERIS_KEY = stemKey("eris");
    public static final ResourceKey<Level> ERIS_LEVEL_KEY = levelKey("eris");
    public static final ResourceKey<DimensionType> ERIS_TYPE_KEY = typeKey("eris");
    // endregion

    // region 卫星
    public static final ResourceKey<LevelStem> EUROPA_KEY = stemKey("europa");
    public static final ResourceKey<Level> EUROPA_LEVEL_KEY = levelKey("europa");
    public static final ResourceKey<DimensionType> EUROPA_TYPE_KEY = typeKey("europa");

    public static final ResourceKey<LevelStem> GANYMEDE_KEY = stemKey("ganymede");
    public static final ResourceKey<Level> GANYMEDE_LEVEL_KEY = levelKey("ganymede");
    public static final ResourceKey<DimensionType> GANYMEDE_TYPE_KEY = typeKey("ganymede");

    public static final ResourceKey<LevelStem> CALLISTO_KEY = stemKey("callisto");
    public static final ResourceKey<Level> CALLISTO_LEVEL_KEY = levelKey("callisto");
    public static final ResourceKey<DimensionType> CALLISTO_TYPE_KEY = typeKey("callisto");

    public static final ResourceKey<LevelStem> TITAN_KEY = stemKey("titan");
    public static final ResourceKey<Level> TITAN_LEVEL_KEY = levelKey("titan");
    public static final ResourceKey<DimensionType> TITAN_TYPE_KEY = typeKey("titan");

    public static final ResourceKey<LevelStem> TITANIA_KEY = stemKey("titania");
    public static final ResourceKey<Level> TITANIA_LEVEL_KEY = levelKey("titania");
    public static final ResourceKey<DimensionType> TITANIA_TYPE_KEY = typeKey("titania");

    public static final ResourceKey<LevelStem> OBERON_KEY = stemKey("oberon");
    public static final ResourceKey<Level> OBERON_LEVEL_KEY = levelKey("oberon");
    public static final ResourceKey<DimensionType> OBERON_TYPE_KEY = typeKey("oberon");

    public static final ResourceKey<LevelStem> TRITON_KEY = stemKey("triton");
    public static final ResourceKey<Level> TRITON_LEVEL_KEY = levelKey("triton");
    public static final ResourceKey<DimensionType> TRITON_TYPE_KEY = typeKey("triton");

    public static final ResourceKey<LevelStem> CHARON_KEY = stemKey("charon");
    public static final ResourceKey<Level> CHARON_LEVEL_KEY = levelKey("charon");
    public static final ResourceKey<DimensionType> CHARON_TYPE_KEY = typeKey("charon");

    public static final ResourceKey<LevelStem> HIIAKA_KEY = stemKey("hiiaka");
    public static final ResourceKey<Level> HIIAKA_LEVEL_KEY = levelKey("hiiaka");
    public static final ResourceKey<DimensionType> HIIAKA_TYPE_KEY = typeKey("hiiaka");

    // region 半人马座 α 星系
    public static final ResourceKey<LevelStem> PROXIMA_B_KEY = stemKey("proxima_b");
    public static final ResourceKey<Level> PROXIMA_B_LEVEL_KEY = levelKey("proxima_b");
    public static final ResourceKey<DimensionType> PROXIMA_B_TYPE_KEY = typeKey("proxima_b");
    // endregion

    // region 太空漂流维度
    public static final ResourceKey<LevelStem> SPACE_KEY = stemKey("space");
    public static final ResourceKey<Level> SPACE_LEVEL_KEY = levelKey("space");
    public static final ResourceKey<DimensionType> SPACE_TYPE_KEY = typeKey("space");
    // endregion
    // endregion

    // region 全部星球维度
    public static final List<ResourceKey<Level>> ALL_LEVEL_KEYS = List.of(
            MOON_LEVEL_KEY, MERCURY_LEVEL_KEY, VENUS_LEVEL_KEY, CERES_LEVEL_KEY,
            MARS_LEVEL_KEY, PHOBOS_LEVEL_KEY, DEIMOS_LEVEL_KEY, JUPITER_LEVEL_KEY,
            IO_LEVEL_KEY, SATURN_LEVEL_KEY, URANUS_LEVEL_KEY, NEPTUNE_LEVEL_KEY,
            PLUTO_LEVEL_KEY, MAKEMAKE_LEVEL_KEY, HAUMEA_LEVEL_KEY, ERIS_LEVEL_KEY,
            EUROPA_LEVEL_KEY, GANYMEDE_LEVEL_KEY, CALLISTO_LEVEL_KEY, TITAN_LEVEL_KEY,
            TITANIA_LEVEL_KEY, OBERON_LEVEL_KEY, TRITON_LEVEL_KEY, CHARON_LEVEL_KEY,
            HIIAKA_LEVEL_KEY, PROXIMA_B_LEVEL_KEY
    );

    private static ResourceKey<LevelStem> stemKey(String id) {
        return ResourceKey.create(Registries.LEVEL_STEM,
                ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, id));
    }

    private static ResourceKey<Level> levelKey(String id) {
        return ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, id));
    }

    private static ResourceKey<DimensionType> typeKey(String id) {
        return ResourceKey.create(Registries.DIMENSION_TYPE,
                ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, id + "_type"));
    }
    // endregion

    // region 注册入口
    public static void register() {
        NewGalaxyMain.LOGGER.info(
                "Registering dimension keys for {}", NewGalaxyMain.MODID);
    }
    // endregion
}