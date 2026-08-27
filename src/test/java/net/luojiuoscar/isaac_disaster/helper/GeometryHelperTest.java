package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeometryHelperTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void convertsRotationToMainAxisAndBack() {
        Vec3 mainAxis = GeometryHelper.mainAxisFromRotation(20.0f, -35.0f);
        GeometryHelper.Rotation rotation = GeometryHelper.rotationFromMainAxis(mainAxis);

        assertEquals(20.0, rotation.xRot(), 1.0E-2);
        assertEquals(-35.0, rotation.yRot(), 1.0E-2);
        assertEquals(1.0, mainAxis.lengthSqr(), EPSILON);
    }

    @Test
    void projectsPerpendicularAxisToDeterministicPlaneTangent() {
        Vec3 normal = new Vec3(0.0, 1.0, 0.0);
        Vec3 projected = GeometryHelper.projectOntoPlane(normal, normal);

        assertEquals(0.0, projected.dot(normal), EPSILON);
        assertEquals(1.0, projected.lengthSqr(), EPSILON);
    }
}
