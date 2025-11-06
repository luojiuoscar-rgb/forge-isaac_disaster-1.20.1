package net.luojiuoscar.isaac_disaster.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.block.custom.PedestalBlock;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Objects;

public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity> {

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(@NotNull PedestalBlockEntity pedestal, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        Level level = pedestal.getLevel();
        if (level == null) {
            return;
        }

        // ====== ItemDisplay ======
        ItemStack stack = pedestal.isLocked()
                ? new ItemStack(ModItems.LOCK.get())
                : pedestal.getItem();

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.75, 0.5f);
        poseStack.scale(0.6f, 0.6f, 0.6f);
        poseStack.mulPose(Axis.YP.rotationDegrees(pedestal.getRenderingRotation()));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(Objects.requireNonNull(pedestal.getLevel()), pedestal.getBlockPos()),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, pedestal.getLevel(), 1);
        poseStack.popPose();


        // ======= shop =======
        // 生命成本优先
        if (!pedestal.isDecoration() && (pedestal.getLiftCost() != 0 || pedestal.getMoneyCost() != 0)){
            poseStack.pushPose();

            // 获取方块朝向
            Direction facing = pedestal.getBlockState().hasProperty(PedestalBlock.FACING)
                    ? pedestal.getBlockState().getValue(PedestalBlock.FACING)
                    : Direction.NORTH;
            poseStack.translate(0.5, 0.4, 0.5);

            switch (facing) {
                case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
                case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
                case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
                case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                default -> {}
            }

            poseStack.translate(-0.1, 0.1, -0.35f);
            poseStack.scale(0.3f, 0.3f, 0.3f); // 缩小显示

            String cost = "";

            if (pedestal.getLiftCost() != 0) {
                ItemStack heartStack = new ItemStack(ModItems.RED_HEART.get());

                itemRenderer.renderStatic(heartStack, ItemDisplayContext.FIXED,
                        getLightLevel(level, pedestal.getBlockPos()), OverlayTexture.NO_OVERLAY,
                        poseStack, bufferSource, level, 1);

                cost = String.valueOf(pedestal.getLiftCost() * StatManager.MAX_HEALTH.getBonus());

            } else { // 金钱成本仅在 liftCost == 0 时生效
                ItemStack coinStack = LevelHelper.getMoney(1).get(0);

                itemRenderer.renderStatic(coinStack, ItemDisplayContext.FIXED,
                        getLightLevel(level, pedestal.getBlockPos()), OverlayTexture.NO_OVERLAY,
                        poseStack, bufferSource, level, 0);

                cost = String.valueOf(pedestal.getMoneyCost());
            }
            // 渲染数字
            double xOffset = -0.5;
            xOffset -= 0.2 * cost.length();

            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(xOffset, -0.15, 0);
            poseStack.scale(0.04f, 0.04f, 0.04f);

            PoseStack.Pose lastPose = poseStack.last();
            Matrix4f matrix = lastPose.pose();

            Component comp = Component.literal(cost);
            Minecraft.getInstance().font.drawInBatch(
                    comp.getVisualOrderText(),
                    0,
                    0,
                    0xFFFFFFFF,
                    false,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    getLightLevel(level, pedestal.getBlockPos())
            );
            if (bufferSource instanceof MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }

            poseStack.popPose();
            poseStack.popPose();
        }
    }

    private int getLightLevel(Level level, BlockPos pos){
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }






















}
