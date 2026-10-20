package com.tairitsutek.newgc.Planets;

import com.tairitsutek.newgc.NewGalaxyMain;

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

    // region 注册入口
    public static void register() {
        NewGalaxyMain.LOGGER.info(
                "Registering dimension keys for {}", NewGalaxyMain.MODID);
    }
    // endregion
}