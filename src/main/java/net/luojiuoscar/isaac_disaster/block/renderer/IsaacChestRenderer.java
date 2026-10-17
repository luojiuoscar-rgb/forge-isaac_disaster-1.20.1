package net.luojiuoscar.isaac_disaster.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.IsaacChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.ItemChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class IsaacChestRenderer implements BlockEntityRenderer<IsaacChestBlockEntity>, ItemRenderable  {

    private final BlockRenderDispatcher blockRenderer;

    public IsaacChestRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(IsaacChestBlockEntity chest, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Level level = chest.getLevel();
        if (level == null) return;

        BlockState state = chest.getBlockState();
        if (state.getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
            // Entity shading uses transformed normals, including while the lid opens.
            // The base model is already rotated by its blockstate; do not rotate it again.
            VertexConsumer vertices = buffer.getBuffer(Sheets.cutoutBlockSheet());
            blockRenderer.getModelRenderer().renderModel(
                    poseStack.last(), vertices, state, blockRenderer.getBlockModel(state),
                    1.0F, 1.0F, 1.0F, light, overlay
            );

            ResourceLocation lidLocation = chest.getLidResourceLocation();
            if (lidLocation != null) {
                BakedModel lidModel = Minecraft.getInstance().getModelManager().getModel(lidLocation);
                poseStack.pushPose();
                applyLidTransform(poseStack, state.getValue(BlockStateProperties.HORIZONTAL_FACING),
                        chest.getOpenNess(partialTick));
                blockRenderer.getModelRenderer().renderModel(
                        poseStack.last(), vertices, state, lidModel,
                        1.0F, 1.0F, 1.0F, light, overlay
                );
                poseStack.popPose();
            }
        }


        // ======= 物品渲染部分 =======
        if (chest instanceof ItemChestBlockEntity itemChest && itemChest.isDisplayingItem()){
            renderItem(itemChest.getItem(0), poseStack, itemChest.getRenderingRotation(), level, buffer,
                    chest.getBlockPos(), 1.0f, 0.6f);
        }
    }

    static void applyLidTransform(PoseStack poseStack, Direction facing, float openness) {
        float closed = 1.0F - openness;
        float easedOpenness = 1.0F - closed * closed * closed;

        poseStack.translate(0.5, 0.5, 0.5);
        float degree = facing.toYRot();
        degree = degree % 180f == 0 ? (degree + 180) % 360f : degree;
        poseStack.mulPose(Axis.YP.rotationDegrees(degree));
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.translate(0.5, 0.45, 0.875);
        poseStack.mulPose(Axis.XP.rotationDegrees(easedOpenness * 90F));
        poseStack.translate(-0.5, -0.45, -0.875);
    }



}
