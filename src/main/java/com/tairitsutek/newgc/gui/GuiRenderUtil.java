package com.tairitsutek.newgc.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public final class GuiRenderUtil {

    private GuiRenderUtil() {

    }

    /**
     * 禁用当前绑定纹理的线性过滤，使用最近邻采样。
     * 调用后，当前绑定的 GUI 纹理不会再被平滑插值，像素边界锐利。
     */
    public static void useNearestFilter() {
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    /**
     * 恢复为线性过滤。如果有需要，可以在渲染结束后调用。
     */
    public static void useLinearFilter() {
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }
}