package com.tairitsutek.newgc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.block.entity.RocketAssemblerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * TBMK 工作台的 3 格高机械臂渲染器。
 * <p>
 * 方块模型只保留一个合法的底座（用于物品图标和破坏粒子）；
 * 实际的三格高结构由这里的直接方块几何渲染，避免原版 BlockElement 的 32 像素边界限制。
 */
public class RocketAssemblerRenderer implements BlockEntityRenderer<RocketAssemblerBlockEntity> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            NewGalaxyMain.MODID, "textures/block/rocket_assembler_machine.png");
    private static final float P = 1.0F / 16.0F;

    public RocketAssemblerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull RocketAssemblerBlockEntity blockEntity, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f mat = poseStack.last().pose();

        // 底座与装配台
        box(vc, mat, 0, 0, 0, 16, 5, 16, 0.34F, packedLight, packedOverlay);
        box(vc, mat, 2, 5, 2, 14, 8, 14, 0.58F, packedLight, packedOverlay);
        box(vc, mat, 2, 8, 2, 14, 12, 4, 0.42F, packedLight, packedOverlay);

        // 两根加长机械臂支柱
        box(vc, mat, 2, 8, 3.5F, 4, 44, 5.5F, 0.62F, packedLight, packedOverlay);
        box(vc, mat, 12, 8, 3.5F, 14, 44, 5.5F, 0.62F, packedLight, packedOverlay);
        box(vc, mat, 2, 42, 3, 14, 48, 5.5F, 0.72F, packedLight, packedOverlay);

        // 三层横向机械臂，连接中央装配舱
        box(vc, mat, 3, 14, 4.5F, 9, 16, 6, 0.72F, packedLight, packedOverlay);
        box(vc, mat, 3, 28, 4.5F, 10, 30, 6, 0.72F, packedLight, packedOverlay);
        box(vc, mat, 3, 38, 4.5F, 9, 40, 6, 0.72F, packedLight, packedOverlay);
        box(vc, mat, 7, 14, 10, 13, 16, 11.5F, 0.72F, packedLight, packedOverlay);
        box(vc, mat, 6, 28, 10, 13, 30, 11.5F, 0.72F, packedLight, packedOverlay);
        box(vc, mat, 7, 38, 10, 13, 40, 11.5F, 0.72F, packedLight, packedOverlay);

        // 夹爪
        box(vc, mat, 6, 17, 6, 7.5F, 20, 9, 0.82F, packedLight, packedOverlay);
        box(vc, mat, 8.5F, 17, 6, 10, 20, 9, 0.82F, packedLight, packedOverlay);

        // 中央半成品火箭
        box(vc, mat, 7, 10, 7, 9, 30, 9, 0.82F, packedLight, packedOverlay);
        box(vc, mat, 7.25F, 30, 7.25F, 8.75F, 35, 8.75F, 0.88F, packedLight, packedOverlay);
        box(vc, mat, 6.5F, 7, 6.5F, 9.5F, 10, 9.5F, 0.46F, packedLight, packedOverlay);
        box(vc, mat, 6.5F, 10, 6.5F, 7, 13, 9.5F, 0.54F, packedLight, packedOverlay);
        box(vc, mat, 9, 10, 6.5F, 9.5F, 13, 9.5F, 0.54F, packedLight, packedOverlay);

        // 状态灯
        box(vc, mat, 1, 5, 1, 2, 7, 2, 0.92F, packedLight, packedOverlay);
        box(vc, mat, 14, 5, 1, 15, 7, 2, 0.92F, packedLight, packedOverlay);
    }

    /** 绘制一个以像素为单位的六面体。 */
    private static void box(VertexConsumer vc, Matrix4f mat,
                            float x0, float y0, float z0,
                            float x1, float y1, float z1,
                            float tint, int light, int overlay) {
        float ax = x0 * P, ay = y0 * P, az = z0 * P;
        float bx = x1 * P, by = y1 * P, bz = z1 * P;
        int color = (int) (tint * 255.0F);
        int alpha = 255;

        // north / south
        quad(vc, mat, ax, ay, az, bx, ay, az, bx, by, az, ax, by, az,
                color, alpha, 0, 0, 1, 1, 0, 0, -1, light, overlay);
        quad(vc, mat, bx, ay, bz, ax, ay, bz, ax, by, bz, bx, by, bz,
                color, alpha, 0, 0, 1, 1, 0, 0, 1, light, overlay);
        // west / east
        quad(vc, mat, ax, ay, bz, ax, ay, az, ax, by, az, ax, by, bz,
                color, alpha, 0, 0, 1, 1, -1, 0, 0, light, overlay);
        quad(vc, mat, bx, ay, az, bx, ay, bz, bx, by, bz, bx, by, az,
                color, alpha, 0, 0, 1, 1, 1, 0, 0, light, overlay);
        // up / down
        quad(vc, mat, ax, by, az, bx, by, az, bx, by, bz, ax, by, bz,
                color, alpha, 0, 0, 1, 1, 0, 1, 0, light, overlay);
        quad(vc, mat, ax, ay, bz, bx, ay, bz, bx, ay, az, ax, ay, az,
                color, alpha, 0, 0, 1, 1, 0, -1, 0, light, overlay);
    }

    private static void quad(VertexConsumer vc, Matrix4f mat,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             int color, int alpha,
                             float u0, float v0, float u1, float v1,
                             float nx, float ny, float nz,
                             int light, int overlay) {
        addVertex(vc, mat, x0, y0, z0, u0, v0, color, alpha, nx, ny, nz, light, overlay);
        addVertex(vc, mat, x1, y1, z1, u0, v1, color, alpha, nx, ny, nz, light, overlay);
        addVertex(vc, mat, x2, y2, z2, u1, v1, color, alpha, nx, ny, nz, light, overlay);
        addVertex(vc, mat, x3, y3, z3, u1, v0, color, alpha, nx, ny, nz, light, overlay);
    }

    private static void addVertex(VertexConsumer vc, Matrix4f mat,
                                  float x, float y, float z,
                                  float u, float v,
                                  int color, int alpha,
                                  float nx, float ny, float nz,
                                  int light, int overlay) {
        vc.addVertex(mat, x, y, z)
                .setColor(color, color, color, alpha)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }
}
