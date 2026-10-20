package net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MomKnifeMeshTest {
    @Test
    void bladeLengthAndHandleTailMatchTheAttackPivotAtEveryPose() {
        for (float yaw : new float[]{0, 90, 180, -90, 37}) {
            for (float pitch : new float[]{-180, -155, -90, -25, 0, 25, 90, 155}) {
                for (float twist : new float[]{0, 45, 90, 137}) {
                    MomKnifePose pose = new MomKnifePose(yaw, pitch, twist);
                    PoseStack poses = new PoseStack();
                    MomKnifeModel.applyPose(poses, pose);
                    Matrix4f rendered = poses.last().pose();
                    Vector3f tail = rendered.transformPosition(new Vector3f());
                    Vector3f tip = rendered.transformPosition(new Vector3f(0, -MomKnifeMesh.LENGTH, 0));
                    Vec3 expected = pose.bladePoint(Vec3.ZERO, MomKnifePose.BLADE_TIP_DISTANCE);
                    assertEquals(0, tail.length(), 1e-6);
                    assertEquals(expected.x, tip.x, 1e-6);
                    assertEquals(expected.y, tip.y, 1e-6);
                    assertEquals(expected.z, tip.z, 1e-6);
                }
            }
        }
    }

    @Test
    void exportedQuadsHaveMatchingNormalsAndBoundedUvs() {
        assertEquals(46, MomKnifeMesh.QUADS.length);
        for (float[] quad : MomKnifeMesh.QUADS) {
            assertEquals(23, quad.length);
            Vector3f a = vertex(quad, 0);
            Vector3f b = vertex(quad, 1);
            Vector3f c = vertex(quad, 2);
            Vector3f normal = new Vector3f(quad[20], quad[21], quad[22]);
            assertEquals(1, normal.length(), 1e-5);
            assertTrue(b.sub(a).cross(c.sub(a)).dot(normal) > 0, "inverted or missing face");
            for (int i = 0; i < 4; i++) {
                assertTrue(quad[i * 5 + 3] >= 0 && quad[i * 5 + 3] <= 1);
                assertTrue(quad[i * 5 + 4] >= 0 && quad[i * 5 + 4] <= 1);
            }
        }
    }


    @Test
    void followingEdgeFacesTheOwnerRatherThanShowingTheBroadSide() {
        for (float yaw : new float[]{0, 90, 180, -90, 37}) {
            PoseStack poses = new PoseStack();
            MomKnifeModel.applyPose(poses, new MomKnifePose(yaw, 0, 0));
            Matrix4f rendered = poses.last().pose();
            // The approved mesh has its sharp edge along local +X.
            Vector3f edge = rendered.transformDirection(new Vector3f(1, 0, 0)).normalize();
            Vec3 towardOwner = Vec3.directionFromRotation(0, yaw);
            assertEquals(towardOwner.x, edge.x, 1e-4);
            assertEquals(towardOwner.y, edge.y, 1e-4);
            assertEquals(towardOwner.z, edge.z, 1e-4);
        }
    }


    @Test
    void bladeAndEdgeStayInTheTargetsVerticalPlaneWhileAiming() {
        for (float yaw : new float[]{0, 90, 180, -90, 37}) {
            Vector3f planeNormal = new Vector3f((float) Math.cos(Math.toRadians(yaw)), 0,
                    (float) Math.sin(Math.toRadians(yaw)));
            for (float pitch : new float[]{0, 25, 90, 155, 180}) {
                PoseStack poses = new PoseStack();
                MomKnifeModel.applyPose(poses, new MomKnifePose(yaw, pitch, 0));
                Matrix4f rendered = poses.last().pose();
                Vector3f edge = rendered.transformDirection(new Vector3f(1, 0, 0)).normalize();
                Vector3f blade = rendered.transformDirection(new Vector3f(0, -1, 0)).normalize();
                assertEquals(0, edge.dot(planeNormal), 1e-6);
                assertEquals(0, blade.dot(planeNormal), 1e-6);
            }
        }
    }

    @Test
    void liftingBackwardKeepsTheCuttingEdgeBelowTheBladeAtIntermediateAngles() {
        for (float yaw : new float[]{0, 40, -40, 80, -80, 179, -179}) {
            for (float pitch : new float[]{-15, -30, -45, -60, -90, -120, -155}) {
                PoseStack poses = new PoseStack();
                MomKnifeModel.applyPose(poses, new MomKnifePose(yaw, pitch, 0));
                Vector3f edge = poses.last().pose().transformDirection(new Vector3f(1, 0, 0)).normalize();
                assertTrue(edge.y < 0, "The edge stays below the blade while lifting toward the target");
            }
        }
    }

    private static Vector3f vertex(float[] quad, int index) {
        return new Vector3f(quad[index * 5], quad[index * 5 + 1], quad[index * 5 + 2]);
    }
}
