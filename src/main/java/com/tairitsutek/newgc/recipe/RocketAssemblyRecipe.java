package com.tairitsutek.newgc.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tairitsutek.newgc.menu.RocketAssemblerLayout;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * TBMK 工作台配方：14 个固定槽位依次对应鼻锥、8 块重型合金板、
 * 引擎和 4 个助推器。
 */
public class RocketAssemblyRecipe implements Recipe<CraftingInput> {

    private final List<Ingredient> ingredients;
    private final ItemStack result;

    public RocketAssemblyRecipe(List<Ingredient> ingredients, ItemStack result) {
        if (ingredients == null || ingredients.size() != RocketAssemblerLayout.INPUT_SLOTS) {
            throw new IllegalArgumentException("TBMK recipe must have exactly "
                    + RocketAssemblerLayout.INPUT_SLOTS + " ingredients");
        }
        this.ingredients = List.copyOf(ingredients);
        this.result = result.copy();
    }

    public ItemStack getResult() {
        return result;
    }

    public Ingredient getIngredient(int slot) {
        return ingredients.get(slot);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        if (input.size() != RocketAssemblerLayout.INPUT_SLOTS) {
            return false;
        }
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.getItem(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input,
                                       HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    /** 不在原版配方书分类；只由 TBMK 工作台和 JEI 展示。 */
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
        return ModRecipes.ROCKET_ASSEMBLY_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.ROCKET_ASSEMBLY.get();
    }

    public static final MapCodec<RocketAssemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.list(Ingredient.CODEC_NONEMPTY)
                            .fieldOf("ingredients")
                            .forGetter(recipe -> recipe.ingredients),
                    ItemStack.CODEC.fieldOf("result").forGetter(RocketAssemblyRecipe::getResult)
            ).apply(instance, RocketAssemblyRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketAssemblyRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RocketAssemblyRecipe decode(RegistryFriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    List<Ingredient> ingredients = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    return new RocketAssemblyRecipe(ingredients, result);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RocketAssemblyRecipe recipe) {
                    buf.writeVarInt(recipe.ingredients.size());
                    for (Ingredient ingredient : recipe.ingredients) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result);
                }
            };
}
