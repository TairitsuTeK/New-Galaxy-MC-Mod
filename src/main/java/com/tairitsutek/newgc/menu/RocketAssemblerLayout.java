package com.tairitsutek.newgc.menu;

/**
 * TBMK 工作台的槽位布局。
 * <p>
 * 槽位按火箭外形排列：顶部鼻锥、中间 2x4 重型合金板、底部引擎，
 * 左右各两枚助推器，最终在右侧输出火箭。
 */
public final class RocketAssemblerLayout {

    public static final int NOSE_SLOT = 0;
    public static final int PLATE_SLOT_START = 1;
    public static final int PLATE_COLUMNS = 2;
    public static final int PLATE_ROWS = 4;
    public static final int ENGINE_SLOT = 9;
    public static final int BOOSTER_SLOT_START = 10;
    public static final int BOOSTER_COUNT = 4;
    public static final int INPUT_SLOTS = 14;
    public static final int OUTPUT_SLOT = 14;
    public static final int SLOT_COUNT = 15;

    public static final int GUI_WIDTH = 200;
    public static final int GUI_HEIGHT = 240;

    private RocketAssemblerLayout() {
    }

    public static int slotX(int index) {
        if (index == NOSE_SLOT || index == ENGINE_SLOT) {
            return 79;
        }
        if (isPlateSlot(index)) {
            int plate = index - PLATE_SLOT_START;
            return 70 + (plate % PLATE_COLUMNS) * 18;
        }
        if (isBoosterSlot(index)) {
            return index < BOOSTER_SLOT_START + 2 ? 48 : 110;
        }
        if (index == OUTPUT_SLOT) {
            return 166;
        }
        return 0;
    }

    public static int slotY(int index) {
        if (index == NOSE_SLOT) {
            return 22;
        }
        if (isPlateSlot(index)) {
            int plate = index - PLATE_SLOT_START;
            return 40 + (plate / PLATE_COLUMNS) * 18;
        }
        if (index == ENGINE_SLOT) {
            return 112;
        }
        if (isBoosterSlot(index)) {
            return index % 2 == 0 ? 58 : 76;
        }
        if (index == OUTPUT_SLOT) {
            return 58;
        }
        return 0;
    }

    public static boolean isPlateSlot(int index) {
        return index >= PLATE_SLOT_START
                && index < PLATE_SLOT_START + PLATE_COLUMNS * PLATE_ROWS;
    }

    public static boolean isBoosterSlot(int index) {
        return index >= BOOSTER_SLOT_START
                && index < BOOSTER_SLOT_START + BOOSTER_COUNT;
    }
}
