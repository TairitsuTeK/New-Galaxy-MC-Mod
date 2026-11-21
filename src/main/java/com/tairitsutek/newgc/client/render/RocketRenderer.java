package com.tairitsutek.newgc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * 火箭渲染器。
 * <p>
 * 模型本身是 6 格高、1.25 格宽，这里放大 2 倍以匹配 1.4 x 6 格的碰撞箱。
 */
public class RocketRenderer extends EntityRenderer<RocketEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "textures/entity/rocket.png");
    private static final ResourceLocation TIER2_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID,
                    "textures/entity/tier2_rocket.png");
    private static final ResourceLocation TIER3_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID,
                    "textures/entity/tier3_rocket.png");
    private static final float MODEL_SCALE = 2.0F;

    private final RocketModel model;

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RocketModel(context.bakeLayer(RocketModel.LAYER));
        this.shadowRadius = 0.55F;
    }

    @Override
    public void render(@NotNull RocketEntity entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 实体模型坐标系：+Y 向下，这里翻转后模型从实体脚部向上生长
        poseStack.scale(-MODEL_SCALE, -MODEL_SCALE, MODEL_SCALE);

        float ageInTicks = entity.tickCount + partialTicks;
        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = buffer.getBuffer(
                RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull RocketEntity entity) {
        if (entity.getTier() >= 3) {
            return TIER3_TEXTURE;
        }
        return entity.getTier() >= 2 ? TIER2_TEXTURE : TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(@NotNull RocketEntity entity, @NotNull net.minecraft.core.BlockPos pos) {
        // 点火/飞行时自带亮度，避免夜间看不见火箭
        int state = entity.getState();
        if (state == RocketEntity.STATE_ASCENT || state == RocketEntity.STATE_COUNTDOWN) {
            return Mth.clamp(super.getBlockLightLevel(entity, pos) + 8, 0, 15);
        }
        return super.getBlockLightLevel(entity, pos);
    }
}
