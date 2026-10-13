package com.tairitsutek.newgc.client.jei;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.recipe.CompressorRecipe;
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

public class CompressorRecipeCategory implements IRecipeCategory<CompressorRecipe> {

    public static final RecipeType<CompressorRecipe> TYPE = RecipeType.create(
            NewGalaxyMain.MODID,
            "compressing",
            CompressorRecipe.class
    );

    private final IDrawable icon;

    public CompressorRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(MachinesBlock.COMPRESSOR_ITEM.get()));
    }

    @Override
    public @NotNull RecipeType<CompressorRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.newgc.compressor");
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CompressorRecipe recipe, @NotNull IFocusGroup focuses) {
        builder.addInputSlot(1, 11).addIngredients(recipe.getIngredient());
        builder.addOutputSlot(101, 11).addItemStack(recipe.getResult());
    }

    @Override
    public void draw(CompressorRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font, "→", 58, 15, 0xFFFFFFFF, false);

        if (recipe.getInputCount() > 1) {
            guiGraphics.drawString(Minecraft.getInstance().font, "×" + recipe.getInputCount(),
                    4, 30, 0xFFFFFFFF, false);
        }
    }
}