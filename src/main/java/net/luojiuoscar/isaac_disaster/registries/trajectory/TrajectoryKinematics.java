package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Primary motion and offset frame, owned directly by TrajectoryRuntime. */
public final class TrajectoryKinematics {
    private boolean initialized;
    private Vec3 position = Vec3.ZERO;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 acceleration = Vec3.ZERO;
    private Vec3 offset = Vec3.ZERO;
    private Vec3 forward = new Vec3(1, 0, 0);
    private Vec3 right = new Vec3(0, 0, 1);
    private Vec3 up = new Vec3(0, 1, 0);
    private Vec3 launchForward = new Vec3(1, 0, 0);
    private Vec3 launchRight = new Vec3(0, 0, 1);
    private Vec3 launchUp = new Vec3(0, 1, 0);
    private double distance;
    private double clock;
    private double recovery = 2;

    public boolean initialized() {
        return initialized;
    }

    public void initialized(boolean value) {
        initialized = value;
    }

    public Vec3 position() {
        return position;
    }

    public void position(Vec3 value) {
        position = vectorOrZero(value);
    }

    public Vec3 velocity() {
        return velocity;
    }

    public void velocity(Vec3 value) {
        velocity = vectorOrZero(value);
    }

    public Vec3 acceleration() {
        return acceleration;
    }

    public void acceleration(Vec3 value) {
        acceleration = vectorOrZero(value);
    }

    public Vec3 offset() {
        return offset;
    }

    public void offset(Vec3 value) {
        offset = vectorOrZero(value);
    }

    public Vec3 forward() {
        return forward;
    }

    public Vec3 right() {
        return right;
    }

    public Vec3 up() {
        return up;
    }

    public Vec3 launchForward() {
        return launchForward;
    }

    public Vec3 launchRight() {
        return launchRight;
    }

    public Vec3 launchUp() {
        return launchUp;
    }

    public double distance() {
        return distance;
    }

    public void distance(double value) {
        distance = Double.isFinite(value) ? Math.max(0, value) : 0.0D;
    }

    public void advanceDistance(double value) {
        if (Double.isFinite(value) && value > 0.0D) distance += value;
    }

    public double clock() {
        return clock;
    }

    public void advanceClock(double value) {
        if (Double.isFinite(value)) clock += value;
    }

    public double recovery() {
        return recovery;
    }

    public void recovery(double value) {
        recovery = Double.isFinite(value) ? Math.max(0, value) : 0.0D;
    }

    public void orient(Vec3 direction) {
        if (direction.lengthSqr() < 1e-12) return;
        forward = direction.normalize();
        right = launchRight;
        up = launchUp;
    }

    public void initializeLaunchBasis(Vec3 direction) {
        if (direction == null || direction.lengthSqr() < 1e-12) return;
        Vec3 axis = direction.normalize();
        Vec3 side = axis.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1e-8) side = axis.cross(new Vec3(0, 0, 1));
        if (side.lengthSqr() < 1e-8) side = new Vec3(1, 0, 0);
        side = side.normalize();
        Vec3 vertical = side.cross(axis).normalize();
        launchForward = axis;
        launchRight = side;
        launchUp = vertical;
        right = side;
        up = vertical;
    }

    public TrajectoryKinematics copy() {
        TrajectoryKinematics c = new TrajectoryKinematics();
        c.initialized = initialized;
        c.position = position;
        c.velocity = velocity;
        c.acceleration = acceleration;
        c.offset = offset;
        c.forward = forward;
        c.right = right;
        c.up = up;
        c.launchForward = launchForward;
        c.launchRight = launchRight;
        c.launchUp = launchUp;
        c.distance = distance;
        c.clock = clock;
        c.recovery = recovery;
        return c;
    }

    private static Vec3 vectorOrZero(Vec3 value) {
        return value == null ? Vec3.ZERO : value;
    }

    public void write(FriendlyByteBuf b) {
        b.writeBoolean(initialized);
        writeVec(b, position);
        writeVec(b, velocity);
        writeVec(b, acceleration);
        writeVec(b, offset);
        writeVec(b, forward);
        writeVec(b, right);
        writeVec(b, up);
        writeVec(b, launchForward);
        writeVec(b, launchRight);
        writeVec(b, launchUp);
        b.writeDouble(distance);
        b.writeDouble(clock);
        b.writeDouble(recovery);
    }

    public static TrajectoryKinematics read(FriendlyByteBuf b) {
        TrajectoryKinematics c = new TrajectoryKinematics();
        c.initialized = b.readBoolean();
        c.position = readVec(b);
        c.velocity = readVec(b);
        c.acceleration = readVec(b);
        c.offset = readVec(b);
        c.forward = readVec(b);
        c.right = readVec(b);
        c.up = readVec(b);
        c.launchForward = readVec(b);
        c.launchRight = readVec(b);
        c.launchUp = readVec(b);
        c.distance = b.readDouble();
        c.clock = b.readDouble();
        c.recovery = b.readDouble();
        return c;
    }

    private static Vec3 readVec(FriendlyByteBuf b) {
        return new Vec3(b.readDouble(), b.readDouble(), b.readDouble());
    }

    private static void writeVec(FriendlyByteBuf b, Vec3 v) {
        b.writeDouble(v.x);
        b.writeDouble(v.y);
        b.writeDouble(v.z);
    }
}
