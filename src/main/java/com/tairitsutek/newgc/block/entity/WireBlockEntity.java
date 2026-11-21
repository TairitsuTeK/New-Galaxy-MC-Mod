package com.tairitsutek.newgc.block.entity;

import com.tairitsutek.newgc.energy.WireNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 电线本身只是网络节点，不再自己储存/抽取能量。
 * 能量搬运由 {@link com.tairitsutek.newgc.energy.WireNetwork} 统一完成。
 */
public class WireBlockEntity extends BlockEntity {

    public WireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WIRE.get(), pos, state);
    }

    public void tick() {
        if (level instanceof ServerLevel serverLevel) {
            WireNetworkManager.get(serverLevel)
                    .getOrCreate(serverLevel, worldPosition)
                    .tick();
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            WireNetworkManager.get(serverLevel).remove(serverLevel, worldPosition);
        }
        super.setRemoved();
    }
}