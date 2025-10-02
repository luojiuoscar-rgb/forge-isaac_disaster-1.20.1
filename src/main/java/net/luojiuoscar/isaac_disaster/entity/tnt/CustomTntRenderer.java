package net.luojiuoscar.isaac_disaster.entity.tnt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;


public class CustomTntRenderer extends EntityRenderer<IsaacBomb> {

    private final BlockRenderDispatcher blockRenderer;

    public CustomTntRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(IsaacBomb bomb, float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);

        float customScale = bomb.getScale();
        poseStack.scale(customScale, customScale, customScale);

        int fuse = bomb.getFuse();
        if ((float) fuse - partialTicks + 1.0F < 10.0F) {
            float f = 1.0F - ((float) fuse - partialTicks + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float fuseScale = 1.0F + f * 0.3F;
            poseStack.scale(fuseScale, fuseScale, fuseScale);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        blockRenderer.renderSingleBlock(
                Blocks.TNT.defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        if (fuse / 5 % 2 == 0) {
            TntMinecartRenderer.renderWhiteSolidBlock(
                    blockRenderer,
                    Blocks.TNT.defaultBlockState(),
                    poseStack,
                    buffer,
                    packedLight,
                    true
            );
        }

        poseStack.popPose();

        super.render(bomb, yaw, partialTicks, poseStack, buffer, packedLight);
    }


    @Override
    public ResourceLocation getTextureLocation(IsaacBomb bomb) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

