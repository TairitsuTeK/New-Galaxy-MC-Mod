package com.tairitsutek.newgc.event.oxygen;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class OxygenData {

    public static final Codec<OxygenData> CODEC =
            Codec.INT.xmap(OxygenData::new, OxygenData::getOxygen);

    /** 第一管氧气（自身） */
    private int oxygen;
    /** 第二管氧气（氧气管扩展） */
    private int tankOxygen;
    /** 第二管上限 */
    private int tankMax;
    /** 伤害冷却 */
    private int damageCooldown = 0;

    /** AttachmentType.builder 要求的构造 */
    public OxygenData(IAttachmentHolder holder) {
        this.oxygen = OxygenConfig.BASE_MAX_OXYGEN;
        this.tankOxygen = 0;
        this.tankMax = OxygenConfig.TANK_MAX_OXYGEN;
    }

    /** Codec 要求的构造 */
    public OxygenData(int oxygen) {
        this.oxygen = Math.max(0, Math.min(OxygenConfig.BASE_MAX_OXYGEN, oxygen));
        this.tankOxygen = 0;
        this.tankMax = OxygenConfig.TANK_MAX_OXYGEN;
    }

    // ==================== 第一管 ====================
    public int getOxygen() {
        return oxygen;
    }

    public void setOxygen(int value) {
        this.oxygen = Math.max(0, Math.min(getBaseMax(), value));
    }

    public int getBaseMax() {
        return OxygenConfig.BASE_MAX_OXYGEN;
    }

    // ==================== 第二管 ====================
    public int getTankOxygen() {
        return tankOxygen;
    }

    public void setTankOxygen(int value) {
        this.tankOxygen = Math.max(0, Math.min(getTankMax(), value));
    }

    public int getTankMax() {
        return tankMax;
    }

    public void setTankMax(int value) {
        this.tankMax = Math.max(0, Math.min(OxygenConfig.ABSOLUTE_MAX_OXYGEN, value));
        if (this.tankOxygen > this.tankMax) {
            this.tankOxygen = this.tankMax;
        }
    }

    // ==================== 通用 ====================
    /** 总氧气 = 第一管 + 第二管 */
    public int getTotalOxygen() {
        return oxygen + tankOxygen;
    }

    /** 总上限 = 第一管上限 + 第二管上限 */
    public int getTotalMax() {
        return getBaseMax() + getTankMax();
    }

    /** 是否全满 */
    public boolean isFull() {
        return oxygen >= getBaseMax() && tankOxygen >= getTankMax();
    }

    /** 是否完全耗尽 */
    public boolean isEmpty() {
        return oxygen <= 0 && tankOxygen <= 0;
    }

    /** 第一管百分比 */
    public float getBasePercentage() {
        return (float) oxygen / getBaseMax();
    }

    /** 第二管百分比 */
    public float getTankPercentage() {
        if (getTankMax() <= 0) return 0.0f;
        return (float) tankOxygen / getTankMax();
    }

    // ==================== 消耗 / 补充 ====================
    /** 消耗（先扣第二管，再扣第一管） */
    public void consumeOxygen(float amount) {
        int need = (int) Math.ceil(amount);

        if (tankOxygen > 0) {
            int fromTank = Math.min(tankOxygen, need);
            tankOxygen -= fromTank;
            need -= fromTank;
        }

        if (need > 0) {
            oxygen = Math.max(0, oxygen - need);
        }
    }

    /** 补充（先补第一管，再补第二管） */
    public void addOxygen(int amount) {
        int spaceBase = getBaseMax() - oxygen;
        int toBase = Math.min(spaceBase, amount);
        oxygen += toBase;
        amount -= toBase;

        if (amount > 0 && getTankMax() > 0) {
            int spaceTank = getTankMax() - tankOxygen;
            int toTank = Math.min(spaceTank, amount);
            tankOxygen += toTank;
        }
    }

    // ==================== 伤害冷却 ====================
    public int getDamageCooldown() {
        return damageCooldown;
    }

    public void setDamageCooldown(int value) {
        this.damageCooldown = Math.max(0, value);
    }

    public void tickDamageCooldown() {
        if (damageCooldown > 0) damageCooldown--;
    }
}