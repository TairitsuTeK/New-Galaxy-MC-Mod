package com.tairitsutek.newgc.space;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 太空维度中的恒星锚点。
 * <p>
 * 漂流起点会按发射星系放到对应恒星的坐标附近；客户端天空再按火箭当前
 * X/Z 位置寻找最近的恒星图标，而不是固定显示地球或出发星球。
 */
public final class SpaceLocations {

    public record Star(double x, double z, ResourceLocation icon) {
    }

    public static final Star SUN = new Star(
            0.0D, 0.0D, icon("sun_icon.png"));
    public static final Star ALPHA_CENTAURI = new Star(
            4096.0D, 0.0D, icon("alpha_centauri_a_star.png"));
    public static final Star PROXIMA_CENTAURI = new Star(
            8192.0D, 0.0D, icon("proxima_star.png"));

    private static final List<Star> STARS = List.of(
            SUN, ALPHA_CENTAURI, PROXIMA_CENTAURI);

    private SpaceLocations() {
    }

    private static ResourceLocation icon(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                NewGalaxyMain.MODID, "textures/gui/" + name);
    }

    /** 根据发射维度选择进入太空时的 X 锚点。 */
    public static double startX(ResourceKey<Level> origin) {
        if (origin != null && origin.equals(ModDimensions.PROXIMA_B_LEVEL_KEY)) {
            return PROXIMA_CENTAURI.x();
        }
        return SUN.x();
    }

    /** 按实际太空坐标寻找最近恒星。 */
    public static Star nearest(double x, double z) {
        Star nearest = SUN;
        double best = Double.MAX_VALUE;
        for (Star star : STARS) {
            double dx = x - star.x();
            double dz = z - star.z();
            double distance = dx * dx + dz * dz;
            if (distance < best) {
                best = distance;
                nearest = star;
            }
        }
        return nearest;
    }
}
