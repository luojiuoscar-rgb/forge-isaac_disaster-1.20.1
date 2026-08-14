package net.luojiuoscar.isaac_disaster.client.item_related;

import net.luojiuoscar.isaac_disaster.system.freeze.EntityFreezeRules;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Captures a frozen mob's first visible pose and restores it while that mob is rendered.
 */
public final class EntityRenderFreeze {
    private static final Map<LivingEntity, FrozenPose> FROZEN_POSES = new WeakHashMap<>();
    private static final ThreadLocal<Deque<RenderContext>> RENDER_CONTEXTS =
            ThreadLocal.withInitial(ArrayDeque::new);

    private EntityRenderFreeze() {
    }

    public static void begin(LivingEntity entity) {
        Deque<RenderContext> contexts = RENDER_CONTEXTS.get();
        if (!(entity instanceof Mob) || !EntityFreezeRules.shouldFreeze(entity)) {
            FROZEN_POSES.remove(entity);
            contexts.push(RenderContext.INACTIVE);
            return;
        }

        contexts.push(new RenderContext(FROZEN_POSES.computeIfAbsent(entity, ignored -> new FrozenPose())));
    }

    public static void end() {
        Deque<RenderContext> contexts = RENDER_CONTEXTS.get();
        if (contexts.isEmpty()) {
            return;
        }

        contexts.pop();
        if (contexts.isEmpty()) {
            RENDER_CONTEXTS.remove();
        }
    }

    public static void freezeRotations(LivingEntity entity, PoseStack poseStack,
                                       Runnable vanillaRotations) {
        FrozenPose pose = activePose();
        if (pose == null || entity.deathTime > 0) {
            vanillaRotations.run();
            return;
        }
        if (pose.rotationDelta == null) {
            Matrix4f beforePose = new Matrix4f(poseStack.last().pose());
            Matrix3f beforeNormal = new Matrix3f(poseStack.last().normal());
            vanillaRotations.run();
            pose.rotationDelta = Transform.capture(beforePose, beforeNormal, poseStack);
        } else {
            pose.rotationDelta.apply(poseStack);
        }
    }

    public static void freezeScale(LivingEntity entity, PoseStack poseStack,
                                    Runnable vanillaScale) {
        FrozenPose pose = activePose();
        if (pose == null || entity.deathTime > 0) {
            vanillaScale.run();
            return;
        }
        if (pose.scaleDelta == null) {
            Matrix4f beforePose = new Matrix4f(poseStack.last().pose());
            Matrix3f beforeNormal = new Matrix3f(poseStack.last().normal());
            vanillaScale.run();
            pose.scaleDelta = Transform.capture(beforePose, beforeNormal, poseStack);
        } else {
            pose.scaleDelta.apply(poseStack);
        }
    }

    public static void freezePart(ModelPart part) {
        FrozenPose pose = activePose();
        if (pose != null) {
            pose.freezePart(part);
        }
    }

    public static void clear() {
        FROZEN_POSES.clear();
        RENDER_CONTEXTS.remove();
    }

    private static FrozenPose activePose() {
        Deque<RenderContext> contexts = RENDER_CONTEXTS.get();
        if (contexts.isEmpty()) {
            return null;
        }
        return contexts.peek().pose;
    }

    private static final class FrozenPartPose {
        private final float x;
        private final float y;
        private final float z;
        private final float xRot;
        private final float yRot;
        private final float zRot;
        private final float xScale;
        private final float yScale;
        private final float zScale;

        private FrozenPartPose(ModelPart part) {
            this.x = part.x;
            this.y = part.y;
            this.z = part.z;
            this.xRot = part.xRot;
            this.yRot = part.yRot;
            this.zRot = part.zRot;
            this.xScale = part.xScale;
            this.yScale = part.yScale;
            this.zScale = part.zScale;
        }

        private static FrozenPartPose capture(ModelPart part) {
            return new FrozenPartPose(part);
        }

        void apply(ModelPart part) {
            part.x = x;
            part.y = y;
            part.z = z;
            part.xRot = xRot;
            part.yRot = yRot;
            part.zRot = zRot;
            part.xScale = xScale;
            part.yScale = yScale;
            part.zScale = zScale;
        }
    }

    private static final class FrozenPose {
        private final Map<ModelPart, FrozenPartPose> parts = new IdentityHashMap<>();
        private Transform rotationDelta;
        private Transform scaleDelta;

        private void freezePart(ModelPart part) {
            parts.computeIfAbsent(part, FrozenPartPose::capture).apply(part);
        }
    }

    private static final class Transform {
        private final Matrix4f pose;
        private final Matrix3f normal;

        private Transform(Matrix4f pose, Matrix3f normal) {
            this.pose = pose;
            this.normal = normal;
        }

        private static Transform capture(Matrix4f beforePose, Matrix3f beforeNormal,
                                          PoseStack stack) {
            Matrix4f poseDelta = beforePose.invert().mul(new Matrix4f(stack.last().pose()));
            Matrix3f normalDelta = beforeNormal.invert().mul(new Matrix3f(stack.last().normal()));
            return new Transform(poseDelta, normalDelta);
        }

        private void apply(PoseStack stack) {
            stack.last().pose().mul(pose);
            stack.last().normal().mul(normal);
        }
    }

    private record RenderContext(FrozenPose pose) {
        private static final RenderContext INACTIVE = new RenderContext(null);
    }
}
