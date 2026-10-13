package com.tairitsutek.newgc.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class CompressorRecipeSerializer implements RecipeSerializer<CompressorRecipe> {

    @Override
    public @NotNull MapCodec<CompressorRecipe> codec() {
        return CompressorRecipe.CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> streamCodec() {
        return CompressorRecipe.STREAM_CODEC;
    }
}
