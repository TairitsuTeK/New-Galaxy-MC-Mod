package com.tairitsutek.newgc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 合金熔炉配方。两个输入槽的顺序不影响匹配结果，方便玩家任意摆放。
 */
public class AlloySmeltingRecipe implements Recipe<AlloyRecipeInput> {

    private final List<Ingredient> ingredients;
    private final ItemStack result;
    private final int inputCount;

    public AlloySmeltingRecipe(List<Ingredient> ingredients, ItemStack result) {
        this(ingredients, result, 1);
    }

    public AlloySmeltingRecipe(List<Ingredient> ingredients, ItemStack result, int inputCount) {
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
        this.inputCount = Math.max(1, inputCount);
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }

    /** 每个输入槽需要消耗的数量；旧配方默认 1。 */
    public int getInputCount() {
        return inputCount;
    }

    @Override
    public boolean matches(AlloyRecipeInput input, @NotNull Level level) {
        if (ingredients.size() != 2) {
            return false;
        }
        Ingredient first = ingredients.get(0);
        Ingredient second = ingredients.get(1);
        ItemStack slot0 = input.getItem(0);
        ItemStack slot1 = input.getItem(1);
        return (first.test(slot0) && second.test(slot1))
                || (first.test(slot1) && second.test(slot0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull AlloyRecipeInput input,
                                        HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /** 合金熔炉配方不进入原版配方书；由 JEI 分类展示。 */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALLOY_SMELTING_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.ALLOY_SMELTING.get();
    }

    public static final MapCodec<AlloySmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.list(Ingredient.CODEC_NONEMPTY)
                            .fieldOf("ingredients")
                            .forGetter(AlloySmeltingRecipe::ingredients),
                    ItemStack.CODEC
                            .fieldOf("result")
                            .forGetter(AlloySmeltingRecipe::getResult),
                    Codec.INT.optionalFieldOf("inputCount", 1)
                            .forGetter(AlloySmeltingRecipe::getInputCount)
            ).apply(instance, AlloySmeltingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NotNull AlloySmeltingRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    List<Ingredient> ingredients = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    int inputCount = buf.readVarInt();
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    return new AlloySmeltingRecipe(ingredients, result, inputCount);
                }

                @Override
                public void encode(@NotNull RegistryFriendlyByteBuf buf,
                                   @NotNull AlloySmeltingRecipe recipe) {
                    buf.writeVarInt(recipe.ingredients.size());
                    for (Ingredient ingredient : recipe.ingredients) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }
                    buf.writeVarInt(recipe.inputCount);
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                }
            };
}
