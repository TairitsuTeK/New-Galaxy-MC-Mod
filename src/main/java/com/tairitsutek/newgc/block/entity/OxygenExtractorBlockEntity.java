package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.SimpleEnergyStorage;
import com.tairitsutek.newgc.event.oxygen.ModAttachments;
import com.tairitsutek.newgc.event.oxygen.OxygenData;
import com.tairitsutek.newgc.item.ToolsItem;
import com.tairitsutek.newgc.menu.OxygenExtractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OxygenExtractorBlockEntity extends BlockEntity implements MenuProvider {

    // 常量
    public static final int BASE_RATE = 1;
    public static final int FE_PER_OXYGEN = 2;
    public static final int FE_PER_TANK_DAMAGE = 2;
    public static final int ENERGY_CAPACITY = 50_000;
    public static final int ENERGY_RECEIVE = 200;
    public static final int ENERGY_EXTRACT = 50_000;
    public static final double PLAYER_RADIUS = 8.0;
    public static final int OXYGEN_BUFFER_MAX = 10_000;
    public static final int TANK_REFILL_PER_TICK = 5;

    // 状态字段
    private int oxygenBuffer = 0;

    private final SimpleEnergyStorage energy =
            new SimpleEnergyStorage(ENERGY_CAPACITY, ENERGY_RECEIVE, ENERGY_EXTRACT);

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getCurrentRate();
                case 1 -> getPlantMultiplier();
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> oxygenBuffer;
                case 5 -> OXYGEN_BUFFER_MAX;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 2) energy.setEnergy(value);
            if (index == 4) oxygenBuffer = value;
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    // 构造
    public OxygenExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OXYGEN_EXTRACTOR.get(), pos, state);
    }

    // 公共访问器
    public ItemStackHandler getItems() { return items; }
    public IEnergyStorage getEnergyStorage() { return energy; }

    // 静态判定
    public static boolean isPlant(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ItemTags.LEAVES)) return true;
        if (stack.is(ItemTags.SAPLINGS)) return true;
        if (stack.is(ItemTags.FLOWERS)) return true;
        if (stack.is(Items.KELP) || stack.is(Items.SEAGRASS)) return true;
        if (stack.is(Items.SHORT_GRASS) || stack.is(Items.FERN)) return true;
        if (stack.is(Items.WHEAT)) return true;
        if (stack.is(Items.SUGAR_CANE)) return true;
        return false;
    }

    public static boolean isOxygenTank(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ToolsItem.OXYGEN_TANK_BASIC.get())
                || stack.is(ToolsItem.OXYGEN_TANK_INTERMEDIATE.get())
                || stack.is(ToolsItem.OXYGEN_TANK_ADVANCED.get())
                || stack.is(ToolsItem.OXYGEN_TANK_ULTIMATE.get())
                || stack.is(ToolsItem.OXYGEN_TANK_SUPREME.get());
    }

    // 倍率与速率
    public int getPlantMultiplier() {
        ItemStack stack = items.getStackInSlot(0);
        if (stack.isEmpty()) return 1;
        if (stack.is(ItemTags.LEAVES)) return 3;
        if (stack.is(ItemTags.SAPLINGS)) return 5;
        if (stack.is(ItemTags.FLOWERS)) return 5;
        if (stack.is(Items.KELP) || stack.is(Items.SEAGRASS)) return 8;
        if (stack.is(Items.SHORT_GRASS) || stack.is(Items.FERN)) return 3;
        if (stack.is(Items.WHEAT)) return 4;
        if (stack.is(Items.SUGAR_CANE)) return 4;
        return 1;
    }

    public int getCurrentRate() {
        return BASE_RATE * getPlantMultiplier();
    }

    // tick
    public void tick() {
        if (level == null || level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        pullEnergy();

        ItemStack stack = items.getStackInSlot(0);
        boolean hasTank = isOxygenTank(stack) && stack.isDamageableItem() && stack.getDamageValue() > 0;
        boolean hasPlant = isPlant(stack);

        int rate = hasPlant ? getCurrentRate() : BASE_RATE;
        int feNeeded = rate * FE_PER_OXYGEN;

        boolean changed = false;

        // 产氧
        if (energy.getEnergyStored() >= feNeeded && oxygenBuffer < OXYGEN_BUFFER_MAX) {
            energy.extractEnergy(feNeeded, false);
            int space = OXYGEN_BUFFER_MAX - oxygenBuffer;
            int add = Math.min(rate, space);
            oxygenBuffer += add;
            changed = true;
        }

        // 充罐
        if (hasTank && oxygenBuffer > 0) {
            int damage = stack.getDamageValue();
            int refillMax = Math.min(damage, TANK_REFILL_PER_TICK);
            int canAfford = energy.getEnergyStored() / FE_PER_TANK_DAMAGE;
            int refill = Math.min(refillMax, Math.min(oxygenBuffer, canAfford));

            if (refill > 0) {
                stack.setDamageValue(damage - refill);
                oxygenBuffer -= refill;
                energy.extractEnergy(refill * FE_PER_TANK_DAMAGE, false);
                changed = true;
            }
        }

        // 给玩家
        if (!hasTank && oxygenBuffer > 0) {
            AABB area = new AABB(worldPosition).inflate(PLAYER_RADIUS);
            List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class, area);
            for (ServerPlayer player : players) {
                if (oxygenBuffer <= 0) break;
                OxygenData oxygenData = player.getData(ModAttachments.OXYGEN.get());
                if (oxygenData.getTankMax() > 0 && oxygenData.getTankOxygen() < oxygenData.getTankMax()) {
                    int space = oxygenData.getTankMax() - oxygenData.getTankOxygen();
                    int give = Math.min(oxygenBuffer, space);
                    oxygenData.setTankOxygen(oxygenData.getTankOxygen() + give);
                    oxygenBuffer -= give;
                    changed = true;
                }
            }
        }

        if (changed) setChanged();
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 能量拉取
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

    // 掉落
    public void drops() {
        if (level == null) return;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        stack.copy());
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    // 菜单
    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.newgc.oxygen_extractor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInv, @NotNull Player player) {
        return new OxygenExtractorMenu(id, playerInv, this, data);
    }

    // 存档
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("OxygenBuffer", oxygenBuffer);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        energy.setEnergy(tag.getInt("Energy"));
        oxygenBuffer = tag.getInt("OxygenBuffer");
    }

    // 网络同步
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
        loadAdditional(pkt.getTag(), registries);
    }
}