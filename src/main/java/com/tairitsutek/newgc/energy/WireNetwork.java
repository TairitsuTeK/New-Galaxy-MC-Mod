package com.tairitsutek.newgc.energy;

import com.tairitsutek.newgc.block.WireBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 一个连通的电线网络。
 *
 * <p>网络本身不储存能量，只负责在“可抽取的来源”和“可接收的目标”之间搬运能量。
 * 这样发电机不会把电倒灌回其他机器，耗能机器也不会被电线反向抽空。</p>
 */
public final class WireNetwork {

    private static final int MAX_WIRES = 4096;
    private static final int RESCAN_INTERVAL = 20;
    private static final int TRANSFER_PER_WIRE_PER_TICK = 2_000;
    private static final int MAX_TRANSFER_PER_TICK = 20_000;

    private final ServerLevel level;
    private final WireNetworkManager manager;

    private Set<BlockPos> wires = new HashSet<>();
    private Set<Endpoint> endpoints = new HashSet<>();

    private long lastScan = Long.MIN_VALUE;
    private long lastTick = Long.MIN_VALUE;
    private boolean invalid;
    private int sinkCursor;

    WireNetwork(ServerLevel level, WireNetworkManager manager) {
        this.level = level;
        this.manager = manager;
    }

    Set<BlockPos> wires() {
        return wires;
    }

    boolean isInvalid() {
        return invalid;
    }

    void invalidate() {
        invalid = true;
    }

    /**
     * 从指定电线开始做 BFS，重建网络成员和端点。
     * 每 20 tick 自动重新扫描一次，以发现新放置的机器或其它模组的能量方块。
     */
    void rescan(BlockPos start) {
        BlockPos actualStart = findValidStart(start);
        Set<BlockPos> visited = new HashSet<>();
        Set<Endpoint> found = new HashSet<>();

        if (actualStart != null) {
            Deque<BlockPos> queue = new ArrayDeque<>();
            queue.add(actualStart);

            while (!queue.isEmpty() && visited.size() < MAX_WIRES) {
                BlockPos pos = queue.poll();
                if (!visited.add(pos)) {
                    continue;
                }
                if (!(level.getBlockState(pos).getBlock() instanceof WireBlock)) {
                    continue;
                }

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = pos.relative(dir);
                    if (level.getBlockState(neighbor).getBlock() instanceof WireBlock) {
                        if (!visited.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    } else {
                        IEnergyStorage storage = level.getCapability(
                                Capabilities.EnergyStorage.BLOCK,
                                neighbor,
                                dir.getOpposite()
                        );
                        if (storage != null) {
                            found.add(new Endpoint(neighbor, dir.getOpposite()));
                        }
                    }
                }
            }
        }

        for (BlockPos old : wires) {
            if (!visited.contains(old)) {
                manager.removeMapping(old, this);
            }
        }

        wires = visited;
        endpoints = found;
        manager.register(this, wires);
        lastScan = level.getGameTime();
    }

    private BlockPos findValidStart(BlockPos preferred) {
        if (preferred != null && level.getBlockState(preferred).getBlock() instanceof WireBlock) {
            return preferred;
        }
        for (BlockPos pos : wires) {
            if (level.getBlockState(pos).getBlock() instanceof WireBlock) {
                return pos;
            }
        }
        return null;
    }

    public void tick() {
        if (invalid) {
            return;
        }

        long now = level.getGameTime();
        if (now == lastTick) {
            return;
        }
        lastTick = now;

        if (wires.isEmpty()) {
            return;
        }
        if (lastScan == Long.MIN_VALUE || now - lastScan >= RESCAN_INTERVAL) {
            rescan(wires.iterator().next());
        }
        if (invalid || wires.isEmpty() || endpoints.isEmpty()) {
            return;
        }

        transfer();
    }

    private void transfer() {
        List<Endpoint> sources = new ArrayList<>();
        List<Endpoint> sinks = new ArrayList<>();
        long sinkSpace = 0L;

        for (Endpoint endpoint : endpoints) {
            IEnergyStorage storage = getStorage(endpoint);
            if (storage == null) {
                continue;
            }
            if (storage.canExtract()) {
                sources.add(endpoint);
            }
            if (storage.canReceive()) {
                int space = storage.receiveEnergy(Integer.MAX_VALUE, true);
                if (space > 0) {
                    sinks.add(endpoint);
                    sinkSpace += space;
                }
            }
        }

        if (sources.isEmpty() || sinks.isEmpty() || sinkSpace <= 0L) {
            return;
        }

        long capacity = Math.min(
                MAX_TRANSFER_PER_TICK,
                Math.min(sinkSpace, (long) TRANSFER_PER_WIRE_PER_TICK * Math.max(1, wires.size()))
        );
        int budget = (int) capacity;
        int collected = 0;

        for (Endpoint endpoint : sources) {
            if (collected >= budget) {
                break;
            }
            IEnergyStorage storage = getStorage(endpoint);
            if (storage == null || !storage.canExtract()) {
                continue;
            }

            int available = storage.extractEnergy(Integer.MAX_VALUE, true);
            if (available <= 0) {
                continue;
            }

            int take = Math.min(budget - collected, available);
            collected += storage.extractEnergy(take, false);
        }

        if (collected <= 0) {
            return;
        }

        int remaining = collected;
        int attempts = 0;
        while (remaining > 0 && attempts < 4) {
            boolean moved = false;
            int sinkCount = sinks.size();
            for (int i = 0; i < sinkCount && remaining > 0; i++) {
                Endpoint endpoint = sinks.get((sinkCursor + i) % sinkCount);
                IEnergyStorage storage = getStorage(endpoint);
                if (storage == null || !storage.canReceive()) {
                    continue;
                }
                int accepted = storage.receiveEnergy(remaining, false);
                if (accepted > 0) {
                    remaining -= accepted;
                    moved = true;
                }
            }
            sinkCursor = (sinkCursor + 1) % sinkCount;
            if (!moved) {
                break;
            }
            attempts++;
        }

        // 极端情况下（sink 状态在模拟后变化），把剩余能量退回可接收的来源，避免凭空消失。
        if (remaining > 0) {
            for (Endpoint endpoint : sources) {
                if (remaining <= 0) {
                    break;
                }
                IEnergyStorage storage = getStorage(endpoint);
                if (storage != null && storage.canReceive()) {
                    remaining -= storage.receiveEnergy(remaining, false);
                }
            }
        }
    }

    private IEnergyStorage getStorage(Endpoint endpoint) {
        return level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                endpoint.pos(),
                endpoint.side()
        );
    }

    private record Endpoint(BlockPos pos, Direction side) {
    }
}
