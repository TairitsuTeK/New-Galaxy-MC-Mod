package com.tairitsutek.newgc.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 太空漂流死亡后的返航点。
 * <p>
 * 火箭进入漂流维度前，把起飞星球维度和起飞平台中心坐标写入玩家持久数据。
 * 玩家在太空死亡时，{@link RespawnDimensionHandler} 会读取这组数据并在重生后
 * 把玩家送回起飞星球，而不是困在空维度中。
 */
public final class SpaceReturnData {

    private static final String KEY_DIMENSION = "newgc_space_return_dimension";
    private static final String KEY_X = "newgc_space_return_x";
    private static final String KEY_Y = "newgc_space_return_y";
    private static final String KEY_Z = "newgc_space_return_z";
    private static final String KEY_YAW = "newgc_space_return_yaw";
    private static final String KEY_PITCH = "newgc_space_return_pitch";

    private SpaceReturnData() {
    }

    public record Point(ResourceKey<Level> dimension, double x, double y, double z,
                        float yaw, float pitch) {
    }

    public static void store(ServerPlayer player, ResourceKey<Level> dimension,
                             double x, double y, double z, float yaw, float pitch) {
        CompoundTag data = player.getPersistentData();
        data.putString(KEY_DIMENSION, dimension.location().toString());
        data.putDouble(KEY_X, x);
        data.putDouble(KEY_Y, y);
        data.putDouble(KEY_Z, z);
        data.putFloat(KEY_YAW, yaw);
        data.putFloat(KEY_PITCH, pitch);
    }

    /** 读取但不清除；用于死亡克隆事件从旧玩家复制数据。 */
    @Nullable
    public static Point read(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY_DIMENSION)) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(data.getString(KEY_DIMENSION));
        if (id == null) {
            return null;
        }
        return new Point(
                ResourceKey.create(Registries.DIMENSION, id),
                data.getDouble(KEY_X),
                data.getDouble(KEY_Y),
                data.getDouble(KEY_Z),
                data.getFloat(KEY_YAW),
                data.getFloat(KEY_PITCH)
        );
    }

    @Nullable
    public static Point consume(Player player) {
        Point point = read(player);
        clear(player);
        return point;
    }

    public static void clear(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(KEY_DIMENSION);
        data.remove(KEY_X);
        data.remove(KEY_Y);
        data.remove(KEY_Z);
        data.remove(KEY_YAW);
        data.remove(KEY_PITCH);
    }
}
