package com.tairitsutek.newgc.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class RocketAssemblyRecipeSerializer implements RecipeSerializer<RocketAssemblyRecipe> {

    @Override
    public @NotNull MapCodec<RocketAssemblyRecipe> codec() {
        return RocketAssemblyRecipe.CODEC;
    }

    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, RocketAssemblyRecipe> streamCodec() {
        return RocketAssemblyRecipe.STREAM_CODEC;
    }
}
