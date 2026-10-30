package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Radial flight and blade geometry shared by authoritative collision and client presentation. */
public final class PlayerMomsKnifeMotion {
    public static final Vec3 FORWARD = new Vec3(0, 0, 1);
    public static final double ENTITY_HIT_RADIUS = 0.25;
    public static final double ENTITY_HIT_PADDING = ENTITY_HIT_RADIUS - MomKnifePose.BLADE_RADIUS;
    private PlayerMomsKnifeMotion() {}

    public static double range(double playerRange, double chargeFraction) {
        if (!Double.isFinite(playerRange) || !Double.isFinite(chargeFraction)) return 0;
        return Math.min(Math.max(0, playerRange) / 2, 8) * Mth.clamp(chargeFraction, 0, 1);
    }

    public static double duration(double range, double speed) {
        return range > 0 && Double.isFinite(range) && speed > 0 && Double.isFinite(speed)
                ? 4 * range / speed : 0;
    }

    public static double oneWayStep(double remaining, double speed) {
        return Double.isFinite(remaining) && Double.isFinite(speed)
                ? Math.max(0, Math.min(remaining, speed)) : 0;
    }

    /** Uses ordinary control geometry and turning budgets, then restores the knife's speed. */
    public static Vec3 controlVelocity(Vec3 position, Vec3 velocity, Vec3 target, boolean tractorBeam) {
        if (!Double.isFinite(target.lengthSqr())) return velocity;
        double speed = velocity.length();
        if (speed < 1.0E-8) return velocity;
        Vec3 desired = TrackingProfile.controlVelocity(position, velocity, target, 0.8);
        if (desired.lengthSqr() < 1.0E-12) return velocity;
        Vec3 change = desired.subtract(velocity);
        double budget = 0.25 * (tractorBeam ? 5 : 1);
        if (change.lengthSqr() > budget * budget) change = change.normalize().scale(budget);
        Vec3 result = velocity.add(change);
        return (result.lengthSqr() < 1.0E-12 ? unit(desired) : result.normalize()).scale(speed);
    }

    /** Direction-only tracking never changes radial timing or one-way speed. */
    public static Vec3 steer(Vec3 current, Vec3 desired) {
        if (!Double.isFinite(desired.lengthSqr()) || desired.lengthSqr() < 1.0E-12) return unit(current);
        return unit(unit(current).scale(0.4).add(desired.normalize().scale(0.6)));
    }

    public static Vec3 unit(Vec3 direction) {
        return Double.isFinite(direction.lengthSqr()) && direction.lengthSqr() > 1.0E-12
                ? direction.scale(1 / Math.sqrt(direction.lengthSqr())) : FORWARD;
    }

    /** Uses a yaw-defined right axis so looking straight up or down does not flip the local frame. */
    public static Vec3 toWorld(Vec3 local, float yaw, float pitch) {
        return unit(offsetToWorld(local, yaw, pitch));
    }

