package com.tairitsutek.newgc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 一阶载人火箭模型。
 * <p>
 * 用 16 段绕 Y 轴均匀旋转的窄面板拼出更平滑的圆柱/圆锥剖面：
 * 深灰喷口、银灰发动机舱、白灰渐变箭体、黑色级间环，
 * 上部是可辨认的载人舱：四扇深蓝舷窗、正面舱门与橙色警示环。
 * <p>
 * 纹理是 256x128 的八区域图集（见 {@code tools/generate_rocket_textures.py}）：
 * 三段机身高光渐变、发动机金属、深色结构、橙色识别色、舷窗玻璃与舱门细节。
 * 所有箱体的 UV 都限制在各自材质区域内，避免贴图串色。
 * <p>
 * 尺寸：最大直径约 1.1 格、高约 5.9 格（渲染时放大 2 倍）。
 */
public class RocketModel extends EntityModel<RocketEntity> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NewGalaxyMain.MODID, "rocket"), "main");

    /** 圆柱/圆锥的分段数，16 段让箭体轮廓接近真实圆柱。 */
    private static final int SEGMENTS = 16;

    private static final int TEX_W = 256;
    private static final int TEX_H = 128;

    /** 256x128 图集中八个 64x64 材质区域。 */
    private static final int UV_HULL_LOW_U  = 0;
    private static final int UV_HULL_LOW_V  = 0;
    private static final int UV_HULL_MID_U  = 64;
    private static final int UV_HULL_MID_V  = 0;
    private static final int UV_HULL_TOP_U  = 128;
    private static final int UV_HULL_TOP_V  = 0;
    private static final int UV_ENGINE_U    = 0;
    private static final int UV_ENGINE_V    = 64;
    private static final int UV_DARK_U      = 64;
    private static final int UV_DARK_V      = 64;
    private static final int UV_ACCENT_U    = 128;
    private static final int UV_ACCENT_V    = 64;
    private static final int UV_GLASS_U     = 192;
    private static final int UV_GLASS_V     = 0;
    private static final int UV_HATCH_U     = 192;
    private static final int UV_HATCH_V     = 64;

    private final ModelPart root;
    private final ModelPart tier2BoosterLeft;
    private final ModelPart tier2BoosterRight;
    private final ModelPart tier2Fins;
    private final ModelPart tier2Antenna;
    private final ModelPart tier3Details;

    public RocketModel(ModelPart root) {
        this.root = root;
        this.tier2BoosterLeft = root.getChild("tier2_booster_left");
        this.tier2BoosterRight = root.getChild("tier2_booster_right");
        this.tier2Fins = root.getChild("tier2_fins");
        this.tier2Antenna = root.getChild("tier2_antenna");
        this.tier3Details = root.getChild("tier3_details");
        setTier2Visible(false);
        setTier3Visible(false);
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ===== 尾喷口：深灰喇叭口 + 橙色警告环 =====
        addRing(root, "bell_low", 4.4F, 0.0F, 3.0F, 1.0F,
                UV_ENGINE_U, UV_ENGINE_V);
        addRing(root, "bell_mid", 3.9F, 3.0F, 5.0F, 1.0F,
                UV_ENGINE_U, UV_ENGINE_V);
        addRing(root, "bell_top", 3.5F, 5.0F, 7.0F, 1.0F,
                UV_DARK_U, UV_DARK_V);
        addRing(root, "engine_ring", 3.6F, 5.1F, 5.9F, 0.9F,
                UV_ACCENT_U, UV_ACCENT_V);

        // ===== 三段机身：下段银灰、中段浅灰、上段白色，形成纵向渐变 =====
        addRing(root, "body_low", 3.0F, 7.0F, 20.0F, 1.0F,
                UV_HULL_LOW_U, UV_HULL_LOW_V);
        addRing(root, "body_mid", 3.0F, 20.0F, 29.0F, 1.0F,
                UV_HULL_MID_U, UV_HULL_MID_V);
        addRing(root, "body_top", 2.9F, 29.0F, 36.0F, 1.0F,
                UV_HULL_TOP_U, UV_HULL_TOP_V);

        // ===== 黑色级间环与橙色识别环 =====
        addRing(root, "band_low", 3.06F, 11.5F, 13.0F, 0.8F,
                UV_DARK_U, UV_DARK_V);
        addRing(root, "band_mid", 3.06F, 23.0F, 24.5F, 0.8F,
                UV_ACCENT_U, UV_ACCENT_V);
        addRing(root, "band_top", 2.96F, 31.0F, 32.3F, 0.8F,
                UV_DARK_U, UV_DARK_V);

        // ===== 载人舱：白色舱段 + 四扇舷窗 =====
        addRing(root, "capsule_low", 2.85F, 36.0F, 40.0F, 1.0F,
                UV_HULL_TOP_U, UV_HULL_TOP_V);
        addRing(root, "capsule_high", 2.75F, 40.0F, 43.5F, 1.0F,
                UV_HULL_TOP_U, UV_HULL_TOP_V);
        addRing(root, "capsule_ring", 2.86F, 36.0F, 36.8F, 0.85F,
                UV_ACCENT_U, UV_ACCENT_V);
        for (int i = 0; i < 4; i++) {
            addWindow(root, i, (float) (i * Math.PI / 2.0D));
        }

        // 正面舱门：深色框 + 独立舱门面板 + 橙色把手
        addHatch(root);

        // ===== 深灰整流罩与尖端 =====
        addRing(root, "nose_base", 2.6F, 43.5F, 45.5F, 1.0F,
                UV_DARK_U, UV_DARK_V);
        addRing(root, "nose_high", 1.9F, 45.5F, 46.5F, 0.9F,
                UV_DARK_U, UV_DARK_V);
        addRing(root, "nose_tip", 0.8F, 46.5F, 47.0F, 0.6F,
                UV_ACCENT_U, UV_ACCENT_V);

        addFins(root);
        addTier2Details(root);
        addTier3Details(root);

        return LayerDefinition.create(mesh, TEX_W, TEX_H);
    }

    /**
     * 用 {@code SEGMENTS} 块绕 Y 轴均匀旋转的窄面板拼出一段圆环 / 圆锥台。
     * <p>
     * 实体模型坐标系里 +Y 向下（渲染时整体按 {@code scale(-1,-1,1)} 镜像），
     * 因此这里把上沿 {@code yTop} 取负作为箱体起点，模型才会从实体脚部向上生长。
     *
     * @param radius    外接圆半径（1 单位 = 1 像素）
     * @param yBottom   下沿（以脚部为 0 的向上高度）
     * @param yTop      上沿（以脚部为 0 的向上高度）
     * @param thickness 面板厚度
     */
    private static void addRing(PartDefinition root, String name, float radius,
                                float yBottom, float yTop, float thickness, int u, int v) {
        float apothem = (float) (radius * Math.cos(Math.PI / SEGMENTS));
        float panelWidth = (float) (2.0D * radius * Math.sin(Math.PI / SEGMENTS));
        float height = yTop - yBottom;

        for (int i = 0; i < SEGMENTS; i++) {
            float angle = (float) (i * (2.0D * Math.PI / SEGMENTS));
            root.addOrReplaceChild(name + "_" + i,
                    CubeListBuilder.create().texOffs(u, v)
                            .addBox(-panelWidth / 2.0F, -yTop, apothem - thickness,
                                    panelWidth, height, thickness),
                    PartPose.rotation(0.0F, angle, 0.0F));
        }
    }

    /**
     * 舷窗：先画深色窗框，再在更外侧画深蓝玻璃，避免玻璃被机身面板遮住。
     */
    private static void addWindow(PartDefinition root, int index, float angle) {
        float surface = 2.70F;
        root.addOrReplaceChild("window_frame_" + index,
                CubeListBuilder.create().texOffs(UV_DARK_U, UV_DARK_V)
                        .addBox(-0.88F, -35.2F, surface,
                                1.76F, 2.7F, 0.10F),
                PartPose.rotation(0.0F, angle, 0.0F));
        root.addOrReplaceChild("window_glass_" + index,
                CubeListBuilder.create().texOffs(UV_GLASS_U, UV_GLASS_V)
                        .addBox(-0.64F, -34.9F, surface + 0.10F,
                                1.28F, 2.1F, 0.08F),
                PartPose.rotation(0.0F, angle, 0.0F));
    }

    /**
     * 正面舱门。舱门比机身面板略微外凸，因此从侧面看有明确的厚度和边框。
     */
    private static void addHatch(PartDefinition root) {
        float surface = 2.92F;
        root.addOrReplaceChild("hatch_frame",
                CubeListBuilder.create().texOffs(UV_DARK_U, UV_DARK_V)
                        .addBox(-1.55F, -28.5F, surface,
                                3.10F, 6.5F, 0.10F),
                PartPose.ZERO);
        root.addOrReplaceChild("hatch_panel",
                CubeListBuilder.create().texOffs(UV_HATCH_U, UV_HATCH_V)
                        .addBox(-1.25F, -28.1F, surface + 0.10F,
                                2.50F, 5.7F, 0.07F),
                PartPose.ZERO);
        root.addOrReplaceChild("hatch_handle",
                CubeListBuilder.create().texOffs(UV_ACCENT_U, UV_ACCENT_V)
                        .addBox(-0.25F, -25.7F, surface + 0.17F,
                                0.50F, 1.20F, 0.05F),
                PartPose.ZERO);
    }

    /**
     * 四片阶梯状三角尾翼：每一级的宽度与伸长度同时递减，叠成斜边。
     */
    private static void addFins(PartDefinition root) {
        for (int i = 0; i < 4; i++) {
            CubeListBuilder fin = CubeListBuilder.create().texOffs(UV_DARK_U, UV_DARK_V);
            for (int step = 0; step < 5; step++) {
                float width = 5.0F - step;
                float depth = 3.0F - step * 0.45F;
                fin.addBox(-width / 2.0F, -9.0F - step * 2.0F, 2.7F,
                        width, 2.0F, depth);
            }
            root.addOrReplaceChild("fin_" + i, fin,
                    PartPose.rotation(0.0F, (float) (i * Math.PI / 2.0D), 0.0F));
        }
    }

    /**
     * 二阶火箭的外置助推器、四片斜置尾翼和顶部通信天线。
     * 这些结构共用 256x128 图集，因此不需要额外模型层。
     */
    private static void addTier2Details(PartDefinition root) {
        addTier2Booster(root, "tier2_booster_left", -4.2F);
        addTier2Booster(root, "tier2_booster_right", 4.2F);

        PartDefinition fins = root.addOrReplaceChild("tier2_fins",
                CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < 4; i++) {
            CubeListBuilder fin = CubeListBuilder.create()
                    .texOffs(UV_DARK_U, UV_DARK_V);
            for (int step = 0; step < 4; step++) {
                float width = 3.5F - step * 0.65F;
                float depth = 2.1F - step * 0.32F;
                fin.addBox(-width / 2.0F, -8.0F - step * 1.8F, 2.7F,
                        width, 1.8F, depth);
            }
            fins.addOrReplaceChild("tier2_fin_" + i, fin,
                    PartPose.rotation(0.0F,
                            (float) (i * Math.PI / 2.0D + Math.PI / 4.0D), 0.0F));
        }

        PartDefinition antenna = root.addOrReplaceChild("tier2_antenna",
                CubeListBuilder.create(), PartPose.ZERO);
        antenna.addOrReplaceChild("tier2_mast",
                CubeListBuilder.create().texOffs(UV_DARK_U, UV_DARK_V)
                        .addBox(-0.2F, -50.0F, 0.0F, 0.4F, 4.0F, 0.4F),
                PartPose.ZERO);
        antenna.addOrReplaceChild("tier2_dish",
                CubeListBuilder.create().texOffs(UV_ACCENT_U, UV_ACCENT_V)
                        .addBox(-1.6F, -51.4F, 0.1F, 3.2F, 0.35F, 1.8F),
                PartPose.ZERO);
    }

    private static void addTier2Booster(PartDefinition root, String name, float x) {
        PartDefinition booster = root.addOrReplaceChild(name,
                CubeListBuilder.create(), PartPose.offset(x, 0.0F, 0.0F));
        addRing(booster, name + "_bell", 0.9F, 6.0F, 8.0F, 0.6F,
                UV_ENGINE_U, UV_ENGINE_V);
        addRing(booster, name + "_body", 0.75F, 8.0F, 21.0F, 0.6F,
                UV_HULL_LOW_U, UV_HULL_LOW_V);
        addRing(booster, name + "_band", 0.8F, 12.0F, 13.2F, 0.55F,
                UV_DARK_U, UV_DARK_V);
        addRing(booster, name + "_nose", 0.55F, 21.0F, 26.0F, 0.45F,
                UV_ACCENT_U, UV_ACCENT_V);
    }

    /**
     * 三阶火箭在二阶外形基础上增加前后两组助推器、四片主翼和金色识别环。
     * 这些细节合并在一个父部件中，按等级整体显示/隐藏。
     */
    private static void addTier3Details(PartDefinition root) {
        PartDefinition details = root.addOrReplaceChild("tier3_details",
                CubeListBuilder.create(), PartPose.ZERO);

        addTier3Booster(details, "tier3_booster_front", 4.6F);
        addTier3Booster(details, "tier3_booster_back", -4.6F);

        PartDefinition wings = details.addOrReplaceChild("tier3_wings",
                CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < 4; i++) {
            CubeListBuilder wing = CubeListBuilder.create().texOffs(UV_ACCENT_U, UV_ACCENT_V);
            for (int step = 0; step < 4; step++) {
                float width = 4.2F - step * 0.75F;
                float depth = 2.4F - step * 0.38F;
                wing.addBox(-width / 2.0F, -10.0F - step * 2.1F, 2.9F,
                        width, 2.1F, depth);
            }
            wings.addOrReplaceChild("tier3_wing_" + i, wing,
                    PartPose.rotation(0.0F,
                            (float) (i * Math.PI / 2.0D), 0.0F));
        }

        addRing(details, "tier3_band_low", 3.12F, 15.5F, 16.7F, 0.72F,
                UV_ACCENT_U, UV_ACCENT_V);
        addRing(details, "tier3_band_mid", 3.12F, 25.5F, 26.7F, 0.72F,
                UV_ACCENT_U, UV_ACCENT_V);
        addRing(details, "tier3_band_top", 2.86F, 41.5F, 42.6F, 0.68F,
                UV_ACCENT_U, UV_ACCENT_V);
    }

    private static void addTier3Booster(PartDefinition root, String name, float z) {
        PartDefinition booster = root.addOrReplaceChild(name,
                CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, z));
        addRing(booster, name + "_bell", 0.95F, 5.5F, 8.0F, 0.6F,
                UV_ENGINE_U, UV_ENGINE_V);
        addRing(booster, name + "_body", 0.8F, 8.0F, 22.0F, 0.62F,
                UV_HULL_MID_U, UV_HULL_MID_V);
        addRing(booster, name + "_band", 0.86F, 12.0F, 13.3F, 0.55F,
                UV_ACCENT_U, UV_ACCENT_V);
        addRing(booster, name + "_nose", 0.6F, 22.0F, 27.0F, 0.45F,
                UV_ACCENT_U, UV_ACCENT_V);
    }

    private void setTier2Visible(boolean visible) {
        this.tier2BoosterLeft.visible = visible;
        this.tier2BoosterRight.visible = visible;
        this.tier2Fins.visible = visible;
        this.tier2Antenna.visible = visible;
    }

    private void setTier3Visible(boolean visible) {
        this.tier3Details.visible = visible;
    }

    @Override
    public void setupAnim(@NotNull RocketEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        setTier2Visible(entity.getTier() >= 2);
        setTier3Visible(entity.getTier() >= 3);
        // 火箭本体始终保持静止：倒计时的震动全部交给相机（RocketLaunchCameraShake），
        // 上升阶段更是不能有任何摆动，否则会被放大成“上下抖动”。
        this.root.zRot = 0.0F;
        this.root.xRot = 0.0F;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
