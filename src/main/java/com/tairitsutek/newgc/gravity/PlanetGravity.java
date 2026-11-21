package com.tairitsutek.newgc.gravity;

import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * 全模组统一的重力配置。
 *
 * <p>数值表示相对于主世界（1.0）的重力倍率。重力的唯一来源是
 * {@code MixinEntityGravity}：它在 {@code Entity#getGravity()} 返回时乘以该倍率，
 * 因此玩家、生物和掉落物都会受到相同影响。
 * {@code MixinLivingEntityFallDamage} 使用同一倍率缩放坠落距离，
 * 让低重力星球的坠落伤害与原版 {@code SAFE_FALL_DISTANCE} 属性保持兼容。</p>
 */
public final class PlanetGravity {

    private static final double DEFAULT_MULTIPLIER = 1.0;

    /**
     * 各天体的重力倍率。数值经过游戏化调整，不严格等于现实值。
     */
    private static final Map<ResourceKey<Level>, Double> MULTIPLIERS = Map.ofEntries(
            Map.entry(ModDimensions.MOON_LEVEL_KEY, 0.30),
            Map.entry(ModDimensions.MERCURY_LEVEL_KEY, 0.40),
            Map.entry(ModDimensions.VENUS_LEVEL_KEY, 0.80),
            Map.entry(ModDimensions.CERES_LEVEL_KEY, 0.30),
            Map.entry(ModDimensions.MARS_LEVEL_KEY, 0.38),
            Map.entry(ModDimensions.PHOBOS_LEVEL_KEY, 0.06),
            Map.entry(ModDimensions.DEIMOS_LEVEL_KEY, 0.04),
            Map.entry(ModDimensions.JUPITER_LEVEL_KEY, 1.60),
            Map.entry(ModDimensions.IO_LEVEL_KEY, 0.183),
            Map.entry(ModDimensions.SATURN_LEVEL_KEY, 1.35),
            Map.entry(ModDimensions.URANUS_LEVEL_KEY, 0.70),
            Map.entry(ModDimensions.NEPTUNE_LEVEL_KEY, 1.45),
            Map.entry(ModDimensions.PLUTO_LEVEL_KEY, 0.06),
            Map.entry(ModDimensions.MAKEMAKE_LEVEL_KEY, 0.05),
            Map.entry(ModDimensions.HAUMEA_LEVEL_KEY, 0.04),
            Map.entry(ModDimensions.ERIS_LEVEL_KEY, 0.08),
            Map.entry(ModDimensions.EUROPA_LEVEL_KEY, 0.13),
            Map.entry(ModDimensions.GANYMEDE_LEVEL_KEY, 0.15),
            Map.entry(ModDimensions.CALLISTO_LEVEL_KEY, 0.13),
            Map.entry(ModDimensions.TITAN_LEVEL_KEY, 0.14),
            Map.entry(ModDimensions.TITANIA_LEVEL_KEY, 0.04),
            Map.entry(ModDimensions.OBERON_LEVEL_KEY, 0.04),
            Map.entry(ModDimensions.TRITON_LEVEL_KEY, 0.08),
            Map.entry(ModDimensions.CHARON_LEVEL_KEY, 0.03),
            Map.entry(ModDimensions.HIIAKA_LEVEL_KEY, 0.02),
            Map.entry(ModDimensions.PROXIMA_B_LEVEL_KEY, 1.10),
            Map.entry(ModDimensions.SPACE_LEVEL_KEY, 0.0)
    );

    private PlanetGravity() {
    }

    /**
     * @return 该维度相对主世界的重力倍率；非本模组维度返回 1.0
     */
    public static double getMultiplier(Level level) {
        return level == null ? DEFAULT_MULTIPLIER : getMultiplier(level.dimension());
    }

    /**
     * @return 该维度相对主世界的重力倍率；非本模组维度返回 1.0
     */
    public static double getMultiplier(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return DEFAULT_MULTIPLIER;
        }
        return MULTIPLIERS.getOrDefault(dimension, DEFAULT_MULTIPLIER);
    }
}
