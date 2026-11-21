package com.tairitsutek.newgc.client.jei;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.menu.RocketAssemblerLayout;
import com.tairitsutek.newgc.recipe.RocketAssemblyRecipe;
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

/**
 * JEI 中的 TBMK 工作台配方：按 GUI 外形展示鼻锥、重型合金板、引擎和助推器。
 */
public class RocketAssemblyRecipeCategory implements IRecipeCategory<RocketAssemblyRecipe> {

    public static final RecipeType<RocketAssemblyRecipe> TYPE = RecipeType.create(
            NewGalaxyMain.MODID,
            "rocket_assembly",
            RocketAssemblyRecipe.class
    );

    private final IDrawable icon;

    public RocketAssemblyRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(MachinesBlock.ROCKET_ASSEMBLER_ITEM.get()));
    }

    @Override
    public @NotNull RecipeType<RocketAssemblyRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.newgc.rocket_assembler");
    }

    @Override
    public int getWidth() {
        return RocketAssemblerLayout.GUI_WIDTH;
    }

    @Override
    public int getHeight() {
        return 132;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RocketAssemblyRecipe recipe,
                          @NotNull IFocusGroup focuses) {
        for (int slot = 0; slot < RocketAssemblerLayout.INPUT_SLOTS; slot++) {
            builder.addInputSlot(RocketAssemblerLayout.slotX(slot),
                            RocketAssemblerLayout.slotY(slot))
                    .addIngredients(recipe.getIngredient(slot));
        }
        builder.addOutputSlot(
                        RocketAssemblerLayout.slotX(RocketAssemblerLayout.OUTPUT_SLOT),
                        RocketAssemblerLayout.slotY(RocketAssemblerLayout.OUTPUT_SLOT))
                .addItemStack(recipe.getResult());
    }

    @Override
    public void draw(RocketAssemblyRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("gui.newgc.rocket_assembler.recipe_hint"),
                4, 4, 0xFF8FA8B8, false);
    }
}
