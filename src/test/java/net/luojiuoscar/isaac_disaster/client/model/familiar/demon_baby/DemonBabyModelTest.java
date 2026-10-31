package net.luojiuoscar.isaac_disaster.client.model.familiar.demon_baby;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DemonBabyModelTest {
    @Test
    void gazeComposesWithRecoilAndBothHornsRemainChildrenOfTheHead() throws Exception {
        DemonBabyModel model = model();
        ModelPart body = model.root().getChild("rig_root").getChild("body");
        ModelPart head = body.getChild("head");
        assertNotNull(head.getChild("horn_left_tip_r1"));
        assertNotNull(head.getChild("horn_right_tip_r1"));
        pose(model, 3, 3, 0, 0);
        Quaternionf original = rotation(body).mul(rotation(head));
        Vector3f position = new Vector3f(head.x, head.y, head.z);
        pose(model, 3, 3, 40, -30);
        Quaternionf expected = new Quaternionf().rotationYXZ((float) Math.toRadians(40), (float) Math.toRadians(-30), 0).mul(original);
        assertTrue(Math.abs(expected.dot(rotation(body).mul(rotation(head)))) > 0.9999);
        assertEquals(position, new Vector3f(head.x, head.y, head.z));
    }

    @Test
    void sevenTickActionReturnsToIdleAndNeverAccumulatesItsPose() throws Exception {
        var model = model();
        var body = model.root().getChild("rig_root").getChild("body");
        pose(model, 3, 3, 0, 0);
        float pitch = body.xRot;
        assertTrue(Math.abs(pitch) > 0.01);
        pose(model, 3, 3, 0, 0);
        assertEquals(pitch, body.xRot, 1e-6);
        pose(model, 7, 7, 0, 0);
        assertEquals(0, body.xRot, 1e-6);
        pose(model, 7, -1, 0, 0);
        assertEquals(0, body.xRot, 1e-6);
        for (String bone : DemonBabyAnimations.ATTACK.boneAnimations().keySet())
            assertTrue(model.getAnyDescendantWithName(bone).isPresent());
    }

    @Test
    void originalIdleLoopAndSourceActionLengthArePreserved() throws Exception {
        var model = model();
        var rig = model.root().getChild("rig_root");
        pose(model, 5, -1, 0, 0);
        assertEquals(23.25F, rig.y, 1e-4);
        pose(model, 25, -1, 0, 0);
        assertEquals(23.25F, rig.y, 1e-4);
        assertEquals(0.8F, DemonBabyAnimations.ATTACK.lengthInSeconds());
        assertTrue(DemonBabyAnimations.IDLE.looping());
        assertFalse(DemonBabyAnimations.ATTACK.looping());
    }

    private static DemonBabyModel model() {
        return new DemonBabyModel(DemonBabyModel.createBodyLayer().bakeRoot());
    }

    private static void pose(DemonBabyModel model, float age, float attackAge, float yaw, float pitch) throws Exception {
        Method method = DemonBabyModel.class.getDeclaredMethod("setupPose", float.class, float.class, float.class, float.class);
        method.setAccessible(true);
        method.invoke(model, age, attackAge, yaw, pitch);
    }

    private static Quaternionf rotation(ModelPart part) {
        return new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot);
    }
}
