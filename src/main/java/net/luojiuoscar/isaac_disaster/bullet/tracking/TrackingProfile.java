package net.luojiuoscar.isaac_disaster.bullet.tracking;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Shared tracking policy for all lightweight bullets. */
public record TrackingProfile(double range, double steer, double forwardBias, double minHomingSpeed) {
    /** Resolves source-specific tracking strength without changing target geometry. */
    public static TrackingProfile forBullet(BulletState bullet) {
        if (ModAttackTypes.C_SECTION.getId().equals(bullet.getTypeId())) {
            return new TrackingProfile(bullet.homingRange(), Math.max(0.85D, bullet.homingSteer()), 0.5D, 0.25D);
        }
        return new TrackingProfile(bullet.homingRange(), bullet.homingSteer(), 0.5D, 0.0D);
    }

    /** Returns the forward-biased center used only for candidate selection. */
    public Vec3 searchCenter(BulletState bullet) {
        Vec3 velocity = bullet.velocity();
        double offset = Math.min(range * forwardBias, 3.0D);
        return velocity.lengthSqr() < 1.0E-8D ? bullet.position() : bullet.position().add(velocity.normalize().scale(offset));
    }

    /** Returns the same target-center geometry used by server and legacy entity paths. */
    public Vec3 targetCenter(LivingEntity target) {
        return target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
    }

    /** Computes a homing velocity with distance braking and radial convergence. */
    public Vec3 limitedHomingVelocity(BulletState bullet, Vec3 target) {
        Vec3 toTarget = target.subtract(bullet.position());
        if (toTarget.lengthSqr() < 1.0E-8D || bullet.velocity().lengthSqr() < 1.0E-8D) return bullet.velocity();
        double distance = toTarget.length();
        double speed = bullet.baseSpeed() * Math.max(0.2D, Math.min(1.0D, distance / Math.max(range, 1.0E-6D)));
        Vec3 targetDirection = toTarget.normalize();
        Vec3 currentDirection = bullet.velocity().normalize();
        double radial = bullet.velocity().dot(targetDirection);
        double strength = steer * Math.min(1.0D, distance);
        if (radial <= 0.0D || distance < bullet.baseSpeed() * 1.5D) strength = 1.0D;
        Vec3 direction = currentDirection.add(targetDirection.subtract(currentDirection).scale(Math.min(1.0D, strength)));
        return direction.normalize().scale(speed);
    }

    /** Computes the desired velocity for either a homing target or an owner control point. */
    public Vec3 desiredVelocity(BulletState bullet, Vec3 target, boolean control) {
        if (target == null) return cruiseVelocity(bullet);
        if (control) return controlVelocity(bullet, target);
        if (bullet.steeringMode() == BulletSteeringMode.DIRECT) {
            Vec3 toTarget = target.subtract(bullet.position());
            if (toTarget.lengthSqr() < 1.0E-8D) return bullet.velocity();
            double distance = toTarget.length();
            double speed = Math.max(minHomingSpeed,
                    Math.min(bullet.baseSpeed(), bullet.baseSpeed() * distance / Math.max(range, 1.0E-6D)));
            return toTarget.normalize().scale(speed);
        }
        return limitedHomingVelocity(bullet, target);
    }

    /** Computes controllable steering while preserving the current speed magnitude. */
    public Vec3 controlVelocity(BulletState bullet, Vec3 target) {
        Vec3 toTarget = target.subtract(bullet.position());
        if (toTarget.lengthSqr() < 1.0E-8D || bullet.velocity().lengthSqr() < 1.0E-8D) return bullet.velocity();
        Vec3 current = bullet.velocity().normalize();
        double adaptiveStrength = bullet.controlSteer() * Math.min(1.0D, toTarget.length());
        Vec3 direction = current.add(toTarget.normalize().subtract(current).scale(adaptiveStrength));
        return direction.normalize().scale(bullet.velocity().length());
    }

    /** Restores the creation-time cruise velocity along the current heading. */
    public Vec3 cruiseVelocity(BulletState bullet) {
        if (bullet.velocity().lengthSqr() < 1.0E-8D) return Vec3.ZERO;
        return bullet.velocity().normalize().scale(bullet.baseSpeed());
    }

    /** Restores the legacy hostility ordering before distance is considered. */
    public static int priority(ServerLevel level, LivingEntity owner, LivingEntity entity) {
        if (entity instanceof Enemy) {
            boolean aggro = false;
            if (entity instanceof Monster monster) {
                aggro = monster.getTarget() == owner;
                if (monster instanceof NeutralMob neutral && owner instanceof Player player) aggro |= neutral.isAngryAt(player);
            }
            return aggro ? 0 : 1;
        }
        if (entity instanceof Mob) return 2;
        if (entity instanceof Player player && owner instanceof Player ownerPlayer
                && level.getServer().isPvpAllowed() && ownerPlayer.canHarmPlayer(player)
                && !ownerPlayer.isAlliedTo(player)) return 3;
        return Integer.MAX_VALUE;
    }

    /** Returns whether a candidate is eligible for this owner. */
    public boolean eligible(ServerLevel level, LivingEntity owner, LivingEntity entity) {
        return entity.isAlive() && entity != owner && !entity.isInvulnerable()
                && !EntityHelper.isFriendly(entity, owner) && priority(level, owner, entity) != Integer.MAX_VALUE;
    }
}
