package net.luojiuoscar.isaac_disaster.client.model.familiar.loki_baby;

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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class LokiBabyModel extends HierarchicalModel<AbstractBabyFamiliarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "loki_baby"), "main");
    private final ModelPart root;
    private final ModelPart rig;
    private final Vector3f animationVector = new Vector3f();

    public LokiBabyModel(ModelPart root) {
        this.root = root;
        rig = root.getChild("rig_root");
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
		.texOffs(32, 28).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 50).addBox(4.0F, -13.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 50).addBox(5.0F, -14.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 50).mirror().addBox(-6.0F, -13.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(36, 50).mirror().addBox(-6.0F, -14.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 43).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(20, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 37).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition wing_left_inner_bone = body.addOrReplaceChild("wing_left_inner_bone", CubeListBuilder.create(), PartPose.offset(5.0F, 1.5F, -0.1F));

		PartDefinition wing_left_inner_r1 = wing_left_inner_bone.addOrReplaceChild("wing_left_inner_r1", CubeListBuilder.create().texOffs(0, 58).addBox(-2.0F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0123F));

		PartDefinition wing_left_outer_bone = body.addOrReplaceChild("wing_left_outer_bone", CubeListBuilder.create(), PartPose.offset(5.5F, -1.0F, -1.1F));

		PartDefinition wing_left_outer_r1 = wing_left_outer_bone.addOrReplaceChild("wing_left_outer_r1", CubeListBuilder.create().texOffs(16, 58).addBox(-2.5F, -1.5F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition wing_right_inner_bone = body.addOrReplaceChild("wing_right_inner_bone", CubeListBuilder.create(), PartPose.offset(-5.0F, 1.5F, -0.1F));

		PartDefinition wing_right_inner_r1 = wing_right_inner_bone.addOrReplaceChild("wing_right_inner_r1", CubeListBuilder.create().texOffs(32, 58).addBox(-1.0F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.0123F));

		PartDefinition wing_right_outer_bone = body.addOrReplaceChild("wing_right_outer_bone", CubeListBuilder.create(), PartPose.offset(-5.5F, -1.0F, -1.1F));

		PartDefinition wing_right_outer_r1 = wing_right_outer_bone.addOrReplaceChild("wing_right_outer_r1", CubeListBuilder.create().texOffs(48, 58).addBox(-2.5F, -1.5F, -0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

    @Override
    public @NotNull ModelPart root() { return root; }

    @Override
    public void setupAnim(AbstractBabyFamiliarEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = ageInTicks - entity.tickCount;
        setupPose(ageInTicks, entity.getAttackAge(partialTick), entity.getAttackDurationTicks());
    }

    private void setupPose(float ageInTicks, float attackAge, float duration) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float weight = attackAge < 0 || attackAge >= duration ? 0
                : Mth.clamp(Math.min(attackAge, duration - attackAge), 0, 1);
        weight = weight * weight * (3 - 2 * weight);
        long actionMillis = (long) (attackAge * 50);
        float spin = 0;
        if (attackAge >= 0 && attackAge < duration) {
            KeyframeAnimations.animate(this, LokiBabyAnimations.ATTACK, actionMillis, 1, animationVector);
            spin = rig.yRot;
            root.getAllParts().forEach(ModelPart::resetPose);
        }
        KeyframeAnimations.animate(this, LokiBabyAnimations.IDLE,
                (long) ((ageInTicks % 20.0F) * 50), 1 - weight, animationVector);
        if (weight > 0) {
            KeyframeAnimations.animate(this, LokiBabyAnimations.ATTACK, actionMillis, weight, animationVector);
        }
        // A full turn is equivalent to idle; fading its accumulated angle would unwind the spin.
        rig.yRot = spin;
    }
}
