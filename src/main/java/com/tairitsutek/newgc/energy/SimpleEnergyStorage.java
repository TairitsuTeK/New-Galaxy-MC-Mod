package com.tairitsutek.newgc.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public class SimpleEnergyStorage extends EnergyStorage {

    public SimpleEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public int generate(int amount) {
        int accepted = Math.min(capacity - energy, amount);
        energy += accepted;
        return accepted;
    }

    public void setEnergy(int value) {
        this.energy = Math.max(0, Math.min(capacity, value));
    }
}
