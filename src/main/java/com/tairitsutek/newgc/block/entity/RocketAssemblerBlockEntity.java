package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.menu.RocketAssemblerLayout;
import com.tairitsutek.newgc.menu.RocketAssemblerMenu;
import com.tairitsutek.newgc.recipe.ModRecipes;
import com.tairitsutek.newgc.recipe.RocketAssemblyRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * TBMK 工作台方块实体。
 * <p>
 * 14 个固定输入槽（鼻锥、8 块重型合金板、引擎、4 个助推器）+ 1 个输出槽。
 * 按下“组装”后立即消耗配方材料，不消耗 FE。
 */
public class RocketAssemblerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOTS = RocketAssemblerLayout.INPUT_SLOTS;
    public static final int OUTPUT_SLOT = RocketAssemblerLayout.OUTPUT_SLOT;
    public static final int SLOT_COUNT = RocketAssemblerLayout.SLOT_COUNT;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                canAssemble = computeCanAssemble();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < INPUT_SLOTS && !stack.isEmpty();
        }
    };

    private boolean canAssemble;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? (canAssemble ? 1 : 0) : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                canAssemble = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public RocketAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ROCKET_ASSEMBLER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public ContainerData getData() {
        return data;
    }

    public boolean canAssembleNow() {
        return computeCanAssemble();
    }

    /** 从当前 14 个固定槽位寻找匹配的组装配方。 */
    private RocketAssemblyRecipe findRecipe() {
        if (level == null) {
            return null;
        }
        List<ItemStack> stacks = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            stacks.add(items.getStackInSlot(i).copy());
        }
        CraftingInput input = CraftingInput.of(INPUT_SLOTS, 1, stacks);
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.ROCKET_ASSEMBLY.get(), input, level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private boolean computeCanAssemble() {
        RocketAssemblyRecipe recipe = findRecipe();
        if (recipe == null || level == null) {
            return false;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty()
                || (ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize());
    }

    /**
     * 执行一次组装：消耗图形中每个非空格子 1 个物品，并把结果放进输出槽。
     */
    public boolean assemble() {
        if (level == null || level.isClientSide()) {
            return false;
        }
        RocketAssemblyRecipe recipe = findRecipe();
        if (recipe == null || !computeCanAssemble()) {
            return false;
        }

        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            items.extractItem(slot, 1, false);
        }

        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        level.playSound(null, worldPosition, SoundEvents.ANVIL_USE,
                SoundSource.BLOCKS, 1.0F, 0.8F);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public void tick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (level.getGameTime() % 10 == 0) {
            boolean current = computeCanAssemble();
            if (current != canAssemble) {
                canAssemble = current;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public void drops() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        stack.copy());
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    // ==================== MenuProvider ====================
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.newgc.rocket_assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv,
                                            @NotNull Player player) {
        return new RocketAssemblerMenu(id, playerInv, this, data);
    }

    // ==================== NBT / 同步 ====================
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putBoolean("CanAssemble", canAssemble);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        canAssemble = tag.getBoolean("CanAssemble");
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
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }
}
