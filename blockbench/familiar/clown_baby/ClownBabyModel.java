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

public class ClownBabyModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("isaac_disaster", "clown_baby"), "main");
	private final ModelPart rig_root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart arm_left;
	private final ModelPart arm_right;
	private final ModelPart tail;
	private final ModelPart tail_tip;

	public ClownBabyModel(ModelPart root) {
		this.rig_root = root.getChild("rig_root");
		this.body = this.rig_root.getChild("body");
		this.head = this.body.getChild("head");
		this.arm_left = this.body.getChild("arm_left");
		this.arm_right = this.body.getChild("arm_right");
		this.tail = this.body.getChild("tail");
		this.tail_tip = this.tail.getChild("tail_tip");
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
		.texOffs(0, 50).addBox(-6.0F, -9.0F, -7.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 50).addBox(-2.0F, -8.0F, -6.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 50).addBox(-7.0F, -8.0F, -6.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 50).addBox(2.0F, -9.0F, -7.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 50).addBox(1.0F, -8.0F, -6.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 50).addBox(6.0F, -8.0F, -6.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 50).addBox(-3.0F, -4.0F, -7.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 50).addBox(-3.0F, -2.0F, -7.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 50).addBox(-3.0F, -3.0F, -7.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 50).addBox(2.0F, -3.0F, -7.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 59).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition arm_left = body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(32, 20).addBox(0.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -1.0F, 0.0F));

		PartDefinition arm_right = body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(50, 37).addBox(-2.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 43).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(20, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 37).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		rig_root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}