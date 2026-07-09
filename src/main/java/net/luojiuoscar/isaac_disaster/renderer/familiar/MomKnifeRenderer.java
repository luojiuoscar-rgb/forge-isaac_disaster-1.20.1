package net.luojiuoscar.isaac_disaster.renderer.familiar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.familiar.MomKnifeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

/**
 * Renders Mom's knife familiar with the vanilla iron sword baked model and a custom tail pivot.
 */
public class MomKnifeRenderer extends EntityRenderer<MomKnifeEntity> {
    private static final ItemStack KNIFE_STACK = new ItemStack(Items.IRON_SWORD);
    private static final float MODEL_SCALE = 0.75F;
    private static final float VANILLA_SWORD_DOWN_ROLL = -135.0F;
    private static final float SWING_DIRECTION = -1.0F;
    private static final float PIVOT_X_OFFSET = 0.16F;
    private static final float PIVOT_Y_OFFSET = 0.48F;

    public MomKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull MomKnifeEntity knife, float entityYaw, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTick, knife.yRotO, knife.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.YP.rotationDegrees(knife.getVisualBladeTwist(partialTick)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(knife.getVisualPitch(partialTick) * SWING_DIRECTION));
        poseStack.mulPose(Axis.ZP.rotationDegrees(VANILLA_SWORD_DOWN_ROLL));
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);

        // The vanilla item model is centered by ItemRenderer. This local offset moves its handle tail onto our pivot.
        poseStack.translate(PIVOT_X_OFFSET, PIVOT_Y_OFFSET, 0.0F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(KNIFE_STACK, knife.level(), null, knife.getId());
        itemRenderer.render(KNIFE_STACK, ItemDisplayContext.NONE, false, poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();
        super.render(knife, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MomKnifeEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
