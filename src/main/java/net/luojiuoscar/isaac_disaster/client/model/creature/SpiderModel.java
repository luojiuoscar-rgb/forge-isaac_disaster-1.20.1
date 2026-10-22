package net.luojiuoscar.isaac_disaster.client.model.creature;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.client.animation.SpiderAnimations;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.AbstractSpiderFamiliarEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/** Approved spider geometry with source-authored idle, gait and wall-climbing gait. */
public final class SpiderModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "spider"), "main");
    private final ModelPart root;
    private final ModelPart spiderRoot;
    private final ModelPart body;
    private final ModelPart bodyGeometry;
    private final ModelPart[] legs = new ModelPart[4];
    private final ModelPart[] fangs = new ModelPart[4];
    private final ModelPart[] resetParts;
    private final SpiderAnimations.Animator animations;

    public SpiderModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.root = root;
        resetParts = root.getAllParts().toArray(ModelPart[]::new);
        Map<String, ModelPart> animationParts = new HashMap<>();
        spiderRoot = root.getChild("spider_root");
        body = spiderRoot.getChild("body");
        bodyGeometry = body.getChild("body_geometry");
        String[] fangNames = {"fang_left_geometry", "fang_right_geometry", "fang_left_tip_geometry", "fang_right_tip_geometry"};
        for (int i = 0; i < fangs.length; i++) fangs[i] = body.getChild(fangNames[i]);
        animationParts.put("spider_root", spiderRoot);
        animationParts.put("body", body);
        String[] legNames = {"leg_left_1", "leg_left_4", "leg_right_1", "leg_right_4"};
        for (int i = 0; i < legs.length; i++) {
            String leg = legNames[i];
            ModelPart part = legs[i] = spiderRoot.getChild(leg);
            animationParts.put(leg, part);
            animationParts.put(leg + "_knee", part.getChild(leg + "_knee"));
        }
        animations = new SpiderAnimations.Animator(animationParts);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        // Preserve the source pivots. Java model Y points down from the 24-unit living origin.
        // Reflecting Y also negates the source X/Z rotation angles; Y rotation keeps its sign.
        PartDefinition spiderRoot = mesh.getRoot().addOrReplaceChild("spider_root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 22.9F, 0.0F));
        PartDefinition body = spiderRoot.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.ZERO);
        body.addOrReplaceChild("body_geometry", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
        body.addOrReplaceChild("fang_left_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, 0.0F, -0.09F, 0.32634F, 0.0F, 0.18F, Set.of(Direction.UP, Direction.DOWN)),
                PartPose.offsetAndRotation(-0.35F, 0.7F, -0.94F, 0.0F, 0.921826537F, 2.609868672F));
        body.addOrReplaceChild("fang_right_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, 0.0F, -0.09F, 0.32634F, 0.0F, 0.18F, Set.of(Direction.UP, Direction.DOWN)),
                PartPose.offsetAndRotation(0.35F, 0.7F, -0.94F, 0.0F, 0.921826537F, 0.531723981F));
        body.addOrReplaceChild("fang_left_tip_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, 0.0F, -0.08F, 0.28705F, 0.0F, 0.16F, Set.of(Direction.UP, Direction.DOWN)),
                PartPose.offsetAndRotation(-0.52F, 0.8F, -1.2F, 0.0F, 0.677772959F, 0.179853561F));
        body.addOrReplaceChild("fang_right_tip_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, 0.0F, -0.08F, 0.28705F, 0.0F, 0.16F, Set.of(Direction.UP, Direction.DOWN)),
                PartPose.offsetAndRotation(0.52F, 0.8F, -1.2F, 0.0F, 0.677772959F, 2.961739092F));
        PartDefinition legLeft1 = spiderRoot.addOrReplaceChild("leg_left_1", CubeListBuilder.create(),
                PartPose.offset(-0.88F, -0.5F, -0.55F));
        PartDefinition legLeft1Knee = legLeft1.addOrReplaceChild("leg_left_1_knee", CubeListBuilder.create(),
                PartPose.offset(-0.52F, -0.55F, -0.4F));
        legLeft1Knee.addOrReplaceChild("leg_left_1_lower_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 2.21041F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.346176981F, 1.788943634F));
        legLeft1.addOrReplaceChild("leg_left_1_upper_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 0.8561F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.486163558F, -2.328164502F));
        PartDefinition legLeft4 = spiderRoot.addOrReplaceChild("leg_left_4", CubeListBuilder.create(),
                PartPose.offset(-0.88F, -0.5F, 0.55F));
        PartDefinition legLeft4Knee = legLeft4.addOrReplaceChild("leg_left_4_knee", CubeListBuilder.create(),
                PartPose.offset(-0.92F, -0.55F, 0.4F));
        legLeft4Knee.addOrReplaceChild("leg_left_4_lower_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 2.13797F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.14078489F, 1.858180671F));
        legLeft4.addOrReplaceChild("leg_left_4_upper_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 1.14407F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.357174476F, -2.602773211F));
        PartDefinition legRight1 = spiderRoot.addOrReplaceChild("leg_right_1", CubeListBuilder.create(),
                PartPose.offset(0.88F, -0.5F, -0.55F));
        PartDefinition legRight1Knee = legRight1.addOrReplaceChild("leg_right_1_knee", CubeListBuilder.create(),
                PartPose.offset(0.52F, -0.55F, -0.4F));
        legRight1Knee.addOrReplaceChild("leg_right_1_lower_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 2.21041F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.346176981F, 1.35264902F));
        legRight1.addOrReplaceChild("leg_right_1_upper_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 0.8561F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.486163558F, -0.813428151F));
        PartDefinition legRight4 = spiderRoot.addOrReplaceChild("leg_right_4", CubeListBuilder.create(),
                PartPose.offset(0.88F, -0.5F, 0.55F));
        PartDefinition legRight4Knee = legRight4.addOrReplaceChild("leg_right_4_knee", CubeListBuilder.create(),
                PartPose.offset(0.92F, -0.55F, 0.4F));
        legRight4Knee.addOrReplaceChild("leg_right_4_lower_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 2.13797F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.14078489F, 1.283411983F));
        legRight4.addOrReplaceChild("leg_right_4_upper_geometry", CubeListBuilder.create().texOffs(8, 0)
                        .addBox(0.0F, -0.13F, -0.13F, 1.14407F, 0.26F, 0.26F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.357174476F, -0.538819443F));
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        resetPose();
        if (!(entity instanceof AbstractSpiderFamiliarEntity spider)) return;
        float partialTick = ageInTicks - entity.tickCount;
        SpiderAnimations.Clip clip = SpiderAnimations.movementClip(spider.getMovementAnimation());
        float seconds = clip == SpiderAnimations.Clip.IDLE ? ageInTicks / 20 : spider.getGaitAnimationTime(partialTick);
        animations.apply(clip, seconds);
    }

    /** Also used to inspect authored poses without requiring a live entity or world. */
    public void applyAnimation(SpiderAnimations.Clip clip, float seconds) {
        resetPose();
        animations.apply(clip, seconds);
    }

    private void resetPose() {
        for (ModelPart part : resetParts) part.resetPose();
    }

    /** Only the neutral body is RGB-tinted; black legs and fangs remain unchanged. */
    @Override
    public void renderToBuffer(@NotNull PoseStack poses, @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        if (!root.visible || !spiderRoot.visible) return;
        poses.pushPose();
        root.translateAndRotate(poses);
        spiderRoot.translateAndRotate(poses);
        if (body.visible) {
            poses.pushPose();
            body.translateAndRotate(poses);
            bodyGeometry.render(poses, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            for (ModelPart fang : fangs) fang.render(poses, buffer, packedLight, packedOverlay, 1, 1, 1, alpha);
            poses.popPose();
        }
        for (ModelPart leg : legs) leg.render(poses, buffer, packedLight, packedOverlay, 1, 1, 1, alpha);
        poses.popPose();
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
