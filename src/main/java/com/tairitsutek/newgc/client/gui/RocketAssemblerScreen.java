package com.tairitsutek.newgc.client.gui;

import com.tairitsutek.newgc.client.jei.JeiRecipeOpener;
import com.tairitsutek.newgc.menu.RocketAssemblerLayout;
import com.tairitsutek.newgc.menu.RocketAssemblerMenu;
import com.tairitsutek.newgc.network.RocketAssemblePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TBMK 工作台界面。槽位直接按一阶/二阶火箭外形排列：
 * 鼻锥 → 2x4 重型合金板 → 引擎，两侧为助推器。
 */
public class RocketAssemblerScreen extends StyledContainerScreen<RocketAssemblerMenu> {

    private Button assembleButton;

    public RocketAssemblerScreen(RocketAssemblerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = RocketAssemblerLayout.GUI_WIDTH;
        this.imageHeight = RocketAssemblerLayout.GUI_HEIGHT;
        this.inventoryLabelY = 146;
    }

    @Override
    protected List<StatusArea> statusAreas() {
        int x = this.leftPos;
        int y = this.topPos;
        Runnable openRecipes = JeiRecipeOpener.isAvailable()
                ? () -> JeiRecipeOpener.open(JeiRecipeOpener.Kind.ROCKET_ASSEMBLY)
                : null;
        return List.of(
                new StatusArea(x + 140, y + 34, 54, 20,
                        () -> List.of(Component.translatable(menu.getCanAssemble()
                                        ? "gui.newgc.machine.assembler_ready"
                                        : "gui.newgc.machine.assembler_waiting")
                                .withStyle(menu.getCanAssemble()
                                        ? ChatFormatting.GREEN : ChatFormatting.GRAY)),
                        null),
                new StatusArea(x + 140, y + 106, 54, 16,
                        () -> List.of(JeiRecipeOpener.recipeHint()),
                        openRecipes));
    }

    @Override
    protected void init() {
        super.init();
        this.assembleButton = new ThemeButton(Button.builder(
                        Component.translatable("gui.newgc.rocket_assembler.assemble"),
                        button -> PacketDistributor.sendToServer(new RocketAssemblePacket()))
                .bounds(leftPos + 140, topPos + 84, 52, 20));
        addRenderableWidget(this.assembleButton);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        UiTheme.screen(g, x, y, imageWidth, imageHeight);

        // 极淡的火箭轮廓，仅用于提示布局，不抢槽位视觉
        g.fill(x + 66, y + 38, x + 110, y + 132, 0x142B647A);
        g.fill(x + 44, y + 56, x + 66, y + 96, 0x0C2B647A);
        g.fill(x + 110, y + 56, x + 132, y + 96, 0x0C2B647A);
        g.fill(x + 77, y + 38, x + 99, y + 40, 0x305A9CB8);
        g.fill(x + 69, y + 110, x + 107, y + 112, 0x305A9CB8);

        // 按火箭外形绘制 14 个输入槽
        for (int slot = 0; slot < RocketAssemblerLayout.INPUT_SLOTS; slot++) {
            UiTheme.slot(g, x + RocketAssemblerLayout.slotX(slot),
                    y + RocketAssemblerLayout.slotY(slot));
        }
        UiTheme.slot(g,
                x + RocketAssemblerLayout.slotX(RocketAssemblerLayout.OUTPUT_SLOT),
                y + RocketAssemblerLayout.slotY(RocketAssemblerLayout.OUTPUT_SLOT));

        // 组装状态 + JEI 配方按钮，二者都是可悬停的状态区域。
        boolean ready = this.menu.getCanAssemble();
        g.fill(x + 140, y + 34, x + 194, y + 54, ready ? 0x4020644A : 0x40112735);
        g.drawCenteredString(this.font,
                Component.translatable(ready
                        ? "gui.newgc.machine.assembler_ready"
                        : "gui.newgc.machine.assembler_waiting"),
                x + 167, y + 40, ready ? UiTheme.GOOD : UiTheme.MUTED);

        g.fill(x + 140, y + 106, x + 194, y + 122, 0x40112735);
        g.fill(x + 140, y + 106, x + 194, y + 107, UiTheme.ACCENT_DIM);
        g.drawCenteredString(this.font,
                Component.translatable("gui.newgc.machine.recipe_button"),
                x + 167, y + 110, UiTheme.ACCENT);

        // 玩家背包
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                UiTheme.slot(g, x + 8 + col * 18, y + 158 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            UiTheme.slot(g, x + 8 + col * 18, y + 216);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.assembleButton.active = this.menu.getCanAssemble();
        super.render(g, mouseX, mouseY, partialTick);
    }
}
