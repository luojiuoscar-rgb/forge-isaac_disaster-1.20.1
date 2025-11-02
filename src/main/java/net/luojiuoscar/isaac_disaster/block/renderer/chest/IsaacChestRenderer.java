package net.luojiuoscar.isaac_disaster.block.renderer.chest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.IsaacChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.ItemChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class IsaacChestRenderer implements BlockEntityRenderer<IsaacChestBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public IsaacChestRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(IsaacChestBlockEntity chest, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Level level = chest.getLevel();
        if (level == null) return;

        BlockState state = chest.getBlockState();
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);


        // 箱盖开合角度
        float openness = chest.getOpenNess(partialTick);
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;

        poseStack.pushPose();

        // ======= 箱子部分 =======
        // 旋转方块朝向&旋转
        poseStack.translate(0.5, 0.5, 0.5);
        float degree = facing.toYRot();
        degree = degree % 180f == 0 ? (degree + 180) % 360f : degree;
        poseStack.mulPose(Axis.YP.rotationDegrees(degree));
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.translate(0.5, 0.45, 0.875);
        poseStack.mulPose(Axis.XP.rotationDegrees(openness * 90F)); // 绕 X 轴旋转
        poseStack.translate(-0.5, -0.45, -0.875);


        ResourceLocation rl = chest.getLidResourceLocation();
        // 获取盖子模型
        BakedModel lidModel = Minecraft.getInstance().getModelManager().getModel(rl);

        // 使用世界光照
        int blockLight = chest.getLevel().getBrightness(LightLayer.BLOCK, chest.getBlockPos());
        int skyLight = chest.getLevel().getBrightness(LightLayer.SKY, chest.getBlockPos());
        int packedLight = LightTexture.pack(blockLight, skyLight);

        // 渲染盖子
        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.solid()),
                state,
                lidModel,
                1.0F, 1.0F, 1.0F,
                packedLight, overlay
        );
        poseStack.popPose();


        // ======= 物品渲染部分 =======
        if (chest instanceof ItemChestBlockEntity itemChest && itemChest.isDisplayingItem()){
            poseStack.pushPose();
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            ItemStack stack = itemChest.getItem(0); // 使用第0位Item为显示目标

            poseStack.translate(0.5f, 1.0, 0.5f);
            poseStack.scale(0.6f, 0.6f, 0.6f);
            poseStack.mulPose(Axis.YP.rotationDegrees(itemChest.getRenderingRotation()));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(level, chest.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 1);

            poseStack.popPose();
        }


    }

    private int getLightLevel(Level level, BlockPos pos){
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }

}
