package com.tairitsutek.newgc.entity;

import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 火箭等级与可达范围。
 * <p>
 * 每条“行星—卫星链”按轨道距离从内到外排序。一阶火箭只能沿链向内移动一格：
 * 行星 → 最近卫星；第一颗卫星 → 行星；后续卫星 → 内侧相邻卫星。
 * 二阶火箭在此基础上多覆盖一个外侧相邻天体：行星 → 前两颗卫星，
 * 第一颗卫星 → 行星 + 第二颗卫星，后续卫星 → 前后各一颗。
 * 三阶火箭覆盖当前行星系的全部卫星，并可前往轨道距离最近的其他行星；
 * 从卫星出发时还会包含母行星，保证玩家可以返回。
 * <p>
 * 没有可用目标时星图不会提供跃迁按钮，火箭会进入漂流状态。
 * 客户端星图与服务端跃迁校验共用同一套规则，避免改包绕过。
 */
public final class RocketDestinations {

    public static final int TIER_1 = 1;
    public static final int TIER_2 = 2;
    public static final int TIER_3 = 3;

    private RocketDestinations() {
    }

    /** 一个可跃迁目标。 */
    public record Target(ResourceKey<Level> dimension, String nameKey) {
    }

    private record Moon(ResourceKey<Level> dimension, String nameKey) {
    }

    private record Planet(ResourceKey<Level> dimension, String nameKey,
                          float orbitRadius, List<Moon> moons) {
    }

    private record Location(Planet planet, int moonIndex) {
    }

    private static Moon moon(ResourceKey<Level> dimension, String nameKey) {
        return new Moon(dimension, nameKey);
    }

    private static Planet planet(ResourceKey<Level> dimension, String nameKey,
                                 float orbitRadius, Moon... moons) {
        return new Planet(dimension, nameKey, orbitRadius, List.of(moons));
    }

    /**
     * 星图中的行星/矮行星顺序必须与这里的卫星顺序一致（由内到外）。
     * <p>
     * {@code orbitRadius} 与星图的 {@code orbitA} 保持一致，三阶火箭据此判断
     * “距离最近的行星”。Proxima b 属于另一个恒星系，因此不参与太阳系的行星距离比较。
     */
    private static final List<Planet> PLANETS = List.of(
            planet(ModDimensions.MERCURY_LEVEL_KEY, "gui.newgc.planet.mercury", 56f),
            planet(ModDimensions.VENUS_LEVEL_KEY, "gui.newgc.planet.venus", 77f),
            planet(Level.OVERWORLD, "gui.newgc.planet.earth", 90f,
                    moon(ModDimensions.MOON_LEVEL_KEY, "gui.newgc.moon.moon")),
            planet(ModDimensions.MARS_LEVEL_KEY, "gui.newgc.planet.mars", 111f,
                    moon(ModDimensions.PHOBOS_LEVEL_KEY, "gui.newgc.moon.phobos"),
                    moon(ModDimensions.DEIMOS_LEVEL_KEY, "gui.newgc.moon.deimos")),
            planet(ModDimensions.CERES_LEVEL_KEY, "gui.newgc.planet.ceres", 152f),
            planet(ModDimensions.JUPITER_LEVEL_KEY, "gui.newgc.planet.jupiter", 205f,
                    moon(ModDimensions.IO_LEVEL_KEY, "gui.newgc.moon.io"),
                    moon(ModDimensions.EUROPA_LEVEL_KEY, "gui.newgc.moon.europa"),
                    moon(ModDimensions.GANYMEDE_LEVEL_KEY, "gui.newgc.moon.ganymede"),
                    moon(ModDimensions.CALLISTO_LEVEL_KEY, "gui.newgc.moon.callisto")),
            planet(ModDimensions.SATURN_LEVEL_KEY, "gui.newgc.planet.saturn", 279f,
                    moon(ModDimensions.TITAN_LEVEL_KEY, "gui.newgc.moon.titan")),
            planet(ModDimensions.URANUS_LEVEL_KEY, "gui.newgc.planet.uranus", 394f,
                    moon(ModDimensions.TITANIA_LEVEL_KEY, "gui.newgc.moon.titania"),
                    moon(ModDimensions.OBERON_LEVEL_KEY, "gui.newgc.moon.oberon")),
            planet(ModDimensions.NEPTUNE_LEVEL_KEY, "gui.newgc.planet.neptune", 494f,
                    moon(ModDimensions.TRITON_LEVEL_KEY, "gui.newgc.moon.triton")),
            planet(ModDimensions.PLUTO_LEVEL_KEY, "gui.newgc.planet.pluto", 560f,
                    moon(ModDimensions.CHARON_LEVEL_KEY, "gui.newgc.moon.charon")),
            planet(ModDimensions.MAKEMAKE_LEVEL_KEY, "gui.newgc.planet.makemake", 600f),
            planet(ModDimensions.HAUMEA_LEVEL_KEY, "gui.newgc.planet.haumea", 580f,
                    moon(ModDimensions.HIIAKA_LEVEL_KEY, "gui.newgc.moon.hiiaka")),
            planet(ModDimensions.ERIS_LEVEL_KEY, "gui.newgc.planet.eris", 660f),
            planet(ModDimensions.PROXIMA_B_LEVEL_KEY, "gui.newgc.planet.proxima_b", 0f)
    );