    /** Transforms a displacement without normalizing away its magnitude or a zero offset. */
    public static Vec3 offsetToWorld(Vec3 local, float yaw, float pitch) {
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw).normalize();
        Vec3 right = new Vec3(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw)));
        right = right.subtract(forward.scale(right.dot(forward))).normalize();
        Vec3 up = forward.cross(right).normalize();
        return right.scale(local.x).add(up.scale(local.y)).add(forward.scale(local.z));
    }

    public static Vec3 toLocal(Vec3 world, float yaw, float pitch) {
        return unit(offsetToLocal(world, yaw, pitch));
    }

    public static Vec3 offsetToLocal(Vec3 world, float yaw, float pitch) {
        Vec3 forward = Vec3.directionFromRotation(pitch, yaw).normalize();
        Vec3 right = new Vec3(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw)));
        right = right.subtract(forward.scale(right.dot(forward))).normalize();
        Vec3 up = forward.cross(right).normalize();
        return new Vec3(world.dot(right), world.dot(up), world.dot(forward));
    }

    /** Shortest-arc interpolation remains finite even for exactly opposite directions. */
    public static Vec3 interpolateDirection(Vec3 from, Vec3 to, double fraction) {
        from = unit(from);
        to = unit(to);
        fraction = Mth.clamp(fraction, 0, 1);
        double dot = Mth.clamp(from.dot(to), -1, 1);
        if (dot > 0.9995) return unit(from.lerp(to, fraction));
        double angle = Math.acos(dot);
        Vec3 tangent = to.subtract(from.scale(dot));
        if (tangent.lengthSqr() < 1.0E-12) {
            tangent = from.cross(Math.abs(from.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0));
        }
        return unit(from.scale(Math.cos(angle * fraction)).add(tangent.normalize().scale(Math.sin(angle * fraction))));
    }

    /** Constant inward acceleration starts at launch and continues through the turnaround. */
    public static double distance(double range, double speed, double age) {
        double duration = duration(range, speed);
        if (duration <= 0 || !Double.isFinite(age)) return 0;
        double fraction = Mth.clamp(age / duration, 0, 1);
        return 4 * range * fraction * (1 - fraction);
    }

    public static Vec3 idlePosition(Player owner, float partialTick) {
        Vec3 forward = owner.level().isClientSide ? owner.getViewVector(partialTick) : owner.getLookAngle();
        return center(owner, partialTick).add(forward.scale(Math.max(0.6, owner.getBbWidth())));
    }

    public static Vec3 center(Player owner, float partialTick) {
        return owner.getEyePosition(partialTick).add(0, -0.15 * owner.getBbHeight(), 0);
    }

    public static Vec3 barragePosition(Player owner) {
        return owner.getEyePosition().subtract(unit(owner.getLookAngle()));
    }

    public static Vec3 scatterOrigin(Player owner) {
        return scatterOrigin(barragePosition(owner), owner.getYRot(), owner.getXRot(),
                owner.getRandom().nextDouble() * 2 - 1, owner.getRandom().nextDouble() * 2 - 1);
    }

    /** A square in the view plane, independent of player size and stable at vertical pitch. */
    public static Vec3 scatterOrigin(Vec3 center, float yaw, float pitch, double offsetX, double offsetY) {
        double x = Double.isFinite(offsetX) ? Mth.clamp(offsetX, -1, 1) : 0;
        double y = Double.isFinite(offsetY) ? Mth.clamp(offsetY, -1, 1) : 0;
        return center.add(toWorld(new Vec3(1, 0, 0), yaw, pitch).scale(x))
                .add(toWorld(new Vec3(0, 1, 0), yaw, pitch).scale(y));
    }

    public static MomKnifePose pose(float yaw, float pitch) {
        return new MomKnifePose(yaw + 180, pitch - 90, 0);
    }

    public static MomKnifePose pose(Vec3 direction, float fallbackYaw) {
        direction = unit(direction);
        float yaw = direction.horizontalDistanceSqr() < 1.0E-10 ? fallbackYaw
                : (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.asin(Mth.clamp(-direction.y, -1, 1)));
        return pose(yaw, pitch);
    }

    public static Blade blade(Vec3 anchor, Vec3 direction) {
        return blade(anchor, direction, 1);
    }

    public static Blade blade(Vec3 anchor, Vec3 direction, double scale) {
        direction = unit(direction);
        scale = Double.isFinite(scale) ? Math.max(0, scale) : 0;
        return new Blade(anchor, anchor.add(direction.scale(MomKnifePose.BLADE_BASE_DISTANCE * scale)),
                anchor.add(direction.scale(MomKnifePose.BLADE_TIP_DISTANCE * scale)), MomKnifePose.BLADE_RADIUS * scale);
    }

    public static Blade blade(Vec3 idle, float yaw, float pitch, double distance) {
        MomKnifePose pose = pose(yaw, pitch);
        Vec3 anchor = idle.add(pose.bladePoint(Vec3.ZERO, 1).scale(distance));
        return new Blade(anchor, pose.bladePoint(anchor, MomKnifePose.BLADE_BASE_DISTANCE),
                pose.bladePoint(anchor, MomKnifePose.BLADE_TIP_DISTANCE));
    }

    /** Samples along the blade as it sweeps, rather than testing only its tip or two diagonals. */
    public static boolean contacts(Blade from, Blade to, AABB target) {
        return contacts(from, to, target, 0);
    }

    public static boolean contacts(Blade from, Blade to, AABB target, double padding) {
        padding = Double.isFinite(padding) ? Math.max(0, padding) : 0;
        double radius = Math.max(from.radius(), to.radius()) + padding;
        AABB expanded = target.inflate(radius);
        if (SweptCollision.segmentAabb(from.base(), from.tip(), expanded).hit()
                || SweptCollision.segmentAabb(to.base(), to.tip(), expanded).hit()) return true;
        int samples = Math.max(1, (int) Math.ceil(Math.max(from.base().distanceTo(from.tip()), to.base().distanceTo(to.tip()))
                / Math.max(radius, 1.0E-8)));
        for (int i = 0; i <= samples; i++) {
            double fraction = (double) i / samples;
            if (SweptCollision.segmentAabb(from.base().lerp(from.tip(), fraction),
                    to.base().lerp(to.tip(), fraction), expanded).hit()) return true;
        }
        return false;
    }

    public record Blade(Vec3 anchor, Vec3 base, Vec3 tip, double radius) {
        public Blade(Vec3 anchor, Vec3 base, Vec3 tip) {
            this(anchor, base, tip, MomKnifePose.BLADE_RADIUS);
        }

        public AABB bounds() {
            return new AABB(base, tip).inflate(radius);
        }
    }
}
