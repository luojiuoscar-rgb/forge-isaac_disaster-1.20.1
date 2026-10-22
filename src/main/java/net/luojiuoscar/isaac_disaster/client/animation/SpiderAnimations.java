package net.luojiuoscar.isaac_disaster.client.animation;

import net.minecraft.client.model.geom.ModelPart;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.SpiderMovement;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Objects;

/** Authored Blockbench channels in source coordinates, reflected only when applied to Java parts. */
public final class SpiderAnimations {
    public enum Clip {
        IDLE(1.6F), MOVE_FORWARD(0.4F), MOVE_LEFT(0.4F), MOVE_RIGHT(0.4F);
        final float length;
        Clip(float length) { this.length = length; }
    }
    public enum Channel { POSITION, ROTATION }
    record Track(String bone, Channel channel, boolean catmullRom, float[] frames) { }

    private SpiderAnimations() { }

    public static Clip movementClip(SpiderMovement.Gait movement) {
        return switch (movement) {
            case FORWARD -> Clip.MOVE_FORWARD;
            case LEFT -> Clip.MOVE_LEFT;
            case RIGHT -> Clip.MOVE_RIGHT;
            case IDLE -> Clip.IDLE;
        };
    }

    public static Vector3f sample(Clip clip, String bone, Channel channel, float seconds) {
        for (Track track : SpiderAnimationData.tracks(clip)) {
            if (track.bone.equals(bone) && track.channel == channel) {
                return sample(track, loopTime(clip, seconds), new Vector3f());
            }
        }
        return new Vector3f();
    }

    /** Per-model bindings and scratch state; rendering never rebuilds maps or sampling vectors. */
    public static final class Animator {
        private record BoundTrack(ModelPart part, Track track) { }
        private final BoundTrack[][] tracks = new BoundTrack[Clip.values().length][];
        private final Vector3f pose = new Vector3f();

        public Animator(Map<String, ModelPart> parts) {
            for (Clip clip : Clip.values()) {
                Track[] source = SpiderAnimationData.tracks(clip);
                BoundTrack[] bound = new BoundTrack[source.length];
                for (int i = 0; i < source.length; i++) {
                    Track track = source[i];
                    ModelPart part = Objects.requireNonNull(parts.get(track.bone),
                            "Missing spider animation bone: " + track.bone);
                    bound[i] = new BoundTrack(part, track);
                }
                tracks[clip.ordinal()] = bound;
            }
        }

        public void apply(Clip clip, float seconds) {
            float time = loopTime(clip, seconds);
            for (BoundTrack bound : tracks[clip.ordinal()]) {
                ModelPart part = bound.part;
                Track track = bound.track;
                sample(track, time, pose);
                if (track.channel == Channel.POSITION) {
                    part.x += pose.x;
                    part.y -= pose.y;
                    part.z += pose.z;
                } else {
                    part.xRot -= (float) Math.toRadians(pose.x);
                    part.yRot += (float) Math.toRadians(pose.y);
                    part.zRot -= (float) Math.toRadians(pose.z);
                }
            }
        }
    }

    private static float loopTime(Clip clip, float seconds) {
        float time = Float.isFinite(seconds) ? seconds % clip.length : 0;
        return time < 0 ? time + clip.length : time;
    }

    private static Vector3f sample(Track track, float time, Vector3f result) {
        float[] frames = track.frames;
        int last = frames.length / 4 - 1;
        int left = 0, right = last;
        while (left < right) {
            int middle = (left + right + 1) / 2;
            if (frames[middle * 4] <= time) left = middle;
            else right = middle - 1;
        }
        int next = Math.min(left + 1, last);
        float span = frames[next * 4] - frames[left * 4];
        float alpha = span == 0 ? 0 : (time - frames[left * 4]) / span;
        for (int axis = 1; axis <= 3; axis++) {
            float p1 = frames[left * 4 + axis], p2 = frames[next * 4 + axis];
            float value;
            if (track.catmullRom) {
                float p0 = frames[Math.max(0, left - 1) * 4 + axis];
                float p3 = frames[Math.min(last, next + 1) * 4 + axis];
                value = 0.5F * ((2 * p1) + (p2 - p0) * alpha
                        + (2 * p0 - 5 * p1 + 4 * p2 - p3) * alpha * alpha
                        + (-p0 + 3 * p1 - 3 * p2 + p3) * alpha * alpha * alpha);
            } else value = p1 + (p2 - p1) * alpha;
            result.setComponent(axis - 1, value);
        }
        return result;
    }
}
