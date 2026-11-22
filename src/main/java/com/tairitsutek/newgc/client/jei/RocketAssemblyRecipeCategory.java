package com.tairitsutek.newgc.client.jei;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.client.gui.UiTheme;
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
        return 146;
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
        UiTheme.screen(guiGraphics, 0, 0, getWidth(), getHeight());

        // 与 TBMK 工作台一致的火箭轮廓，让 JEI 里也能直观看出部件位置。
        guiGraphics.fill(66, 36, 110, 130, 0x142B647A);
        guiGraphics.fill(44, 54, 66, 94, 0x0C2B647A);
        guiGraphics.fill(110, 54, 132, 94, 0x0C2B647A);
        guiGraphics.fill(77, 36, 99, 38, 0x305A9CB8);
        guiGraphics.fill(69, 108, 107, 110, 0x305A9CB8);

        for (int slot = 0; slot < RocketAssemblerLayout.INPUT_SLOTS; slot++) {
            UiTheme.slot(guiGraphics,
                    RocketAssemblerLayout.slotX(slot),
                    RocketAssemblerLayout.slotY(slot));
        }
        UiTheme.slot(guiGraphics,
                RocketAssemblerLayout.slotX(RocketAssemblerLayout.OUTPUT_SLOT),
                RocketAssemblerLayout.slotY(RocketAssemblerLayout.OUTPUT_SLOT));

        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, getTitle(), 8, 5, UiTheme.TEXT, false);
        guiGraphics.drawString(font,
                Component.translatable("gui.newgc.jei.rocket_hint"),
                8, 16, UiTheme.MUTED, false);
        guiGraphics.drawString(font,
                Component.translatable("gui.newgc.jei.rocket_info"),
                8, 134, UiTheme.TEXT, false);
    }
}
