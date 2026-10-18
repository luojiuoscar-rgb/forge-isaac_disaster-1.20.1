package net.luojiuoscar.isaac_disaster.client.model.creature;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.AbstractFlyFamiliarEntity;
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
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** Vanilla rigid parts with procedural flight, independent of server tick animation state. */
public final class FlyModel<T extends AbstractFlyFamiliarEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "fly"), "main");
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final float WING_BEATS_PER_SECOND = 8.0F;
    private static final float HOVER_CYCLES_PER_SECOND = 2.0F;
    private static final float WING_MIDDLE = (float) Math.toRadians(-15.0);
    private static final float WING_AMPLITUDE = (float) Math.toRadians(45.0);
    private static final float HOVER_AMPLITUDE = 0.06F;

    private final ModelPart root;
    private final ModelPart flyRoot;
    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public FlyModel(ModelPart root) {
        // Opposite wing faces are explicit, so cull back faces rather than drawing both twice.
        super(RenderType::entityCutout);
        this.root = root;
        this.flyRoot = root.getChild("fly_root");
        this.body = flyRoot.getChild("body");
        this.leftWing = flyRoot.getChild("wing_left");
        this.rightWing = flyRoot.getChild("wing_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition fly = mesh.getRoot().addOrReplaceChild("fly_root", CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        fly.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.ZERO);
        Set<Direction> wingFaces = Set.of(Direction.UP, Direction.DOWN);
        fly.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 4)
                        .addBox(0.0F, 0.0F, -1.0F, 3.0F, 0.0F, 2.0F, wingFaces),
                PartPose.offset(0.85F, -0.825F, 0.0F));
        fly.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 4).mirror()
                        .addBox(-3.0F, 0.0F, -1.0F, 3.0F, 0.0F, 2.0F, wingFaces),
                PartPose.offset(-0.85F, -0.825F, 0.0F));
        // The 16x16 PNG has two pixels per logical UV unit; geometry remains 2x2x2.
        return LayerDefinition.create(mesh, 8, 8);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        // Wrap time before trigonometry to retain smooth partial-tick motion in long sessions.
        double seconds = (ageInTicks % TICKS_PER_SECOND) / TICKS_PER_SECOND;
        float wing = WING_MIDDLE - WING_AMPLITUDE
                * (float) Math.cos(seconds * Math.PI * 2.0 * WING_BEATS_PER_SECOND);
        leftWing.zRot = wing;
        rightWing.zRot = -wing;
        flyRoot.y -= HOVER_AMPLITUDE
                * (float) Math.sin(seconds * Math.PI * 2.0 * HOVER_CYCLES_PER_SECOND);
    }

    /** Applies the supplied RGB tint to the body only, preserving the approved wing texture. */
    @Override
    public void renderToBuffer(@NotNull PoseStack poses, @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        if (!root.visible || !flyRoot.visible) return;
        poses.pushPose();
        root.translateAndRotate(poses);
        flyRoot.translateAndRotate(poses);
        body.render(poses, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftWing.render(poses, buffer, packedLight, packedOverlay, 1, 1, 1, alpha);
        rightWing.render(poses, buffer, packedLight, packedOverlay, 1, 1, 1, alpha);
        poses.popPose();
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
