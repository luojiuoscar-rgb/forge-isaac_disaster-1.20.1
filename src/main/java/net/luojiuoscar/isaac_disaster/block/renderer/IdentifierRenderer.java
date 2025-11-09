package net.luojiuoscar.isaac_disaster.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.IdentifierBlockEntity;
import net.luojiuoscar.isaac_disaster.item.block.IdentifierBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

public class IdentifierRenderer implements BlockEntityRenderer<IdentifierBlockEntity> {

    private final BlockRenderDispatcher blockRenderDispatcher;

    public IdentifierRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(IdentifierBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        LocalPlayer player = Minecraft.getInstance().player;
        if (entity.getModelResource() == null || player == null) return;
        // 手持任意identifier时显示
        if (!(player.getMainHandItem().getItem() instanceof IdentifierBlockItem ||
                player.getOffhandItem().getItem() instanceof IdentifierBlockItem)) return;

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(entity.getModelResource());
        BlockState state = entity.getBlockState();

        // ===== 绘制方块 =====
        poseStack.pushPose();

        blockRenderDispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()),
                state,
                model,
                1f, 1f, 1f,
                0xF000F0,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();

        // ========= 渲染放大的边界 =========
        double range = 3.0;
        float scale = (float) ((range * 2 + 1));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5); // 移动到方块中心
        poseStack.scale(scale, scale, scale); // 按范围放大
        poseStack.translate(-0.5, -0.5, -0.5); // 移回原点

        // 使用线框或半透明渲染
        blockRenderDispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.solid()),
                state,
                model,
                1f, 1f, 1f,
                0xF000F0,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();

    }
}
