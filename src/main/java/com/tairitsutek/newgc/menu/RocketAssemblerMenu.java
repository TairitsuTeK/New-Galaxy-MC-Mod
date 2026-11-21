package com.tairitsutek.newgc.menu;

import com.tairitsutek.newgc.block.entity.RocketAssemblerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.Nullable;

/**
 * 火箭组装台菜单：14 个按火箭外形排列的输入槽 + 输出槽 + 玩家背包。
 */
public class RocketAssemblerMenu extends AbstractContainerMenu {

    @Nullable
    private final RocketAssemblerBlockEntity blockEntity;
    private final ContainerData data;

    public RocketAssemblerMenu(int id, Inventory playerInv,
                               RocketAssemblerBlockEntity be, ContainerData data) {
        super(ModMenus.ROCKET_ASSEMBLER.get(), id);
        this.blockEntity = be;
        this.data = data;

        for (int slot = 0; slot < RocketAssemblerLayout.INPUT_SLOTS; slot++) {
            addSlot(new SlotItemHandler(be.getItems(), slot,
                    RocketAssemblerLayout.slotX(slot), RocketAssemblerLayout.slotY(slot)));
        }
        addSlot(new SlotItemHandler(be.getItems(), RocketAssemblerLayout.OUTPUT_SLOT,
                RocketAssemblerLayout.slotX(RocketAssemblerLayout.OUTPUT_SLOT),
                RocketAssemblerLayout.slotY(RocketAssemblerLayout.OUTPUT_SLOT)) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        addPlayerSlots(playerInv);
        addDataSlots(data);
    }

    public RocketAssemblerMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        super(ModMenus.ROCKET_ASSEMBLER.get(), id);
        this.blockEntity = null;
        this.data = new SimpleContainerData(1);
        ItemStackHandler dummy = new ItemStackHandler(
                RocketAssemblerBlockEntity.SLOT_COUNT);
        for (int slot = 0; slot < RocketAssemblerLayout.INPUT_SLOTS; slot++) {
            addSlot(new SlotItemHandler(dummy, slot,
                    RocketAssemblerLayout.slotX(slot), RocketAssemblerLayout.slotY(slot)));
        }
        addSlot(new SlotItemHandler(dummy, RocketAssemblerLayout.OUTPUT_SLOT,
                RocketAssemblerLayout.slotX(RocketAssemblerLayout.OUTPUT_SLOT),
                RocketAssemblerLayout.slotY(RocketAssemblerLayout.OUTPUT_SLOT)) {
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
                addSlot(new Slot(playerInv, col + row * 9 + 9,
                        8 + col * 18, 158 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 216));
        }
    }

    public boolean getCanAssemble() {
        return data.get(0) != 0;
    }

    /** 服务端执行组装；失败时把原因发到快捷栏。 */
    public boolean tryAssemble(ServerPlayer player) {
        if (blockEntity == null || !stillValid(player)) {
            return false;
        }
        if (blockEntity.assemble()) {
            return true;
        }
        player.displayClientMessage(
                Component.translatable("message.newgc.rocket_assembler.missing"), true);
        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();

        if (index == RocketAssemblerLayout.OUTPUT_SLOT
                || index < RocketAssemblerLayout.INPUT_SLOTS) {
            if (!this.moveItemStackTo(stack, RocketAssemblerLayout.SLOT_COUNT,
                    this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0,
                RocketAssemblerLayout.INPUT_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (blockEntity == null) {
            // 客户端菜单只保留槽位显示，真正的距离校验在服务端菜单执行。
            return true;
        }
        if (blockEntity.getLevel() != player.level()) {
            return false;
        }
        return player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}
