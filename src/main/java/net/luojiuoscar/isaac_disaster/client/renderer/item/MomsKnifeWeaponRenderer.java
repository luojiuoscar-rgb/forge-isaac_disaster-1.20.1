package net.luojiuoscar.isaac_disaster.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife.MomKnifeModel;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class MomsKnifeWeaponRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/mom_knife.png");
    private final MomKnifeModel<Entity> model = new MomKnifeModel<>();

    public MomsKnifeWeaponRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, @NotNull PoseStack poses,
                             @NotNull MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        applyModelPose(poses, context);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)), light, overlay, 1, 1, 1, 1);
        poses.popPose();
    }

    static void applyModelPose(PoseStack poses, ItemDisplayContext context) {
        poses.translate(0.5, 0.5, 0.5);
        boolean hand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (hand) {
            float twist = switch (context) {
                case FIRST_PERSON_RIGHT_HAND -> 120;
                case FIRST_PERSON_LEFT_HAND -> -120;
                default -> -90;
            };
            poses.mulPose(Axis.YP.rotationDegrees(twist));
            MomKnifeModel.applyPose(poses, PlayerMomsKnifeMotion.pose(new Vec3(0, 1, 0), 0));
            // The mesh's handle runs from Y=0 to Y=-7; center it on the native hand pivot.
            poses.translate(0, 3.5, 0);
        } else {
            boolean gui = context == ItemDisplayContext.GUI;
            if (context == ItemDisplayContext.GROUND) {
                poses.scale(0.5F, 0.5F, 0.5F);
            } else {
                poses.mulPose(Axis.ZP.rotationDegrees(gui ? -35 : -145));
                poses.mulPose(Axis.YP.rotationDegrees(-20));
            }
            poses.mulPose((gui ? Axis.XP : Axis.ZP).rotationDegrees(180));
            poses.scale(MomKnifeModel.MODEL_SCALE, MomKnifeModel.MODEL_SCALE, MomKnifeModel.MODEL_SCALE);
            poses.translate(-1, 13.25, 0);
        }
    }
}
