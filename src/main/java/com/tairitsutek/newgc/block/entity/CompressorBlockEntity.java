package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.SimpleEnergyStorage;
import com.tairitsutek.newgc.menu.CompressorMenu;
import com.tairitsutek.newgc.recipe.CompressorRecipe;
import com.tairitsutek.newgc.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class CompressorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int ENERGY_CAPACITY = 50_000;
    public static final int ENERGY_RECEIVE  = 200;
    public static final int ENERGY_EXTRACT  = 50_000;
    public static final int FE_PER_COMPRESS = 100;
    public static final int COMPRESS_TICKS  = 200;

    public static final int INPUT_SLOT  = 0;
    public static final int OUTPUT_SLOT = 1;

    private int progress = 0;

    private final SimpleEnergyStorage energy =
            new SimpleEnergyStorage(ENERGY_CAPACITY, ENERGY_RECEIVE, ENERGY_EXTRACT);

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != INPUT_SLOT) return false;
            if (stack.isEmpty() || level == null) return false;
            return findRecipe(level, stack) != null;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> COMPRESS_TICKS;
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

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPRESSOR.get(), pos, state);
    }

    // ==================== 公共方法 ====================
    public ItemStackHandler getItems() { return items; }
    public IEnergyStorage getEnergyStorage() { return energy; }
    public ContainerData getData() { return data; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return COMPRESS_TICKS; }

    private static CompressorRecipe findRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) return null;
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.COMPRESSING.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    // ==================== tick ====================
    public void tick() {
        if (level == null || level.isClientSide()) return;

        pullEnergy();

        ItemStack input = items.getStackInSlot(INPUT_SLOT);
        CompressorRecipe recipe = findRecipe(level, input);
        boolean canCompress = recipe != null
                && input.getCount() >= recipe.getInputCount()
                && energy.getEnergyStored() >= FE_PER_COMPRESS;

        boolean changed = false;

        if (canCompress) {
            ItemStack result = recipe.getResultItem(level.registryAccess());
            ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
            boolean outputOk = output.isEmpty()
                    || (ItemStack.isSameItemSameComponents(output, result)
                    && output.getCount() + result.getCount() <= output.getMaxStackSize());

            if (outputOk) {
                progress++;
                if (progress >= COMPRESS_TICKS) {
                    progress = 0;
                    energy.extractEnergy(FE_PER_COMPRESS, false);
                    items.extractItem(INPUT_SLOT, recipe.getInputCount(), false);
                    if (output.isEmpty()) {
                        items.setStackInSlot(OUTPUT_SLOT, result.copy());
                    } else {
                        output.grow(result.getCount());
                    }
                }
                changed = true;
            }
        }

        if (!canCompress && progress != 0) {
            progress = 0;
            changed = true;
        }

        if (changed) setChanged();
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void pullEnergy() {
        if (level == null) return;
        if (energy.getEnergyStored() >= energy.getMaxEnergyStored()) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(dir);
            IEnergyStorage other = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    neighbor,
                    dir.getOpposite()
            );
            if (other != null && other.canExtract()) {
                int space = energy.getMaxEnergyStored() - energy.getEnergyStored();
                int want = Math.min(ENERGY_RECEIVE, space);
                int got = other.extractEnergy(want, false);
                if (got > 0) {
                    energy.receiveEnergy(got, false);
                    setChanged();
                }
            }
        }
    }

    // ==================== 掉落物品 ====================
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

    // ==================== MenuProvider ====================
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.newgc.compressor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new CompressorMenu(id, playerInv, this, data);
    }

    // ==================== NBT ====================
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

    // ==================== 网络同步 ====================
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
    public void onDataPacket(@NotNull Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = pkt.getTag();
        loadAdditional(tag, registries);
    }
}
