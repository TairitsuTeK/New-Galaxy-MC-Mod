package com.tairitsutek.newgc.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 每个服务端维度一份电线网络缓存。
 */
public final class WireNetworkManager {

    private static final Map<ResourceKey<Level>, WireNetworkManager> MANAGERS = new HashMap<>();

    private final Map<BlockPos, WireNetwork> byPos = new HashMap<>();

    private WireNetworkManager() {
    }

    public static WireNetworkManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level.dimension(), ignored -> new WireNetworkManager());
    }

    public static void clear(ServerLevel level) {
        MANAGERS.remove(level.dimension());
    }

    public static void clearAll() {
        MANAGERS.clear();
    }

    public WireNetwork getOrCreate(ServerLevel level, BlockPos pos) {
        WireNetwork network = byPos.get(pos);
        if (network != null && !network.isInvalid()) {
            return network;
        }

        network = new WireNetwork(level, this);
        network.rescan(pos);
        return network;
    }

    void register(WireNetwork network, Collection<BlockPos> positions) {
        for (BlockPos pos : positions) {
            WireNetwork old = byPos.get(pos);
            if (old != null && old != network) {
                invalidate(old);
            }
        }
        for (BlockPos pos : positions) {
            byPos.put(pos, network);
        }
    }

    void removeMapping(BlockPos pos, WireNetwork network) {
        byPos.remove(pos, network);
    }

    /**
     * 电线被放置/移除，或邻近方块发生变化时调用，使周围旧网络失效。
     */
    public void invalidateAround(ServerLevel level, BlockPos pos) {
        Set<WireNetwork> affected = new HashSet<>();
        collect(affected, pos);
        for (Direction dir : Direction.values()) {
            collect(affected, pos.relative(dir));
        }
        for (WireNetwork network : affected) {
            invalidate(network);
        }
    }

    public void remove(ServerLevel level, BlockPos pos) {
        invalidateAround(level, pos);
    }

    private void collect(Set<WireNetwork> affected, BlockPos pos) {
        WireNetwork network = byPos.get(pos);
        if (network != null) {
            affected.add(network);
        }
    }

    private void invalidate(WireNetwork network) {
        if (network.isInvalid()) {
            return;
        }
        network.invalidate();
        for (BlockPos pos : network.wires()) {
            byPos.remove(pos, network);
        }
    }
}
