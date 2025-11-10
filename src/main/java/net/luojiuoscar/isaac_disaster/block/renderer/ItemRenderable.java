package net.luojiuoscar.isaac_disaster.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

public interface ItemRenderable<T> {
    default void renderItem(@NotNull ItemStack stack, PoseStack poseStack, float rotation, Level level, MultiBufferSource buffer,
                            BlockPos pos, float yOffset, float scale) {
        poseStack.pushPose();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.translate(0.5f, yOffset, 0.5f);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(ModEffects.CURSE_OF_THE_BLIND.get())){
            stack = new ItemStack(ModItems.QUESTION_MARK.get());
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(level, pos),
                OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 1);

        poseStack.popPose();
    }

    default int getLightLevel(Level level, BlockPos pos){
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }

}
