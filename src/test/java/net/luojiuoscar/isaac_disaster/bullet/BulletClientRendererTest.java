package net.luojiuoscar.isaac_disaster.bullet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulletClientRendererTest {
    @Test
    void afterWeatherPoseContainsOnlyCameraRelativeTranslation() {
        PoseStack poseStack = BulletClientRenderer.afterWeatherPose(new Vec3(3.5D, -2.0D, 7.25D));
        Matrix4f matrix = poseStack.last().pose();

        assertEquals(1.0F, matrix.m00());
        assertEquals(1.0F, matrix.m11());
        assertEquals(1.0F, matrix.m22());
        assertEquals(3.5F, matrix.m30());
        assertEquals(-2.0F, matrix.m31());
        assertEquals(7.25F, matrix.m32());
    }
}
