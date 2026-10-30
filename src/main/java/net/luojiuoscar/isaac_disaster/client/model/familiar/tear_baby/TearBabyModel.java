package net.luojiuoscar.isaac_disaster.client.model.familiar.tear_baby;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.util.Mth;

/** Shared exported Bobby/Maggy rig, rather than a required model for all tear babies. */
public class TearBabyModel extends HierarchicalModel<AbstractBabyFamiliarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "tear_baby"), "main");
    private final ModelPart root;
    private final Vector3f animationVector = new Vector3f();
    private final ModelPart body;
    private final ModelPart head;
    private final Quaternionf bodyRotation = new Quaternionf();
    private final Quaternionf headRotation = new Quaternionf();
    private final Quaternionf composedRotation = new Quaternionf();

    public TearBabyModel(ModelPart root) {
        this.root = root;
        body = root.getChild("rig_root").getChild("body");
        head = body.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition rig_root = partdefinition.addOrReplaceChild("rig_root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = rig_root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 37).addBox(-3.0F, -3.0F, -2.5F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
        .texOffs(0, 29).addBox(-4.0F, -1.0F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
        .texOffs(0, 38).addBox(-3.0F, 2.0F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(44, 0).addBox(6.0F, -9.0F, -4.0F, 1.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(0, 0).addBox(-6.0F, -11.0F, -5.0F, 12.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
        .texOffs(44, 14).addBox(-7.0F, -9.0F, -4.0F, 1.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(32, 28).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition arm_left = body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(32, 20).addBox(0.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -1.0F, 0.0F));

        PartDefinition arm_right = body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(50, 37).addBox(-2.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 43).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(20, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(60, 37).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(AbstractBabyFamiliarEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = ageInTicks - entity.tickCount;
        setupPose(ageInTicks, entity.getAttackAge(partialTick), netHeadYaw, headPitch);
    }

    void setupPose(float ageInTicks, float attackAge, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        long timeMillis = (long) ((ageInTicks % 20.0F) * 50.0F);
        float duration = AbstractBabyFamiliarEntity.ATTACK_DURATION_TICKS;
        float weight = attackAge < 0 || attackAge >= duration ? 0 : Mth.clamp(Math.min(attackAge, duration - attackAge), 0, 1);
        weight = weight * weight * (3 - 2 * weight);
        KeyframeAnimations.animate(this, TearBabyAnimations.IDLE, timeMillis, 1 - weight, animationVector);
        if (weight > 0) {
            KeyframeAnimations.animate(this, TearBabyAnimations.ATTACK, (long) (attackAge * 80), weight, animationVector);
        }
        // Apply gaze in the neutral body frame, preserving the full animated global head rotation.
        bodyRotation.rotationZYX(body.zRot, body.yRot, body.xRot);
        headRotation.rotationZYX(head.zRot, head.yRot, head.xRot);
        composedRotation.set(bodyRotation).invert()
                .rotateYXZ(Mth.clamp(netHeadYaw, -60, 60) * Mth.DEG_TO_RAD,
                        Mth.clamp(headPitch, -75, 75) * Mth.DEG_TO_RAD, 0)
                .mul(bodyRotation).mul(headRotation);
        // Decompose Rz * Ry * Rx, the same order used by ModelPart.
        float x = composedRotation.x, y = composedRotation.y, z = composedRotation.z, w = composedRotation.w;
        head.xRot = (float) Math.atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));
        head.yRot = (float) Math.asin(Mth.clamp(2 * (w * y - z * x), -1, 1));
        head.zRot = (float) Math.atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));
    }
}
