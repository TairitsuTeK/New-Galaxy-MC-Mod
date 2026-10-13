package com.tairitsutek.newgc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CompressorRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int inputCount;

    public CompressorRecipe(Ingredient ingredient, ItemStack result, int inputCount) {
        this.ingredient = ingredient;
        this.result = result;
        this.inputCount = inputCount;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    public boolean matches(SingleRecipeInput input, @NotNull Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SingleRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.COMPRESSING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.COMPRESSING.get();
    }

    public static final MapCodec<CompressorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CompressorRecipe::getIngredient),
                    ItemStack.CODEC.fieldOf("result").forGetter(CompressorRecipe::getResult),
                    Codec.INT.optionalFieldOf("inputCount", 1).forGetter(CompressorRecipe::getInputCount)
            ).apply(instance, CompressorRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CompressorRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, CompressorRecipe::getIngredient,
                    ItemStack.STREAM_CODEC, CompressorRecipe::getResult,
                    ByteBufCodecs.VAR_INT, CompressorRecipe::getInputCount,
                    CompressorRecipe::new
            );
}
