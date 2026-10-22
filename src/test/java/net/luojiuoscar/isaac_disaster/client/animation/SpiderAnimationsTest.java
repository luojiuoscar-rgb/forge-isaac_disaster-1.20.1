package net.luojiuoscar.isaac_disaster.client.animation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.luojiuoscar.isaac_disaster.client.model.creature.SpiderModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SpiderAnimationsTest {
    @Test
    void everyAuthoredKeyframeMatchesTheApprovedSource() throws Exception {
        JsonObject source;
        try (var reader = Files.newBufferedReader(Path.of("blockbench/creature/blue_spider/blue_spider.bbmodel"))) {
            source = JsonParser.parseReader(reader).getAsJsonObject();
        }
        int count = 0;
        for (var animation : source.getAsJsonArray("animations")) {
            var definition = animation.getAsJsonObject();
            String name = definition.get("name").getAsString();
            var clip = SpiderAnimations.Clip.valueOf(name.substring(name.lastIndexOf('.') + 1).toUpperCase());
            for (var entry : definition.getAsJsonObject("animators").entrySet()) {
                var animator = entry.getValue().getAsJsonObject();
                if (!animator.has("keyframes")) continue;
                String bone = animator.get("name").getAsString();
                for (var frameValue : animator.getAsJsonArray("keyframes")) {
                    var frame = frameValue.getAsJsonObject();
                    var channel = SpiderAnimations.Channel.valueOf(frame.get("channel").getAsString().toUpperCase());
                    float time = frame.get("time").getAsFloat();
                    var expected = frame.getAsJsonArray("data_points").get(0).getAsJsonObject();
                    var actual = SpiderAnimations.sample(clip, bone, channel, time);
                    assertNotNull(actual, clip + " " + bone + " " + channel);
                    for (int axis = 0; axis < 3; axis++) {
                        float value = expected.get(new String[]{"x", "y", "z"}[axis]).getAsFloat();
                        assertEquals(value, actual.get(axis), 0.00003F,
                                clip + " " + bone + " " + channel + " t=" + time + " axis=" + axis);
                    }
                    count++;
                }
            }
        }
        assertTrue(count > 400, "Exercise all authored tracks rather than a few example poses");
    }

    @Test
    void fractionalFramesInterpolateAndLoopsRunInEitherDirection() {
        var clip = SpiderAnimations.Clip.MOVE_FORWARD;
        var channel = SpiderAnimations.Channel.ROTATION;
        var a = SpiderAnimations.sample(clip, "leg_left_1", channel, 0.02F);
        var b = SpiderAnimations.sample(clip, "leg_left_1", channel, 0.03F);
        var mid = SpiderAnimations.sample(clip, "leg_left_1", channel, 0.025F);
        for (int i = 0; i < 3; i++) assertEquals((a.get(i) + b.get(i)) / 2, mid.get(i), 0.00002F);
        assertEquals(SpiderAnimations.sample(clip, "leg_left_1", channel, 0.39F).x,
                SpiderAnimations.sample(clip, "leg_left_1", channel, -0.01F).x, 0.00003F);
        assertEquals(a.x, SpiderAnimations.sample(clip, "leg_left_1", channel, 0.42F).x, 0.00003F);
        assertEquals(0.08F, SpiderAnimations.sample(SpiderAnimations.Clip.IDLE,
                "body", SpiderAnimations.Channel.POSITION, 0.8F).y, 0.000001F);
    }

    @Test
    void modelReflectsSourceAxesAndResetsEveryPose() {
        var model = new SpiderModel<Entity>(SpiderModel.createBodyLayer().bakeRoot());
        model.applyAnimation(SpiderAnimations.Clip.MOVE_FORWARD, 0.04F);
        ModelPart root = model.root().getChild("spider_root");
        ModelPart leg = root.getChild("leg_left_1");
        var source = SpiderAnimations.sample(SpiderAnimations.Clip.MOVE_FORWARD, "leg_left_1",
                SpiderAnimations.Channel.ROTATION, 0.04F);
        assertEquals(-Math.toRadians(source.x), leg.xRot, 0.000001);
        assertEquals(Math.toRadians(source.y), leg.yRot, 0.000001);
        assertEquals(-Math.toRadians(source.z), leg.zRot, 0.000001);
        assertEquals(22.9F - SpiderAnimations.sample(SpiderAnimations.Clip.MOVE_FORWARD,
                "spider_root", SpiderAnimations.Channel.POSITION, 0.04F).y, root.y, 0.000001F);
        model.applyAnimation(SpiderAnimations.Clip.IDLE, 0.8F);
        assertEquals(0, leg.xRot);
        assertEquals(0, root.xRot);
        assertEquals(22.9F, root.y);
        assertEquals(-0.08F, root.getChild("body").y, 0.000001F);
        model.setupAnim(null, 0, 0, 0, 0, 0);
        assertEquals(0, root.getChild("body").y);
    }

    @Test
    void alternatingClipsAndModelsMatchSamplesWithoutLeakingEarlierPoses() {
        var first = new SpiderModel<Entity>(SpiderModel.createBodyLayer().bakeRoot());
        var second = new SpiderModel<Entity>(SpiderModel.createBodyLayer().bakeRoot());
        float[] times = {0, 0.025F, 0.19F, 0.399F, 0.6F, 1.59F, -0.03F, Float.NaN};
        for (var clip : SpiderAnimations.Clip.values()) {
            for (float time : times) {
                assertPoseMatchesSamples(first, clip, time);
                assertPoseMatchesSamples(second, SpiderAnimations.Clip.IDLE, 0.8F);
                assertPoseMatchesSamples(first, SpiderAnimations.Clip.IDLE, 0);
                assertPoseMatchesSamples(second, clip, time);
            }
        }
    }

    private static void assertPoseMatchesSamples(SpiderModel<Entity> model, SpiderAnimations.Clip clip, float time) {
        model.applyAnimation(clip, time);
        ModelPart root = model.root().getChild("spider_root");
        var parts = new java.util.LinkedHashMap<String, ModelPart>();
        parts.put("spider_root", root);
        parts.put("body", root.getChild("body"));
        for (String name : new String[]{"leg_left_1", "leg_left_4", "leg_right_1", "leg_right_4"}) {
            parts.put(name, root.getChild(name));
            parts.put(name + "_knee", root.getChild(name).getChild(name + "_knee"));
        }
        parts.forEach((name, part) -> {
            var initial = part.getInitialPose();
            var position = SpiderAnimations.sample(clip, name, SpiderAnimations.Channel.POSITION, time);
            var rotation = SpiderAnimations.sample(clip, name, SpiderAnimations.Channel.ROTATION, time);
            String context = clip + " " + name + " t=" + time;
            assertEquals(initial.x + position.x, part.x, 1e-6F, context);
            assertEquals(initial.y - position.y, part.y, 1e-6F, context);
            assertEquals(initial.z + position.z, part.z, 1e-6F, context);
            assertEquals(initial.xRot - (float)Math.toRadians(rotation.x), part.xRot, 1e-6F, context);
            assertEquals(initial.yRot + (float)Math.toRadians(rotation.y), part.yRot, 1e-6F, context);
            assertEquals(initial.zRot - (float)Math.toRadians(rotation.z), part.zRot, 1e-6F, context);
        });
    }
}
