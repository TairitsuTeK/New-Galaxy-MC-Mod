package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.SimpleEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

public class WireBlockEntity extends BlockEntity {

    public static final int CAPACITY = 10000;
    public static final int TRANSFER_PER_TICK = 2000; // 提高单次传输上限

    private final SimpleEnergyStorage energy =
            new SimpleEnergyStorage(CAPACITY, TRANSFER_PER_TICK, TRANSFER_PER_TICK);

    public WireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIRE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        // 1. 从相邻的发电机/其他电线接收能量
        // 注意：这里不加 canExtract 判断，直接让对面尝试 receive，如果对面是电线，它会自己处理
        for (Direction dir : Direction.values()) {
            if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) break;

            BlockPos neighborPos = worldPosition.relative(dir);
            IEnergyStorage other = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    neighborPos,
                    dir.getOpposite()
            );

            // 如果对方是输出端（比如发电机），就抽它的电
            if (other != null && other.canExtract()) {
                int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
                int want = Math.min(TRANSFER_PER_TICK, space);
                int got = other.extractEnergy(want, false);
                if (got > 0) {
                    energy.receiveEnergy(got, false);
                    setChanged();
                }
            }
        }

        // 2. 把自身的能量推给相邻的机器
        if (energy.getEnergyStored() <= 0) return;

        for (Direction dir : Direction.values()) {
            if (energy.getEnergyStored() <= 0) break;

            BlockPos neighborPos = worldPosition.relative(dir);
            IEnergyStorage other = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    neighborPos,
                    dir.getOpposite()
            );

            if (other != null && other.canReceive()) {
                int available = energy.getEnergyStored();
                int want = Math.min(TRANSFER_PER_TICK, available);

                // 模拟推送，看对方能收多少
                int sent = other.receiveEnergy(want, true);
                if (sent > 0) {
                    // 正式推送
                    sent = other.receiveEnergy(sent, false);
                    if (sent > 0) {
                        energy.extractEnergy(sent, false);
                        setChanged();
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("Energy"));
    }
}