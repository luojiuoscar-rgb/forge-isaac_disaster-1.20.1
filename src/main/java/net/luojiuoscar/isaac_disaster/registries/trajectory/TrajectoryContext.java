package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.world.phys.Vec3;

/** Immutable input snapshot for one pure trajectory calculation. */
public final class TrajectoryContext {
    public final IBulletObject bulletObject;
    public final Input input;

    /** Current shooter anchor at 60% of body height; never a frozen launch position. */
    public final Vec3 trajectoryPos;

    public final int amplifier;

    /** State owned by the module currently being evaluated. */
    public final TrajectoryState runtimeState;

    public final TrajectoryFrame frame;

    /** True when another trajectory has already produced this tick's intent. */
    public final boolean composed;

    public TrajectoryContext(
            IBulletObject bulletObject,
            Input input,
            Vec3 trajectoryPos,
            int amplifier,
            TrajectoryState runtimeState) {
        this(
                bulletObject,
                input,
                trajectoryPos,
                amplifier,
                runtimeState,
                bulletObject == null
                        ? null
                        : new TrajectoryFrame(
                                bulletObject,
                                bulletObject.getTrajectoryRuntime().origin(),
                                bulletObject.getTrajectoryRuntime().launchDirection()),
                false);
    }

    public TrajectoryContext(
            IBulletObject bulletObject,
            Input input,
            Vec3 trajectoryPos,
            int amplifier,
            TrajectoryState runtimeState,
            TrajectoryFrame frame) {
        this(
                bulletObject,
                input,
                trajectoryPos,
                amplifier,
                runtimeState,
                frame,
                false);
    }

    public TrajectoryContext(
            IBulletObject bulletObject,
            Input input,
            Vec3 trajectoryPos,
            int amplifier,
            TrajectoryState runtimeState,
            TrajectoryFrame frame,
            boolean composed) {
        this.bulletObject = bulletObject;
        this.input = input;
        this.trajectoryPos = trajectoryPos == null ? Vec3.ZERO : trajectoryPos;
        this.amplifier = amplifier;
        this.runtimeState = runtimeState;
        this.frame = frame;
        this.composed = composed;
    }

    /** Immutable kinematic input for one trajectory evaluation step. */
    public record Input(
            Vec3 position,
            Vec3 velocity,
            Vec3 baseVelocity,
            Vec3 acceleration,
            double deltaTicks) {}
}
