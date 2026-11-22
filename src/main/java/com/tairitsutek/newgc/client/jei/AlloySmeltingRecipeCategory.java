package com.tairitsutek.newgc.client.jei;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.client.gui.UiTheme;
import com.tairitsutek.newgc.recipe.AlloySmeltingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/** JEI 展示合金熔炉配方，同时支持配方自定义的每槽输入数量。 */
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
        return 160;
    }

    @Override
    public int getHeight() {
        return 96;
    }

    @Override
    public boolean needsRecipeBorder() {
        // 分类自身已经绘制完整的深空面板与描边，不再叠加 JEI 默认边框。
        return false;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloySmeltingRecipe recipe,
                          @NotNull IFocusGroup focuses) {
        if (!recipe.ingredients().isEmpty()) {
            builder.addInputSlot(20, 28).addIngredients(recipe.ingredients().get(0));
        }
        if (recipe.ingredients().size() > 1) {
            builder.addInputSlot(20, 56).addIngredients(recipe.ingredients().get(1));
        }
        builder.addOutputSlot(132, 42).addItemStack(recipe.getResult());
    }

    @Override
    public void draw(AlloySmeltingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        UiTheme.screen(guiGraphics, 0, 0, getWidth(), getHeight());
        UiTheme.slot(guiGraphics, 20, 28);
        UiTheme.slot(guiGraphics, 20, 56);
        UiTheme.slot(guiGraphics, 132, 42);
        UiTheme.progressBar(guiGraphics, 52, 46, 56, 8,
                1, 1, 0xFF5B4B2A, 0xFFEBC27A);
    }
}
