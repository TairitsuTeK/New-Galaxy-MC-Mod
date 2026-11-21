package com.tairitsutek.newgc.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EnergyCellMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public EnergyCellMenu(int id, Inventory playerInv, ContainerData data) {
        super(ModMenus.ENERGY_CELL.get(), id);
        this.data = data;
        addPlayerSlots(playerInv);
        addDataSlots(data);
    }

    public EnergyCellMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        super(ModMenus.ENERGY_CELL.get(), id);
        this.data = new SimpleContainerData(2);
        addPlayerSlots(playerInv);
        addDataSlots(data);
    }

    private void addPlayerSlots(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public int getEnergyStored() { return data.get(0); }
    public int getMaxEnergyStored() { return data.get(1); }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
