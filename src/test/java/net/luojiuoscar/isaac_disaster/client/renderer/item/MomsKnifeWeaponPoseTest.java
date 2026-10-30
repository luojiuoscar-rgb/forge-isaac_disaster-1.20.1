package net.luojiuoscar.isaac_disaster.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MomsKnifeWeaponPoseTest {
    @Test
    void allHeldViewsPutTheHandleMidpointAtTheNativeGrip() {
        for (var context : new ItemDisplayContext[]{ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                ItemDisplayContext.THIRD_PERSON_LEFT_HAND}) {
            var poses = new PoseStack();
            MomsKnifeWeaponRenderer.applyModelPose(poses, context);
            var grip = poses.last().pose().transformPosition(new Vector3f(0, -3.5F, 0));
            assertEquals(0.5F, grip.x, 1e-6, context.toString());
            assertEquals(0.5F, grip.y, 1e-6, context.toString());
            assertEquals(0.5F, grip.z, 1e-6, context.toString());
        }
    }

    @Test
    void theInventoryModelIsCenteredDiagonalAndPresentsTheBladeFace() {
        var poses = new PoseStack();
        MomsKnifeWeaponRenderer.applyModelPose(poses, ItemDisplayContext.GUI);
        var center = poses.last().pose().transformPosition(new Vector3f(1, -13.25F, 0));
        assertEquals(0.5F, center.x, 1e-6);
        assertEquals(0.5F, center.y, 1e-6);
        var handle = poses.last().pose().transformPosition(new Vector3f(0, -3.5F, 0));
        var tip = poses.last().pose().transformPosition(new Vector3f(0, -26.5F, 0));
        assertTrue(tip.x > handle.x && tip.y > handle.y, "The GUI tip points up and right from the handle");
        var edge = poses.last().pose().transformDirection(new Vector3f(1, 0, 0)).normalize();
        assertTrue(edge.x > 0 && edge.y < 0, "The cutting edge lies on the lower-right side");
        var face = poses.last().normal().transform(new Vector3f(0, 0, 1)).normalize();
        assertTrue(Math.abs(face.z) > 0.8, "The broad blade face is visible rather than edge-on");
    }

    @Test
    void theGroundModelIsUprightAtHalfItsPreviousSize() {
        var poses = new PoseStack();
        MomsKnifeWeaponRenderer.applyModelPose(poses, ItemDisplayContext.GROUND);
        var tail = poses.last().pose().transformPosition(new Vector3f(0, 0, 0));
        var tip = poses.last().pose().transformPosition(new Vector3f(0, -26.5F, 0));
        assertEquals(tail.x, tip.x, 1e-6);
        assertEquals(tail.z, tip.z, 1e-6);
        assertEquals(0.42F, tip.y - tail.y, 1e-6);
    }

    @Test
    void theThirdPersonSwordFrameHoldsTheBladeForwardWithItsCuttingEdgeDown() {
        for (boolean left : new boolean[]{false, true}) {
            var poses = new PoseStack();
            poses.mulPose(Axis.XP.rotationDegrees(-90));
            poses.mulPose(Axis.YP.rotationDegrees(180));
            new ItemTransform(new Vector3f(0, left ? 90 : -90, left ? -10 : 10), new Vector3f(),
                    new Vector3f(0.85F)).apply(left, poses);
            MomsKnifeWeaponRenderer.applyModelPose(poses, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            var direction = poses.last().pose().transformDirection(new Vector3f(0, -1, 0)).normalize();
            var edge = poses.last().pose().transformDirection(new Vector3f(1, 0, 0)).normalize();
            assertTrue(direction.z < -0.95 && Math.abs(direction.y) < 0.3, "The blade projects in front of the hand");
            assertTrue(edge.y > 0.95, "Positive model Y is downward in the humanoid arm frame");
        }
    }
}
