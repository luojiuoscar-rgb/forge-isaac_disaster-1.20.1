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

public class RelicModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("isaac_disaster", "relic"), "main");
	private final ModelPart rig_root;

	public RelicModel(ModelPart root) {
		this.rig_root = root.getChild("rig_root");
		// Keep the original UVs and scale the complete static cross around its root.
		this.rig_root.xScale = 0.625F;
		this.rig_root.yScale = 0.625F;
		this.rig_root.zScale = 0.625F;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rig_root = partdefinition.addOrReplaceChild("rig_root", CubeListBuilder.create().texOffs(1, 1).addBox(-3.5F, -8.0F, -1.5F, 7.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(11, 22).addBox(-2.5F, -9.0F, -1.5F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(29, 22).addBox(-2.5F, 8.0F, -1.5F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(23, 1).addBox(3.5F, -5.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(37, 1).addBox(-6.5F, -5.0F, -1.5F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(51, 1).addBox(6.5F, -4.0F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(1, 22).addBox(-7.5F, -4.0F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.0F, 0.0F));

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