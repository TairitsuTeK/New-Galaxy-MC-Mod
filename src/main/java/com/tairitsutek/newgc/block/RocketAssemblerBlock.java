package com.tairitsutek.newgc.block;

import com.mojang.serialization.MapCodec;
import com.tairitsutek.newgc.block.entity.ModBlockEntities;
import com.tairitsutek.newgc.block.entity.RocketAssemblerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
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
 * TBMK 工作台：3 格高、带加长机械臂的黑灰色组装工作站。
 */
public class RocketAssemblerBlock extends BaseEntityBlock {

    public static final MapCodec<RocketAssemblerBlock> CODEC =
            simpleCodec(RocketAssemblerBlock::new);

    /** 工作台模型高 3 格，碰撞箱同步覆盖整个机械臂框架。 */
    private static final VoxelShape SHAPE =
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 48.0D, 16.0D);

    public RocketAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state,
                                           @NotNull BlockGetter level,
                                           @NotNull BlockPos pos,
                                           @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state,
                                                     @NotNull BlockGetter level,
                                                     @NotNull BlockPos pos,
                                                     @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        // 三格高结构由 RocketAssemblerRenderer 绘制，默认方块模型只用于物品/粒子。
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RocketAssemblerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.ROCKET_ASSEMBLER.get(),
                (lvl, pos, st, be) -> be.tick());
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level,
                                                        @NotNull BlockPos pos,
                                                        @NotNull Player player,
                                                        @NotNull BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketAssemblerBlockEntity assembler) {
                player.openMenu(assembler, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state,
                                                 @NotNull Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketAssemblerBlockEntity assembler) {
                assembler.drops();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketAssemblerBlockEntity assembler) {
                assembler.drops();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