    private static Location locate(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return null;
        }
        for (Planet planet : PLANETS) {
            if (planet.dimension().equals(dimension)) {
                return new Location(planet, -1);
            }
            for (int i = 0; i < planet.moons().size(); i++) {
                if (planet.moons().get(i).dimension().equals(dimension)) {
                    return new Location(planet, i);
                }
            }
        }
        return null;
    }
    /**
     * 当前天体在给定火箭等级下允许前往的目标。
     *
     * @param from 当前维度
     * @param tier 火箭等级（一阶或二阶）
     */
    public static List<Target> allowedTargets(ResourceKey<Level> from, int tier) {
        Location location = locate(from);
        if (location == null) {
            return List.of();
        }

        Planet planet = location.planet();
        List<Moon> moons = planet.moons();

        if (tier >= TIER_3) {
            return tierThreeTargets(location);
        }

        List<Target> targets = new ArrayList<>(2);

        if (location.moonIndex() < 0) {
            // 行星/矮行星：一阶去最近卫星，二阶再增加第二颗卫星。
            if (!moons.isEmpty()) {
                targets.add(new Target(moons.get(0).dimension(), moons.get(0).nameKey()));
                if (tier >= TIER_2 && moons.size() > 1) {
                    targets.add(new Target(moons.get(1).dimension(), moons.get(1).nameKey()));
                }
            }
            return List.copyOf(targets);
        }

        int index = location.moonIndex();
        // 沿链向内一格：第一颗卫星回行星，后续卫星去上一颗卫星。
        if (index == 0) {
            targets.add(new Target(planet.dimension(), planet.nameKey()));
        } else {
            Moon inner = moons.get(index - 1);
            targets.add(new Target(inner.dimension(), inner.nameKey()));
        }

        // 二阶火箭额外覆盖外侧相邻卫星。
        if (tier >= TIER_2 && index + 1 < moons.size()) {
            Moon outer = moons.get(index + 1);
            targets.add(new Target(outer.dimension(), outer.nameKey()));
        }
        return List.copyOf(targets);
    }

    /**
     * 三阶航线：
     * <ul>
     *     <li>当前所在行星系中，轨道距离最近的其他行星；</li>
     *     <li>当前行星的全部卫星；</li>
     *     <li>当前在卫星时，额外包含母行星，保证可以返回。</li>
     * </ul>
     */
    private static List<Target> tierThreeTargets(Location location) {
        Planet planet = location.planet();
        List<Moon> moons = planet.moons();
        List<Target> targets = new ArrayList<>(moons.size() + 2);

        // “最近的行星”指当前行星系中的其他行星；卫星以母行星的轨道半径参与比较。
        Planet nearest = nearestOtherPlanet(planet);
        if (nearest != null) {
            targets.add(new Target(nearest.dimension(), nearest.nameKey()));
        }

        if (location.moonIndex() >= 0) {
            targets.add(new Target(planet.dimension(), planet.nameKey()));
        }

        for (int i = 0; i < moons.size(); i++) {
            if (i == location.moonIndex()) {
                continue;
            }
            Moon moon = moons.get(i);
            targets.add(new Target(moon.dimension(), moon.nameKey()));
        }
        return List.copyOf(targets);
    }

    /**
     * 按星图轨道半径寻找最近的其他行星。
     * <p>
     * Proxima b 位于另一个恒星系，不能通过太阳系内部航线抵达，因此不参与比较。
     */
    @Nullable
    private static Planet nearestOtherPlanet(Planet from) {
        if (from.orbitRadius() <= 0.0F) {
            return null;
        }
        Planet nearest = null;
        float bestDistance = Float.MAX_VALUE;
        for (Planet candidate : PLANETS) {
            if (candidate == from || candidate.orbitRadius() <= 0.0F) {
                continue;
            }
            float distance = Math.abs(candidate.orbitRadius() - from.orbitRadius());
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    /** 火箭等级对应的名称翻译键。 */
    public static String tierNameKey(int tier) {
        if (tier >= TIER_3) {
            return "gui.newgc.rocket.tier3";
        }
        return tier >= TIER_2 ? "gui.newgc.rocket.tier2" : "gui.newgc.rocket.tier1";
    }

    public static boolean canReach(ResourceKey<Level> target, ResourceKey<Level> from, int tier) {
        if (target == null) {
            return false;
        }
        for (Target allowed : allowedTargets(from, tier)) {
            if (allowed.dimension().equals(target)) {
                return true;
            }
        }
        return false;
    }

    /** 当前天体可前往目标的本地化名称，用于星图提示与服务端回执。 */
    public static List<String> targetNames(ResourceKey<Level> from, int tier) {
        return allowedTargets(from, tier).stream()
                .map(target -> Component.translatable(target.nameKey()).getString())
                .toList();
    }

    public static String targetSummary(ResourceKey<Level> from, int tier) {
        List<String> names = targetNames(from, tier);
        return names.isEmpty() ? "—" : String.join(" / ", names);
    }

    /** 服务端消息使用：保留 Component 参数，客户端会按自己的语言解析目标名称。 */
    public static Component targetSummaryComponent(ResourceKey<Level> from, int tier) {
        List<Target> targets = allowedTargets(from, tier);
        if (targets.isEmpty()) {
            return Component.literal("—");
        }
        MutableComponent summary = Component.empty();
        for (int i = 0; i < targets.size(); i++) {
            if (i > 0) {
                summary.append(Component.literal(" / "));
            }
            summary.append(Component.translatable(targets.get(i).nameKey()));
        }
        return summary;
    }

    public static Component nameComponent(ResourceKey<Level> dimension) {
        Location location = locate(dimension);
        if (location == null) {
            return Component.literal("—");
        }
        if (location.moonIndex() < 0) {
            return Component.translatable(location.planet().nameKey());
        }
        return Component.translatable(
                location.planet().moons().get(location.moonIndex()).nameKey());
    }

    public static String currentName(ResourceKey<Level> from) {
        Location location = locate(from);
        if (location == null) {
            return "—";
        }
        if (location.moonIndex() < 0) {
            return Component.translatable(location.planet().nameKey()).getString();
        }
        return Component.translatable(
                location.planet().moons().get(location.moonIndex()).nameKey()).getString();
    }

    public static String nameFor(ResourceKey<Level> dimension) {
        Location location = locate(dimension);
        if (location == null) {
            return "—";
        }
        if (location.moonIndex() < 0) {
            return Component.translatable(location.planet().nameKey()).getString();
        }
        return Component.translatable(
                location.planet().moons().get(location.moonIndex()).nameKey()).getString();
    }

    /** 当前天体所属的行星；卫星会返回其母行星。用于太空天空的最近行星贴图。 */
    @Nullable
    public static ResourceKey<Level> planetFor(ResourceKey<Level> dimension) {
        Location location = locate(dimension);
        return location == null ? null : location.planet().dimension();
    }

    public static boolean isKnown(ResourceKey<Level> dimension) {
        return locate(dimension) != null;
    }

    public static boolean hasAnyTarget(ResourceKey<Level> from, int tier) {
        return !allowedTargets(from, tier).isEmpty();
    }
}
