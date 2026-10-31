package net.luojiuoscar.isaac_disaster.client.model.familiar.robo_baby_2;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoboBaby2ModelTest {
    @Test
    void robotTwoRetainsBothEyesAndAntennaOnItsOwnHead() throws Exception {
        var head = model().root().getChild("rig_root").getChild("body").getChild("head");
        Field field = ModelPart.class.getDeclaredField("cubes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ModelPart.Cube> cubes = (List<ModelPart.Cube>) field.get(head);
        assertTrue(cubes.stream().anyMatch(cube -> cube.minY < -11), "The second robot keeps its antenna");
        assertTrue(cubes.stream().anyMatch(cube -> cube.minX == -5 && cube.maxX == -2 && cube.minZ == -6));
        assertTrue(cubes.stream().anyMatch(cube -> cube.minX == 2 && cube.maxX == 5 && cube.minZ == -6));
    }

    @Test
    void idleResetsTheWholeRigAndLoopsWithoutAccumulatingOffsets() throws Exception {
        var model = model();
        ModelPart rig = model.root().getChild("rig_root");
        ModelPart arm = rig.getChild("body").getChild("arm_left");
        pose(model, 5, 0, 0);
        float y = rig.y, armAngle = arm.zRot;
        assertNotEquals(24, y);
        assertNotEquals(0, armAngle);
        pose(model, 5, 0, 0);
        assertEquals(y, rig.y);
        assertEquals(armAngle, arm.zRot);
        pose(model, 25, 0, 0);
        assertEquals(y, rig.y);
        assertEquals(armAngle, arm.zRot);
        arm.x = 999;
        pose(model, 5, 0, 0);
        assertEquals(4, arm.x, 1e-6);
    }

    @Test
    void gazeStaysLimitedAndResetsWithoutDisturbingTheIdleRig() throws Exception {
        var model = model();
        var rig = model.root().getChild("rig_root");
        var head = rig.getChild("body").getChild("head");
        pose(model, 5, 30, -25);
        assertEquals(Math.toRadians(30), head.yRot, 1e-5);
        assertEquals(Math.toRadians(-25), head.xRot, 1e-5);
        float y = rig.y;
        pose(model, 5, 120, -120);
        assertEquals(Math.toRadians(60), head.yRot, 1e-5);
        assertEquals(Math.toRadians(-75), head.xRot, 1e-5);
        assertEquals(y, rig.y);
        pose(model, 5, 0, 0);
        assertEquals(0, head.xRot, 1e-6);
        assertEquals(0, head.yRot, 1e-6);
        assertEquals(y, rig.y);
    }

    private static RoboBaby2Model model() {
        return new RoboBaby2Model(RoboBaby2Model.createBodyLayer().bakeRoot());
    }

    private static void pose(RoboBaby2Model model, float age, float yaw, float pitch) throws Exception {
        Method method = RoboBaby2Model.class.getDeclaredMethod("setupPose", float.class, float.class, float.class);
        method.setAccessible(true);
        method.invoke(model, age, yaw, pitch);
    }
}
