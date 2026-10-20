package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.SimpleEnergyStorage;
import com.tairitsutek.newgc.menu.SolidFuelGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class SolidFuelGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int CAPACITY = 100_000;
    public static final int GENERATE_PER_TICK = 40;
    // 发电机向外输出的最大速度
    public static final int EXTRACT_PER_TICK = 1000;

    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int syncCooldown = 0;

    private final SimpleEnergyStorage energy =
            new SimpleEnergyStorage(CAPACITY, CAPACITY, EXTRACT_PER_TICK);

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return getBurnTime(stack) > 0;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> maxBurnTime = value;
                case 2 -> energy.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public SolidFuelGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLID_FUEL_GENERATOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() { return energy; }
    public ItemStackHandler getItems() { return items; }
    public ContainerData getData() { return data; }
    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }
    public int getBurnTime() { return burnTime; }
    public int getMaxBurnTime() { return maxBurnTime; }

    // ==================== 每 tick ====================
    public void tick() {
        if (level == null || level.isClientSide()) return;

        boolean changed = false;
        boolean energyFull = energy.getEnergyStored() >= energy.getMaxEnergyStored();

        // 1. 燃烧并发电
        if (burnTime > 0 && !energyFull) {
            burnTime--;
            energy.generate(GENERATE_PER_TICK);
            changed = true;
        }

        // 2. 尝试消耗燃料
        if (burnTime <= 0) {
            ItemStack fuel = items.getStackInSlot(0);
            int burn = getBurnTime(fuel);
            if (burn > 0) {
                burnTime = burn;
                maxBurnTime = burn;
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    items.setStackInSlot(0, ItemStack.EMPTY);
                }
                changed = true;
            }
        }

        // 3. 主动向外推送能量
        if (energy.getEnergyStored() > 0) {
            boolean pushed = false;
            for (Direction dir : Direction.values()) {
                if (energy.getEnergyStored() <= 0) break;

                BlockPos neighborPos = worldPosition.relative(dir);
                IEnergyStorage other = level.getCapability(
                        Capabilities.EnergyStorage.BLOCK,
                        neighborPos,
                        dir.getOpposite()
                );

                if (other != null && other.canReceive()) {
                    int available = energy.getEnergyStored();
                    int want = Math.min(EXTRACT_PER_TICK, available);

                    // 模拟推送
                    int sent = other.receiveEnergy(want, true);
                    if (sent > 0) {
                        sent = other.receiveEnergy(sent, false);
                        if (sent > 0) {
                            energy.extractEnergy(sent, false);
                            pushed = true;
                        }
                    }
                }
            }
            if (pushed) changed = true;
        }

        // 4. 状态同步
        if (changed) {
            setChanged();

            if (syncCooldown <= 0) {
                syncCooldown = 20;
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            } else {
                syncCooldown--;
            }
        }
    }

    public void drops() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level,
                        worldPosition.getX(),
                        worldPosition.getY(),
                        worldPosition.getZ(),
                        stack);
            }
        }
    }

    public static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(ItemTags.COALS)) return 1600;
        if (stack.is(ItemTags.LOGS)) return 300;
        if (stack.is(ItemTags.PLANKS)) return 300;
        if (stack.is(ItemTags.WOODEN_STAIRS)) return 300;
        if (stack.is(ItemTags.WOODEN_SLABS)) return 150;
        if (stack.is(ItemTags.SAPLINGS)) return 100;
        if (stack.is(ItemTags.WOOL)) return 100;
        if (stack.is(Items.BLAZE_ROD)) return 2400;
        if (stack.is(Items.LAVA_BUCKET)) return 20000;
        if (stack.is(Items.COAL_BLOCK)) return 16000;
        if (stack.is(Items.DRIED_KELP_BLOCK)) return 4001;
        if (stack.is(Items.STICK)) return 100;
        if (stack.is(Items.BAMBOO)) return 50;
        return 0;
    }

    // ==================== MenuProvider ====================
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.newgc.solid_fuel_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new SolidFuelGeneratorMenu(id, playerInv, this, data);
    }

    // ==================== NBT ====================
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.put("Items", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        energy.setEnergy(tag.getInt("Energy"));
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
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