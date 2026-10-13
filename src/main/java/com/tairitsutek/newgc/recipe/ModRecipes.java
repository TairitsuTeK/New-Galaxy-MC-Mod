package com.tairitsutek.newgc.recipe;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, NewGalaxyMain.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NewGalaxyMain.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CompressorRecipe>> COMPRESSING =
            RECIPE_TYPES.register("compressing",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "compressing")));

    public static final DeferredHolder<RecipeSerializer<?>, CompressorRecipeSerializer> COMPRESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("compressing", CompressorRecipeSerializer::new);

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }
}
