package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.block.LaunchPadBlock;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.entity.ModEntities;
import com.tairitsutek.newgc.entity.RocketDestinations;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 发射平台方块实体。
 * <p>
 * 多个方块共同组成 2x2 结构，火箭由结构最小角（控制器）上的方块实体统一管理，
 * 火箭本体的落点固定为 2x2 平台的中心。
 */
public class LaunchPadBlockEntity extends BlockEntity {

    @Nullable
    private UUID rocketId;
    /** 红石自动点火后的冷却，避免信号持续时反复点火。 */
    private int cooldown;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCH_PAD.get(), pos, state);
    }

    //region Tick
    public static void tick(Level level, BlockPos pos, BlockState state, LaunchPadBlockEntity pad) {
        if (level.isClientSide()) {
            return;
        }
        if (pad.cooldown > 0) {
            pad.cooldown--;
        }
        // 只有 2x2 控制器负责红石点火，避免四块平台各点一次
        if (!LaunchPadBlock.isController(level, pos)) {
            return;
        }
        if (pad.cooldown == 0 && level.hasNeighborSignal(pos) && pad.tryIgnite()) {
            pad.cooldown = 40;
        }
    }
    //endregion

    //region 结构位置
    /**
     * 该方块所属 2x2 结构的最小角；结构不完整时退回自身坐标。
     */
    public BlockPos getAnchor() {
        BlockPos controller = LaunchPadBlock.findController(this.level, this.worldPosition);
        return controller != null ? controller : this.worldPosition;
    }

    /** 平台中心的 X 坐标。 */
    private double centerX(BlockPos anchor) {
        return anchor.getX() + LaunchPadBlock.PAD_CENTER_OFFSET;
    }

    /** 平台中心的 Z 坐标。 */
    private double centerZ(BlockPos anchor) {
        return anchor.getZ() + LaunchPadBlock.PAD_CENTER_OFFSET;
    }
    //endregion

    //region 火箭查找
    /**
     * 查找停靠在本平台上的火箭。优先使用缓存的 UUID，丢失后再在结构范围内搜索。
     */
    @Nullable
    public RocketEntity findRocket() {
        return this.locateRocket(this.getAnchor());
    }

    /**
     * 以 anchor（2x2 最小角）为基准查找火箭，不要求结构当前仍然完整。
     * <p>
     * 破坏平台时需要通过它清理火箭，因此搜索范围覆盖整个结构并向上留出火箭高度。
     */
    @Nullable
    public RocketEntity locateRocket(BlockPos anchor) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (this.rocketId != null) {
            Entity entity = serverLevel.getEntity(this.rocketId);
            if (entity instanceof RocketEntity rocket && rocket.isAlive()) {
                return rocket;
            }
            this.rocketId = null;
        }

        AABB area = new AABB(
                anchor.getX() - 1.0D, anchor.getY() - 1.0D, anchor.getZ() - 1.0D,
                anchor.getX() + LaunchPadBlock.MULTIBLOCK_SIZE + 1.0D,
                anchor.getY() + 10.0D,
                anchor.getZ() + LaunchPadBlock.MULTIBLOCK_SIZE + 1.0D);

        List<RocketEntity> nearby = serverLevel.getEntitiesOfClass(
                RocketEntity.class, area, Entity::isAlive);
        for (RocketEntity rocket : nearby) {
            this.rocketId = rocket.getUUID();
            return rocket;
        }
        return null;
    }

    public boolean hasRocket() {
        return this.findRocket() != null;
    }
    //endregion
    //region 停靠与收回
    /**
     * 在 2x2 平台中心停靠一枚新火箭。
     *
     * @return 是否成功停靠（结构不完整或已有火箭时返回 false）
     */
    public boolean dock() {
        return this.dock(1);
    }

    public boolean dock(int tier) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }
        BlockPos anchor = this.getAnchor();
        if (!LaunchPadBlock.isCompletePad(serverLevel, anchor)) {
            return false;
        }
        if (this.locateRocket(anchor) != null) {
            return false;
        }

        RocketEntity rocket = ModEntities.ROCKET.get().create(serverLevel);
        if (rocket == null) {
            return false;
        }

        rocket.moveTo(this.centerX(anchor), anchor.getY() + LaunchPadBlock.PAD_HEIGHT,
                this.centerZ(anchor), 0.0F, 0.0F);
        rocket.setTier(tier);
        rocket.setLaunchPadPos(anchor);
        serverLevel.addFreshEntity(rocket);

        this.rocketId = rocket.getUUID();
        this.setChanged();
        return true;
    }

    /**
     * 收回停靠中的火箭，并把火箭物品退还到平台中心上方。
     */
    public boolean undock() {
        BlockPos anchor = this.getAnchor();
        RocketEntity rocket = this.locateRocket(anchor);
        if (rocket == null || rocket.getState() != RocketEntity.STATE_IDLE) {
            return false;
        }

        ItemStack drop = new ItemStack(rocket.getDropItem());
        rocket.discard();
        if (this.level != null) {
            Block.popResource(this.level, anchor.east().south(), drop);
        }
        this.rocketId = null;
        this.setChanged();
        return true;
    }

    /**
     * 平台被破坏时清理火箭，避免留下悬空的火箭实体。
     * <p>
     * 点火后火箭会自行拆掉平台并把平台收进储物舱，此时火箭已经处于非 IDLE 状态，
     * 不能被这里当成“故障火箭”清理掉。
     */
    public void onPadRemoved() {
        RocketEntity rocket = this.locateRocket(this.worldPosition);
        if (rocket != null && rocket.getState() == RocketEntity.STATE_IDLE) {
            BlockPos drop = rocket.blockPosition();
            ItemStack item = new ItemStack(rocket.getDropItem());
            rocket.discard();
            if (this.level != null) {
                Block.popResource(this.level, drop, item);
            }
        }
        this.rocketId = null;
    }
    //endregion

    //region 点火
    public boolean tryIgnite() {
        RocketEntity rocket = this.findRocket();
        if (rocket == null || !rocket.isReadyToLaunch()) {
            return false;
        }

        // 红石没有聊天框确认途径，因此禁止无目标漂流发射，并提醒附近玩家手动确认。
        if (!RocketDestinations.hasAnyTarget(
                rocket.level().dimension(), rocket.getTier())) {
            this.cooldown = 40;
            Player nearest = null;
            double bestDistance = 16.0D * 16.0D;
            for (Player player : this.level.players()) {
                double distance = player.distanceToSqr(
                        this.worldPosition.getX() + 0.5D,
                        this.worldPosition.getY() + 0.5D,
                        this.worldPosition.getZ() + 0.5D);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = player;
                }
            }
            if (nearest != null) {
                nearest.displayClientMessage(
                        Component.translatable("message.newgc.rocket.redstone_confirm_required"),
                        true);
            }
            return false;
        }

        if (!rocket.ignite()) {
            return false;
        }
        this.setChanged();
        return true;
    }
    //endregion

    //region 存档
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.rocketId != null) {
            tag.putUUID("Rocket", this.rocketId);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.rocketId = tag.hasUUID("Rocket") ? tag.getUUID("Rocket") : null;
    }
    //endregion
}
