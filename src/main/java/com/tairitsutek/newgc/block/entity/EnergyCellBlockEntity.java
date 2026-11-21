package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.SimpleEnergyStorage;
import com.tairitsutek.newgc.menu.EnergyCellMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

/**
 * 能量电池：只储存 FE，可作为电网的缓冲池。
 */
public class EnergyCellBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CAPACITY = 1_000_000;
    public static final int MAX_IO = 20_000;

    private final SimpleEnergyStorage energy =
            new SimpleEnergyStorage(CAPACITY, MAX_IO, MAX_IO);

    /**
     * 通过能力接口收放电时标记方块实体已修改，确保世界保存时不会丢失电量。
     */
    private final IEnergyStorage externalEnergy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = energy.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = energy.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) setChanged();
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energy.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) energy.setEnergy(value);
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public EnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CELL.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return externalEnergy; }
    public ContainerData getData() { return data; }
    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.newgc.energy_cell");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new EnergyCellMenu(id, playerInv, data);
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

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = pkt.getTag();
        loadAdditional(tag, registries);
    }
}
