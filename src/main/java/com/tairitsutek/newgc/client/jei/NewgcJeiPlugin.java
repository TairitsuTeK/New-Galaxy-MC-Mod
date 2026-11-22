package com.tairitsutek.newgc.client.jei;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.recipe.AlloySmeltingRecipe;
import com.tairitsutek.newgc.recipe.CompressorRecipe;
import com.tairitsutek.newgc.recipe.ModRecipes;
import com.tairitsutek.newgc.recipe.RocketAssemblyRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class NewgcJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        JeiRecipeOpener.setRuntime(jeiRuntime);
        JeiRecipeOpener.setRecipeType(JeiRecipeOpener.Kind.COMPRESSOR,
                CompressorRecipeCategory.TYPE);
        JeiRecipeOpener.setRecipeType(JeiRecipeOpener.Kind.ALLOY_SMELTING,
                AlloySmeltingRecipeCategory.TYPE);
        JeiRecipeOpener.setRecipeType(JeiRecipeOpener.Kind.SMELTING,
                mezz.jei.api.constants.RecipeTypes.SMELTING);
        JeiRecipeOpener.setRecipeType(JeiRecipeOpener.Kind.ROCKET_ASSEMBLY,
                RocketAssemblyRecipeCategory.TYPE);
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiRecipeOpener.setRuntime(null);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new CompressorRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new AlloySmeltingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new RocketAssemblyRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        RecipeManager manager = getRecipeManager();
        if (manager == null) return;

        List<CompressorRecipe> recipes = manager.getAllRecipesFor(ModRecipes.COMPRESSING.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(CompressorRecipeCategory.TYPE, recipes);

        List<AlloySmeltingRecipe> alloyRecipes =
                manager.getAllRecipesFor(ModRecipes.ALLOY_SMELTING.get())
                        .stream()
                        .map(RecipeHolder::value)
                        .toList();
        registration.addRecipes(AlloySmeltingRecipeCategory.TYPE, alloyRecipes);

        List<RocketAssemblyRecipe> assemblyRecipes =
                manager.getAllRecipesFor(ModRecipes.ROCKET_ASSEMBLY.get())
                        .stream()
                        .map(RecipeHolder::value)
                        .toList();
        registration.addRecipes(RocketAssemblyRecipeCategory.TYPE, assemblyRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(MachinesBlock.COMPRESSOR_ITEM.get()),
                CompressorRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(MachinesBlock.ALLOY_SMELTER_ITEM.get()),
                AlloySmeltingRecipeCategory.TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(MachinesBlock.ROCKET_ASSEMBLER_ITEM.get()),
                RocketAssemblyRecipeCategory.TYPE
        );
    }

    private static RecipeManager getRecipeManager() {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            return connection.getRecipeManager();
        }
        if (mc.level != null) {
            return mc.level.getRecipeManager();
        }
        return null;
    }
}