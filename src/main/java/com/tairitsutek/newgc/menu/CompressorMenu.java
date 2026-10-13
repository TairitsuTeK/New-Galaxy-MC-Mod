package com.tairitsutek.newgc.menu;

import com.tairitsutek.newgc.block.entity.CompressorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CompressorMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public CompressorMenu(int id, Inventory playerInv, CompressorBlockEntity be, ContainerData data) {
        super(ModMenus.COMPRESSOR.get(), id);
        this.data = data;
        addSlot(new SlotItemHandler(be.getItems(), 0, 56, 34));
        addSlot(new SlotItemHandler(be.getItems(), 1, 116, 34) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerSlots(playerInv);
        addDataSlots(data);
    }

    public CompressorMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        super(ModMenus.COMPRESSOR.get(), id);
        this.data = new SimpleContainerData(4);
        ItemStackHandler dummy = new ItemStackHandler(2);
        addSlot(new SlotItemHandler(dummy, 0, 56, 34));
        addSlot(new SlotItemHandler(dummy, 1, 116, 34) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
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

    public int getProgress() { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }
    public int getEnergyStored() { return data.get(2); }
    public int getMaxEnergyStored() { return data.get(3); }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == 0 || index == 1) {
            if (!this.moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
