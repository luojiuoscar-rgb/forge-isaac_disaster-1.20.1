package net.luojiuoscar.isaac_disaster.client.model.familiar.tear_baby;

import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TearBabyModelTest {
    @Test
    void independentAimComposesWithAnimatedBodyAndRetainsHeadRecoilAndPosition() {
        TearBabyModel model = new TearBabyModel(TearBabyModel.createBodyLayer().bakeRoot());
        ModelPart body = model.root().getChild("rig_root").getChild("body");
        ModelPart head = body.getChild("head");
        model.setupPose(4, 4, 0, 0);
        Quaternionf original = rotation(body).mul(rotation(head));
        Vector3f position = new Vector3f(head.x, head.y, head.z);
        model.setupPose(4, 4, 40, -30);
        Quaternionf expected = new Quaternionf().rotationYXZ((float) Math.toRadians(40), (float) Math.toRadians(-30), 0).mul(original);
        Quaternionf actual = rotation(body).mul(rotation(head));
        assertTrue(Math.abs(expected.dot(actual)) > 0.9999, "Aim must compose in the neutral body frame: expected "
                + expected + " actual " + actual + " head " + head.xRot + "," + head.yRot + "," + head.zRot);
        assertEquals(position, new Vector3f(head.x, head.y, head.z), "Aim retains animated translation");
    }

    @Test
    void attackCompletesInTenTicksAndTransitionsBackToIdleWithoutAccumulation() {
        TearBabyModel model = new TearBabyModel(TearBabyModel.createBodyLayer().bakeRoot());
        ModelPart body = model.root().getChild("rig_root").getChild("body");
        model.setupPose(5, 5, 0, 0);
        float pitch = body.xRot;
        assertTrue(Math.abs(pitch) > 0.01);
        model.setupPose(5, 5, 0, 0);
        assertEquals(pitch, body.xRot, 0.00001F);
        model.setupPose(10, 10, 0, 0);
        assertEquals(0, body.xRot, 0.00001F);
        model.setupPose(10, -1, 0, 0);
        assertEquals(0, body.xRot, 0.00001F);
        for (String bone : TearBabyAnimations.ATTACK.boneAnimations().keySet())
            assertTrue(model.getAnyDescendantWithName(bone).isPresent());
    }

    private static Quaternionf rotation(ModelPart part) {
        return new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot);
    }

    @Test
    void idleMovesTheExportedRigLoopsAndResetsEveryFrame() {
        TearBabyModel model = new TearBabyModel(TearBabyModel.createBodyLayer().bakeRoot());
        ModelPart rig = model.root().getChild("rig_root");
        model.setupPose(5, -1, 0, 0);
        assertEquals(23.25F, rig.y, 0.0001F);
        model.setupPose(5, -1, 0, 0);
        assertEquals(23.25F, rig.y, 0.0001F, "Repeated rendering must not accumulate bone offsets");
        model.setupPose(25, -1, 0, 0);
        assertEquals(23.25F, rig.y, 0.0001F, "The idle cycle repeats after twenty ticks");
        model.setupPose(0, -1, 0, 0);
        assertEquals(24, rig.y, 0.0001F);
        for (String bone : TearBabyAnimations.IDLE.boneAnimations().keySet()) {
            assertTrue(model.getAnyDescendantWithName(bone).isPresent(), "Missing exported animation bone: " + bone);
        }
    }
}
