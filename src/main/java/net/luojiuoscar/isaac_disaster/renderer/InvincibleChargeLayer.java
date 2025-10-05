package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class InvincibleChargeLayer<T extends Player, M extends net.minecraft.client.model.HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation POWER_TEXTURE = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,"textures/entity/player/invincible_armor.png");

    public InvincibleChargeLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (!shouldRenderPower(player)) return;

        var vertexConsumer = buffer.getBuffer(RenderType.energySwirl(
                POWER_TEXTURE,
                player.tickCount * 0.01F, // X移动速度（控制电光流动）
                player.tickCount * 0.01F  // Y移动速度
        ));

        getParentModel().renderToBuffer(poseStack, vertexConsumer, light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean shouldRenderPower(Player player) {
        if (player == null || player.isInvisible()) return false;

        return player.getEffect(ModEffects.INVINCIBLE.get()) != null;
    }
}
