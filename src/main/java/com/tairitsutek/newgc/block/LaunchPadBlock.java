package com.tairitsutek.newgc.block;

import com.mojang.serialization.MapCodec;
import com.tairitsutek.newgc.block.entity.LaunchPadBlockEntity;
import com.tairitsutek.newgc.block.entity.ModBlockEntities;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.entity.RocketLaunchService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 发射平台。
 * <p>
 * 平台只有 1/4 格高，必须摆放成 <b>2x2</b> 多方块结构才能放置火箭。
 * 结构以最小角（x、z 最小的一块）作为控制器，火箭停靠在四块平台的中心。
 * <p>
 * 使用方式：
 * <ol>
 *     <li>摆放 2x2 发射平台；</li>
 *     <li>手持火箭右键平台 → 火箭停靠在平台中心；</li>
 *     <li>右键火箭登乘，进入后按空格发射；</li>
 *     <li>潜行右键平台可收回火箭。</li>
 * </ol>
 */
public class LaunchPadBlock extends BaseEntityBlock {

    public static final MapCodec<LaunchPadBlock> CODEC = simpleCodec(LaunchPadBlock::new);

    /** 平台高度（格）：4/16。 */
    public static final double PAD_HEIGHT = 0.25D;
    /** 多方块结构边长。 */
    public static final int MULTIBLOCK_SIZE = 2;
    /** 平台中心相对最小角的 X/Z 偏移。 */
    public static final double PAD_CENTER_OFFSET = MULTIBLOCK_SIZE / 2.0D;

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

    public LaunchPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    //region 薄板形状
    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                           @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                                    @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
    //endregion

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                 @NotNull BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.LAUNCH_PAD.get(), LaunchPadBlockEntity::tick);
    }
    //region 2x2 多方块
    /**
     * 判断坐标处是否为发射平台方块。
     */
    public static boolean isPad(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(MachinesBlock.LAUNCH_PAD.get());
    }

    /**
     * 判断以 origin 为最小角的 2x2 结构是否完整。
     */
    public static boolean isCompletePad(BlockGetter level, BlockPos origin) {
        for (int dx = 0; dx < MULTIBLOCK_SIZE; dx++) {
            for (int dz = 0; dz < MULTIBLOCK_SIZE; dz++) {
                if (!isPad(level, origin.offset(dx, 0, dz))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 查找包含 pos 的完整 2x2 结构的最小角；结构不完整时返回 null。
     */
    @Nullable
    public static BlockPos findController(BlockGetter level, BlockPos pos) {
        BlockPos best = null;
        for (int dx = -(MULTIBLOCK_SIZE - 1); dx <= 0; dx++) {
            for (int dz = -(MULTIBLOCK_SIZE - 1); dz <= 0; dz++) {
                BlockPos origin = pos.offset(dx, 0, dz);
                if (!isCompletePad(level, origin)) {
                    continue;
                }
                best = pickLowerCorner(best, origin);
            }
        }
        return best;
    }

    /**
     * 在方块被移除的瞬间定位它所属的 2x2 结构最小角。
     * <p>
     * 与 {@link #findController} 的区别是：本方法会忽略 {@code pos} 自身的方块状态，
     * 因此可以在破坏平台的回调中继续找到控制器。
     */
    @Nullable
    public static BlockPos findControllerIgnoring(BlockGetter level, BlockPos pos) {
        BlockPos best = null;
        for (int dx = -(MULTIBLOCK_SIZE - 1); dx <= 0; dx++) {
            for (int dz = -(MULTIBLOCK_SIZE - 1); dz <= 0; dz++) {
                BlockPos origin = pos.offset(dx, 0, dz);
                if (!isCompleteIgnoring(level, origin, pos)) {
                    continue;
                }
                best = pickLowerCorner(best, origin);
            }
        }
        return best;
    }

    private static boolean isCompleteIgnoring(BlockGetter level, BlockPos origin, BlockPos ignored) {
        for (int dx = 0; dx < MULTIBLOCK_SIZE; dx++) {
            for (int dz = 0; dz < MULTIBLOCK_SIZE; dz++) {
                BlockPos check = origin.offset(dx, 0, dz);
                if (check.equals(ignored)) {
                    continue;
                }
                if (!isPad(level, check)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    private static BlockPos pickLowerCorner(@Nullable BlockPos current, BlockPos candidate) {
        if (current == null) {
            return candidate;
        }
        if (candidate.getX() < current.getX()) {
            return candidate;
        }
        if (candidate.getX() == current.getX() && candidate.getZ() < current.getZ()) {
            return candidate;
        }
        return current;
    }

    /**
     * 判断坐标是否为所属 2x2 结构的控制器（最小角）。
     */
    public static boolean isController(BlockGetter level, BlockPos pos) {
        return pos.equals(findController(level, pos));
    }

    /**
     * 取得负责该平台的控制器方块实体；结构不完整时退回点击位置自身的方块实体。
     */
    @Nullable
    public static LaunchPadBlockEntity getControllerEntity(BlockGetter level, BlockPos pos) {
        BlockPos controller = findController(level, pos);
        if (controller != null && level.getBlockEntity(controller) instanceof LaunchPadBlockEntity pad) {
            return pad;
        }
        return level.getBlockEntity(pos) instanceof LaunchPadBlockEntity pad ? pad : null;
    }
    //endregion
    //region 交互
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state,
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        int rocketTier = stack.is(MachinesBlock.TIER3_ROCKET_ITEM.get()) ? 3
                : stack.is(MachinesBlock.TIER2_ROCKET_ITEM.get()) ? 2
                : stack.is(MachinesBlock.ROCKET_ITEM.get()) ? 1 : 0;
        if (rocketTier == 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (findController(level, pos) == null) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.rocket.need_2x2"), true);
            }
            return ItemInteractionResult.FAIL;
        }

        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }

        LaunchPadBlockEntity pad = getControllerEntity(level, pos);
        if (pad == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!pad.dock(rocketTier)) {
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.occupied"), true);
            return ItemInteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(
                Component.translatable("message.newgc.rocket.docked"), true);
        return ItemInteractionResult.CONSUME;
    }
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
                                                        @NotNull BlockPos pos, @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        LaunchPadBlockEntity pad = getControllerEntity(level, pos);
        if (pad == null) {
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive()) {
            if (pad.undock()) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.rocket.removed"), true);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        RocketEntity rocket = pad.findRocket();
        if (rocket == null) {
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.empty"), true);
            return InteractionResult.CONSUME;
        }

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            RocketLaunchService.LaunchResult result =
                    RocketLaunchService.request(serverPlayer, rocket);
            if (result == RocketLaunchService.LaunchResult.LAUNCHED) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.newgc.rocket.countdown",
                                RocketEntity.COUNTDOWN_TICKS / 20), true);
            } else if (result == RocketLaunchService.LaunchResult.FAILED) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.newgc.rocket.need_pad"), true);
            }
        }
        return InteractionResult.CONSUME;
    }
    //endregion

    //region 移除
    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            LaunchPadBlockEntity pad = null;

            // 破坏任意一块平台都要收回整枚火箭，因此优先在控制器上清理
            BlockPos controller = findControllerIgnoring(level, pos);
            if (controller != null && level.getBlockEntity(controller) instanceof LaunchPadBlockEntity found) {
                pad = found;
            }
            if (pad == null && level.getBlockEntity(pos) instanceof LaunchPadBlockEntity self) {
                pad = self;
            }
            if (pad != null) {
                pad.onPadRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    //endregion
}
