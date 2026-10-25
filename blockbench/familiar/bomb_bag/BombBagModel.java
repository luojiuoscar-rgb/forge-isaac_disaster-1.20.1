// Blockbench model for Minecraft Forge 1.20.1 (Mojang mappings).

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BombBagModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("isaac_disaster", "bomb_bag"), "main");
	private final ModelPart rig_root;
	private final ModelPart pouch;
	private final ModelPart closure;
	private final ModelPart mouth;

	public BombBagModel(ModelPart root) {
		this.rig_root = root.getChild("rig_root");
		this.pouch = this.rig_root.getChild("pouch");
		this.closure = this.rig_root.getChild("closure");
		this.mouth = this.closure.getChild("mouth");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rig_root = partdefinition.addOrReplaceChild("rig_root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition pouch = rig_root.addOrReplaceChild("pouch", CubeListBuilder.create().texOffs(47, 21).addBox(-3.0F, 7.0F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(77, 1).addBox(-4.0F, 5.0F, -3.5F, 8.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-5.0F, -4.0F, -4.5F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(41, 1).addBox(5.0F, -3.0F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(59, 1).addBox(-6.0F, -3.0F, -3.5F, 1.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(1, 21).addBox(-4.0F, -6.0F, -3.5F, 8.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(71, 21).addBox(-3.0F, -7.0F, -2.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition closure = rig_root.addOrReplaceChild("closure", CubeListBuilder.create().texOffs(95, 21).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(1, 32).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(65, 32).addBox(-3.5F, -1.0F, -3.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition cord_end_right_r1 = closure.addOrReplaceChild("cord_end_right_r1", CubeListBuilder.create().texOffs(73, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, 1.0F, -2.5F, 0.0F, 0.0F, 0.2967F));

		PartDefinition cord_end_left_r1 = closure.addOrReplaceChild("cord_end_left_r1", CubeListBuilder.create().texOffs(59, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.0F, -2.5F, 0.0F, 0.0F, -0.1745F));

		PartDefinition mouth = closure.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(23, 32).addBox(-3.0F, -2.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(79, 32).addBox(-1.0F, -3.0F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition mouth_right_fold_r1 = mouth.addOrReplaceChild("mouth_right_fold_r1", CubeListBuilder.create().texOffs(45, 32).addBox(-2.0F, -2.0F, -2.25F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition mouth_left_fold_r1 = mouth.addOrReplaceChild("mouth_left_fold_r1", CubeListBuilder.create().texOffs(33, 21).addBox(0.0F, -3.0F, -2.5F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2094F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		rig_root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}