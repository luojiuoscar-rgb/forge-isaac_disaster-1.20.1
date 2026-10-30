package net.luojiuoscar.isaac_disaster.client.model.familiar.loki_baby;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LokiBabyModelTest {
    @Test
    void hornsBelongToTheHeadAndEveryAnimationBoneExists() throws Exception {
        var model = model();
        ModelPart body = model.root().getChild("rig_root").getChild("body");
        assertEquals(3, cubeCount(body));
        assertEquals(9, cubeCount(body.getChild("head")));
        for (var animation : new AnimationDefinition[]{LokiBabyAnimations.IDLE, LokiBabyAnimations.ATTACK})
            for (String bone : animation.boneAnimations().keySet())
                assertTrue(model.getAnyDescendantWithName(bone).isPresent(), bone);
        pose(model, 5, -1);
        assertEquals(0, body.getChild("head").yRot, 1e-6);
        assertEquals(0, body.getChild("head").xRot, 1e-6);
    }

    @Test
    void idleLoopsWithoutAccumulatingOffsetsAndUsesItsOwnWingAnimation() throws Exception {
        var model = model();
        ModelPart body = model.root().getChild("rig_root").getChild("body");
        pose(model, 5, -1);
        float idleY = body.y;
        float wing = body.getChild("wing_left_inner_bone").zRot;
        assertNotEquals(-6, idleY);
        assertNotEquals(0, wing);
        pose(model, 5, -1);
        assertEquals(idleY, body.y);
        pose(model, 25, -1);
        assertEquals(idleY, body.y);
        assertEquals(wing, body.getChild("wing_left_inner_bone").zRot);
    }

    @Test
    void attackFollowsTheFullSpinAndDoesNotUnwindDuringTheLastTransition() throws Exception {
        var model = model();
        ModelPart rig = model.root().getChild("rig_root");
        pose(model, 1, 1);
        assertEquals(Math.toRadians(20), rig.yRot, 1e-5, "The native anticipation is retained");
        float previous = rig.yRot;
        for (float tick = 2; tick < 12; tick += 0.25F) {
            pose(model, tick, tick);
            assertTrue(rig.yRot <= previous + 1e-5, "The full turn must not reverse while fading to idle at " + tick);
            previous = rig.yRot;
        }
        assertEquals(-2 * Math.PI, previous, 1e-5);
        pose(model, 11.9F, 11.9F);
        assertEquals(-2 * Math.PI, rig.yRot, 1e-5);
        pose(model, 12, -1);
        assertEquals(0, rig.yRot, 1e-6);
        assertEquals(1, Math.cos(previous - rig.yRot), 1e-6, "A complete turn has the same orientation as idle");
    }

    @Test
    void headStaysLevelDuringTheSpinAndReturnsToNeutralAfterEveryPoseReset() throws Exception {
        var model = model();
        ModelPart rig = model.root().getChild("rig_root");
        ModelPart head = rig.getChild("body").getChild("head");
        for (float attackAge : new float[]{-1, 0, 4, 8, 11.9F, 12}) {
            head.xRot = 0.7F;
            head.yRot = -0.9F;
            pose(model, 4, attackAge);
            assertEquals(0, head.xRot, 1e-6, "Head must remain level through idle and the full spin");
            assertEquals(0, head.yRot, 1e-6, "Head must face with the body without independent yaw");
            if (attackAge == 4) assertTrue(Math.abs(rig.yRot) > 0.1, "The body spin remains animated");
        }
    }

    private static LokiBabyModel model() {
        return new LokiBabyModel(LokiBabyModel.createBodyLayer().bakeRoot());
    }

    private static int cubeCount(ModelPart part) throws Exception {
        Field field = ModelPart.class.getDeclaredField("cubes");
        field.setAccessible(true);
        return ((List<?>) field.get(part)).size();
    }

    private static void pose(LokiBabyModel model, float age, float attackAge) throws Exception {
        Method method = LokiBabyModel.class.getDeclaredMethod("setupPose", float.class, float.class, float.class);
        method.setAccessible(true);
        method.invoke(model, age, attackAge, 12F);
    }
}
