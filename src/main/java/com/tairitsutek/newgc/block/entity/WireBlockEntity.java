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

    public static final int CAPACITY = 5000;
    public static final int TRANSFER_PER_TICK = 500;

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

        for (Direction dir : Direction.values()) {
            if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) break;

            BlockPos neighborPos = worldPosition.relative(dir);
            IEnergyStorage other = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    neighborPos,
                    dir.getOpposite()
            );
            if (other == null || !other.canExtract()) continue;

            int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
            int want = Math.min(TRANSFER_PER_TICK, space);
            int got = other.extractEnergy(want, true);   // simulate
            if (got <= 0) continue;
            got = other.extractEnergy(got, false);
            if (got > 0) {
                energy.receiveEnergy(got, false);
                setChanged();
            }
        }

        if (energy.getEnergyStored() <= 0) return;

        for (Direction dir : Direction.values()) {
            if (energy.getEnergyStored() <= 0) break;

            BlockPos neighborPos = worldPosition.relative(dir);
            IEnergyStorage other = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    neighborPos,
                    dir.getOpposite()
            );
            if (other == null || !other.canReceive()) continue;

            int available = energy.getEnergyStored();
            int want = Math.min(TRANSFER_PER_TICK, available);
            int sent = other.receiveEnergy(want, true);  // simulate
            if (sent <= 0) continue;
            sent = other.receiveEnergy(sent, false);
            if (sent > 0) {
                energy.extractEnergy(sent, false);
                setChanged();
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
