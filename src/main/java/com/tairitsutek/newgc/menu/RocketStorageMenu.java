package com.tairitsutek.newgc.menu;

import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 火箭储物舱：9 格货物 + 玩家背包。
 * <p>
 * 发射时拆下的 2x2 发射平台会被自动放进这里，玩家也可以存放其它物资。
 */
public class RocketStorageMenu extends AbstractContainerMenu {

    private final Container cargo;

    public RocketStorageMenu(int id, Inventory playerInv, Container cargo) {
        super(ModMenus.ROCKET_STORAGE.get(), id);
        checkContainerSize(cargo, RocketEntity.CARGO_SIZE);
        this.cargo = cargo;
        cargo.startOpen(playerInv.player);

        for (int col = 0; col < RocketEntity.CARGO_SIZE; col++) {
            addSlot(new Slot(cargo, col, 8 + col * 18, 20));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public RocketStorageMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, new SimpleContainer(RocketEntity.CARGO_SIZE));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < RocketEntity.CARGO_SIZE) {
            if (!this.moveItemStackTo(stack, RocketEntity.CARGO_SIZE, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, RocketEntity.CARGO_SIZE, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return result;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.cargo.stopOpen(player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
