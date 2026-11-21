package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.ExternalEnergyStorage;
import com.tairitsutek.newgc.energy.SimpleEnergyStorage;
import com.tairitsutek.newgc.menu.AlloySmelterMenu;
import com.tairitsutek.newgc.recipe.AlloyRecipeInput;
import com.tairitsutek.newgc.recipe.AlloySmeltingRecipe;
import com.tairitsutek.newgc.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

/**
 * 合金熔炉：把两种金属/材料在 FE 驱动下熔炼成合金。
 */
public class AlloySmelterBlockEntity extends BlockEntity implements MenuProvider {

    public static final int ENERGY_CAPACITY = 50_000;
    public static final int ENERGY_RECEIVE = 200;
    public static final int ENERGY_EXTRACT = 50_000;
    public static final int FE_PER_ALLOY = 5_000;
    public static final int ALLOY_TICKS = 200;

    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    private int progress = 0;

    private final SimpleEnergyStorage energy =
            new SimpleEnergyStorage(ENERGY_CAPACITY, ENERGY_RECEIVE, ENERGY_EXTRACT);

    private final IEnergyStorage externalEnergy =
            new ExternalEnergyStorage(energy, ExternalEnergyStorage.Mode.RECEIVE_ONLY);

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != INPUT_A_SLOT && slot != INPUT_B_SLOT) return false;
            if (stack.isEmpty() || level == null) return false;
            int otherSlot = slot == INPUT_A_SLOT ? INPUT_B_SLOT : INPUT_A_SLOT;
            ItemStack other = items.getStackInSlot(otherSlot);
            return other.isEmpty() || findRecipe(level, stack, other) != null;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> ALLOY_TICKS;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 2 -> energy.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public AlloySmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_SMELTER.get(), pos, state);
    }

    public ItemStackHandler getItems() { return items; }
    public IEnergyStorage getEnergyStorage() { return externalEnergy; }
    public ContainerData getData() { return data; }

    private static AlloySmeltingRecipe findRecipe(Level level, ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) return null;
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.ALLOY_SMELTING.get(),
                        new AlloyRecipeInput(first, second), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        ItemStack first = items.getStackInSlot(INPUT_A_SLOT);
        ItemStack second = items.getStackInSlot(INPUT_B_SLOT);
        AlloySmeltingRecipe recipe = findRecipe(level, first, second);
        boolean canSmelt = recipe != null
                && first.getCount() >= recipe.getInputCount()
                && second.getCount() >= recipe.getInputCount()
                && energy.getEnergyStored() >= FE_PER_ALLOY;
        boolean changed = false;

        if (canSmelt) {
            ItemStack result = recipe.getResultItem(level.registryAccess());
            ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
            boolean outputOk = output.isEmpty()
                    || (ItemStack.isSameItemSameComponents(output, result)
                    && output.getCount() + result.getCount() <= output.getMaxStackSize());

            if (outputOk) {
                progress++;
                if (progress >= ALLOY_TICKS) {
                    progress = 0;
                    energy.extractEnergy(FE_PER_ALLOY, false);
                    items.extractItem(INPUT_A_SLOT, recipe.getInputCount(), false);
                    items.extractItem(INPUT_B_SLOT, recipe.getInputCount(), false);
                    if (output.isEmpty()) {
                        items.setStackInSlot(OUTPUT_SLOT, result.copy());
                    } else {
                        output.grow(result.getCount());
                    }
                }
                changed = true;
            }
        }

        if (!canSmelt && progress != 0) {
            progress = 0;
            changed = true;
        }

        if (changed) setChanged();
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void drops() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
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

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.newgc.alloy_smelter");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new AlloySmelterMenu(id, playerInv, this, data);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        energy.setEnergy(tag.getInt("Energy"));
        progress = tag.getInt("Progress");
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
