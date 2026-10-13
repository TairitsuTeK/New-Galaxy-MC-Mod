package com.tairitsutek.newgc.menu;

import com.tairitsutek.newgc.block.entity.OxygenExtractorBlockEntity;
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

public class OxygenExtractorMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public OxygenExtractorMenu(int id, Inventory playerInv, OxygenExtractorBlockEntity be, ContainerData data) {
        super(ModMenus.OXYGEN_EXTRACTOR.get(), id);
        this.data = data;
        addSlot(new SlotItemHandler(be.getItems(), 0, 80, 35));
        addPlayerSlots(playerInv);
        addDataSlots(data);
    }

    public OxygenExtractorMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        super(ModMenus.OXYGEN_EXTRACTOR.get(), id);
        this.data = new SimpleContainerData(6);
        ItemStackHandler dummy = new ItemStackHandler(1);
        addSlot(new SlotItemHandler(dummy, 0, 80, 35));
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

    public int getCurrentRate() { return data.get(0); }
    public int getPlantMultiplier() { return data.get(1); }
    public int getEnergyStored() { return data.get(2); }
    public int getMaxEnergyStored() { return data.get(3); }
    public int getOxygenBuffer() { return data.get(4); }        // ★ 新增
    public int getOxygenBufferMax() { return data.get(5); }     // ★ 新增

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, 37, true)) return ItemStack.EMPTY;
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