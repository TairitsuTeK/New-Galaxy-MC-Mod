package com.tairitsutek.newgc.client.jei;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.recipe.AlloySmeltingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** JEI 展示合金熔炉配方，同时支持重型合金的 5+5 输入数量。 */
public class AlloySmeltingRecipeCategory implements IRecipeCategory<AlloySmeltingRecipe> {

    public static final RecipeType<AlloySmeltingRecipe> TYPE = RecipeType.create(
            NewGalaxyMain.MODID,
            "alloy_smelting",
            AlloySmeltingRecipe.class
    );

    private final IDrawable icon;

    public AlloySmeltingRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(MachinesBlock.ALLOY_SMELTER_ITEM.get()));
    }

    @Override
    public @NotNull RecipeType<AlloySmeltingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.newgc.alloy_smelter");
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloySmeltingRecipe recipe,
                          @NotNull IFocusGroup focuses) {
        if (recipe.ingredients().size() > 0) {
            builder.addInputSlot(1, 15).addIngredients(recipe.ingredients().get(0));
        }
        if (recipe.ingredients().size() > 1) {
            builder.addInputSlot(41, 15).addIngredients(recipe.ingredients().get(1));
        }
        builder.addOutputSlot(101, 15).addItemStack(recipe.getResult());
    }

    @Override
    public void draw(AlloySmeltingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font, "→", 64, 18,
                0xFFFFFFFF, false);
        if (recipe.getInputCount() > 1) {
            guiGraphics.drawString(Minecraft.getInstance().font,
                    "×" + recipe.getInputCount(), 4, 38, 0xFFFFC878, false);
        }
    }
}
