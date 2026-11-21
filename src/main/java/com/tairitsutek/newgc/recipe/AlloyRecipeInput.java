package com.tairitsutek.newgc.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * 合金熔炉的两个输入槽。与 {@link net.minecraft.world.item.crafting.SingleRecipeInput}
 * 相同，只是多提供一个槽位。
 */
public record AlloyRecipeInput(ItemStack first, ItemStack second) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> first;
            case 1 -> second;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
