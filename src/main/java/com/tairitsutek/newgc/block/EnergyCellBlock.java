package com.tairitsutek.newgc.block;

import com.mojang.serialization.MapCodec;
import com.tairitsutek.newgc.block.entity.EnergyCellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCellBlock extends BaseEntityBlock {

    public static final MapCodec<EnergyCellBlock> CODEC = simpleCodec(EnergyCellBlock::new);

    public EnergyCellBlock(Properties properties) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new EnergyCellBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level,
                                                         @NotNull BlockPos pos,
                                                         @NotNull Player player,
                                                         @NotNull BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EnergyCellBlockEntity cell) {
                player.openMenu(cell, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
