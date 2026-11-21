package com.tairitsutek.newgc.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class AlloySmeltingRecipeSerializer implements RecipeSerializer<AlloySmeltingRecipe> {

    @Override
    public @NotNull MapCodec<AlloySmeltingRecipe> codec() {
        return AlloySmeltingRecipe.CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> streamCodec() {
        return AlloySmeltingRecipe.STREAM_CODEC;
    }
}
