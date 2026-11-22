package com.tairitsutek.newgc.client.jei;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 保存 JEI 运行时引用，让机器 GUI 的进度条可以直接打开对应配方分类。
 * <p>
 * 这个类刻意不引用任何 JEI 类型：JEI 是可选依赖，未安装时机器界面仍然必须
 * 能正常加载。真正的 JEI 对象由 {@link NewgcJeiPlugin} 在运行时注入，并通过
 * 反射调用 {@code IRecipesGui#showTypes}。
 */
public final class JeiRecipeOpener {

    public enum Kind {
        COMPRESSOR,
        ALLOY_SMELTING,
        SMELTING,
        ROCKET_ASSEMBLY
    }

    @Nullable
    private static Object runtime;
    private static final Map<Kind, Object> RECIPE_TYPES = new EnumMap<>(Kind.class);

    private JeiRecipeOpener() {
    }

    public static void setRuntime(@Nullable Object value) {
        runtime = value;
        if (value == null) {
            RECIPE_TYPES.clear();
        }
    }

    public static void setRecipeType(Kind kind, Object recipeType) {
        if (recipeType == null) {
            RECIPE_TYPES.remove(kind);
        } else {
            RECIPE_TYPES.put(kind, recipeType);
        }
    }

    public static boolean isAvailable() {
        return runtime != null;
    }

    /**
     * 打开某个机器在 JEI 中的完整配方分类。
     *
     * @return 是否成功调用 JEI；未安装 JEI 时返回 false
     */
    public static boolean open(Kind kind) {
        Object recipeType = RECIPE_TYPES.get(kind);
        if (runtime == null || recipeType == null) {
            return false;
        }
        try {
            Class<?> runtimeApi = Class.forName("mezz.jei.api.runtime.IJeiRuntime");
            Object recipesGui = runtimeApi.getMethod("getRecipesGui").invoke(runtime);
            Class<?> guiApi = Class.forName("mezz.jei.api.runtime.IRecipesGui");
            Method showTypes = guiApi.getMethod("showTypes", List.class);
            showTypes.invoke(recipesGui, List.of(recipeType));
            return true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    /** 进度条悬停提示：只显示“查看配方”。 */
    public static Component recipeHint() {
        return Component.translatable("gui.newgc.machine.view_recipe");
    }
}
