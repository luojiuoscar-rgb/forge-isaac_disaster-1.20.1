package net.luojiuoscar.isaac_disaster.client.flight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.capability.player.flight.PlayerIsaacFlightProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;

/** Adds the visual Elytra-like body tilt without entering Minecraft's real Elytra state. */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class IsaacFlightRenderHandler {
    private IsaacFlightRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity().isFallFlying()
                || event.getEntity().getPose() != net.minecraft.world.entity.Pose.FALL_FLYING
                || !event.getEntity().getCapability(PlayerIsaacFlightProvider.PLAYER_ISAAC_FLIGHT)
                .map(state -> state.isThrusting()).orElse(false)) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        poseStack.mulPose(Axis.XP.rotationDegrees(event.getEntity().getXRot()));
    }
}
