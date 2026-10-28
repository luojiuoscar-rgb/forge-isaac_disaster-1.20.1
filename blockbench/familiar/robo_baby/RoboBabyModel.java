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

public class RoboBabyModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("isaac_disaster", "robo_baby"), "main");
	private final ModelPart rig_root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart arm_left;
	private final ModelPart arm_right;
	private final ModelPart tail;
	private final ModelPart tail_tip;

	public RoboBabyModel(ModelPart root) {
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

		PartDefinition body = rig_root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(64, 0).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(64, 32).addBox(-4.0F, -1.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(94, 32).addBox(-3.0F, 4.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(64, 45).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(44, 0).addBox(6.0F, -7.0F, -4.0F, 1.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-6.0F, -9.0F, -5.0F, 12.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(44, 14).addBox(-7.0F, -7.0F, -4.0F, 1.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(32, 28).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(64, 10).addBox(-5.0F, -7.0F, -6.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(116, 32).mirror().addBox(2.0F, -7.0F, -6.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(76, 10).addBox(-4.0F, -8.0F, -6.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(96, 10).addBox(-1.0F, -7.0F, -6.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(104, 10).addBox(-3.0F, -1.0F, -6.0F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 18).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(74, 18).addBox(-0.5F, -14.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 18).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition arm_left = body.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(84, 45).addBox(1.0F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(116, 40).mirror().addBox(-1.5F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -1.0F, 0.0F));

		PartDefinition arm_right = body.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(98, 45).addBox(-4.0F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(106, 18).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 43).addBox(-2.0F, 3.0F, -1.5F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(64, 24).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition tail_tip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(20, 38).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 37).addBox(-0.5F, 5.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

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