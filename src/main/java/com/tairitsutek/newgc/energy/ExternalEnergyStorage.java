package com.tairitsutek.newgc.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 对外暴露的能量能力包装器。
 *
 * <p>机器内部仍然使用完整的 {@link SimpleEnergyStorage}（可以内部抽取能量用于加工），
 * 但通过本包装器注册给能量网络时，可以限制为“只接收”或“只输出”，
 * 从而避免电线从耗能机器中反向抽电。</p>
 */
public final class ExternalEnergyStorage implements IEnergyStorage {

    public enum Mode {
        RECEIVE_ONLY,
        EXTRACT_ONLY,
        BOTH
    }

    private final IEnergyStorage delegate;
    private final Mode mode;

    public ExternalEnergyStorage(IEnergyStorage delegate, Mode mode) {
        this.delegate = delegate;
        this.mode = mode;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (mode == Mode.EXTRACT_ONLY) {
            return 0;
        }
        return delegate.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (mode == Mode.RECEIVE_ONLY) {
            return 0;
        }
        return delegate.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return delegate.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return delegate.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return mode != Mode.RECEIVE_ONLY && delegate.canExtract();
    }

    @Override
    public boolean canReceive() {
        return mode != Mode.EXTRACT_ONLY && delegate.canReceive();
    }
}
