package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Shared flight, attack queue and contact lifecycle; variants supply color, damage and contact effects. */
public abstract class AbstractFlyFamiliarEntity extends AbstractIsaacFamiliarEntity {
    public enum FlyState { ORBIT, CHASE, RETURN, ROAM }

    private static final EntityDataAccessor<Byte> FLY_STATE =
            SynchedEntityData.defineId(AbstractFlyFamiliarEntity.class, EntityDataSerializers.BYTE);
    private final FlyRoaming roaming = new FlyRoaming(this, random);
    private FlyOwnerFrame ownerFrame;
    private boolean consumed;
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ;
    private float lerpYaw, lerpPitch;
    private long nextOrbitCheck = Long.MIN_VALUE;

    protected AbstractFlyFamiliarEntity(EntityType<? extends AbstractFlyFamiliarEntity> type, Level level) {
        super(type, level);
    }

    /** Returns the body's RGB tint as {@code 0xRRGGBB}; wings keep their texture color. */
    public abstract int getBodyColor();

    protected abstract float getContactDamage(ServerPlayer owner, LivingEntity target);

    /** Runs once after the contact attempt and after this fly has been consumed and unlinked. */
    protected abstract void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                         float attemptedDamage, boolean damageAccepted);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FLY_STATE, (byte) FlyState.ORBIT.ordinal());
    }

    public final FlyState getFlyState() {
        return FlyState.values()[entityData.get(FLY_STATE)];
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double sqrDistance) {
        return sqrDistance <= 32.0 * 32.0;
    }

    private void changeState(FlyState state) {
        if (getFlyState() != state) entityData.set(FLY_STATE, (byte) state.ordinal());
    }

    @Override
    protected void onRecalled() {
        if (ownerFrame != null) ownerFrame.attack().remove(this);
        ownerFrame = null;
        roaming.stop();
        nextOrbitCheck = Long.MIN_VALUE;
        changeState(FlyState.RETURN);
    }

    @Override
    protected final void tickFamiliar() {
        if (level().isClientSide) {
            tickClientInterpolation();
            return;
        }
        if (consumed || isRemoved() || !(getOwner() instanceof ServerPlayer owner) || !owner.isAlive()) return;
        long tick = level().getGameTime();
        ownerFrame = FlyOwnerFrame.get(owner);
        ownerFrame.refresh(owner, tick);
        if (!ownerFrame.contains(this)) return;
        FlyAttackQueue attack = ownerFrame.attack();
        LivingEntity target = ownerFrame.attackTarget(owner, this, tick);

        Vec3 goal;
        List<Vec3> steps;
        if (target != null) {
            roaming.stop();
            changeState(FlyState.CHASE);
            steps = attack.steps(this, tick);
        } else {
            Vec3 slot = ownerFrame.position(this);
            if (getFlyState() == FlyState.ROAM && !roaming.active(tick)) {
                roaming.stop();
                changeState(FlyState.RETURN);
            }
            // A scaled owner's ring can exceed the common recall range even in open air.
            // Stay near its waist instead of repeatedly flying outward and being replaced.
            boolean outsideRecallRange = slot.distanceToSqr(owner.position()) > MAX_VALID_DISTANCE * MAX_VALID_DISTANCE;
            if (!roaming.active(tick) && tick >= nextOrbitCheck) {
                nextOrbitCheck = tick + 4;
                if (outsideRecallRange || !FlyNavigation.clearPath(this, position(), slot)) roaming.start(tick);
            }
            if (roaming.active(tick)) {
                changeState(FlyState.ROAM);
                goal = roaming.goal(owner, tick);
            } else {
                changeState(position().distanceToSqr(slot) < (FlyOrbitLayout.IDLE_SPEED + 0.05) * (FlyOrbitLayout.IDLE_SPEED + 0.05)
                        ? FlyState.ORBIT : FlyState.RETURN);
                goal = slot;
            }
            if (goal == null) {
                setDeltaMovement(Vec3.ZERO);
                return;
            }
            double speed = getFlyState() == FlyState.RETURN ? FlyOrbitLayout.MAX_SPEED : FlyOrbitLayout.IDLE_SPEED;
            Vec3 delta = goal.subtract(position());
            steps = delta.lengthSqr() < 1e-14 ? List.of()
                    : List.of(delta.scale(Math.min(1, speed / delta.length())));
        }

        Vec3 start = position();
        for (Vec3 step : steps) {
            if (isRemoved()) return;
            Vec3 end = position().add(step);
            // Check actual movement once; a predecessor's trail never grants permission to clip.
            if (!FlyNavigation.clearPath(this, position(), end)) {
                if (target != null) attack.blocked(this);
                else {
                    if (getFlyState() == FlyState.ROAM) roaming.blocked(tick);
                    else roaming.start(tick);
                    changeState(FlyState.ROAM);
                }
                break;
            }
            if (target != null && tryContact(owner, position(), end)) return;
            Vec3 from = position();
            setPos(end);
            if (target != null) attack.moved(this, from, end);
        }
        Vec3 movement = position().subtract(start);
        // A movement sweep includes its start point; only stationary attacks need a separate test.
        if (target != null && movement.lengthSqr() < 1e-14
                && FlyNavigation.clearPath(this, position(), position())
                && tryContact(owner, position(), position())) return;
        setDeltaMovement(movement);
        if (movement.horizontalDistanceSqr() > 1e-10) {
            setYRot((float) (Math.toDegrees(Math.atan2(movement.z, movement.x)) - 90));
        }
    }

    private boolean tryContact(ServerPlayer owner, Vec3 from, Vec3 to) {
        double halfHeight = getBbHeight() * 0.5;
        Vec3 centerFrom = from.add(0, halfHeight, 0);
        Vec3 centerTo = to.add(0, halfHeight, 0);
        LivingEntity first = null;
        double firstParameter = Double.POSITIVE_INFINITY;
        for (LivingEntity candidate : ownerFrame.candidates()) {
            if (!ownerFrame.validForTick(owner, candidate)) continue;
            var hit = SweptCollision.segmentAabb(centerFrom, centerTo, candidate.getBoundingBox()
                    .inflate(getBbWidth() * 0.5, halfHeight, getBbWidth() * 0.5));
            if (hit.hit() && hit.parameter() < firstParameter && FlyOwnerFrame.validTarget(owner, candidate)) {
                first = candidate;
                firstParameter = hit.parameter();
            }
        }
        if (first == null) return false;
        var type = getFamiliarType();
        var data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        if (type == null || data == null || !data.containsEntity(type, getUUID())) return false;
        Vec3 hitPosition = from.lerp(to, firstParameter);
        setPos(hitPosition);
        ownerFrame.attack().moved(this, from, hitPosition);
        consumed = true; // Hurt handlers may reenter mod code, so claim this attempt before dispatch.
        float damage = getContactDamage(owner, first);
        if (!Float.isFinite(damage) || damage < 0) damage = 0;
        DamageSource source = ModDamageSources.familiar(owner.serverLevel(), this, owner);
        boolean accepted;
        try {
            // Consume ownership before hurt callbacks can replace this fly or add new requirements.
            // Keep the entity alive through hurt so direct-source attribution and shields still work.
            data.removeEntity(type, getUUID());
            data.addCount(type, -1);
            accepted = first.hurt(source, damage);
        } finally {
            discardSilently();
            ownerFrame.attack().remove(this);
        }
        onContactHit(owner, first, hitPosition.add(0, halfHeight, 0), damage, accepted);
        return true;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (teleport || !level().isClientSide) {
            super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
            lerpSteps = 0;
            return;
        }
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYaw = yaw;
        lerpPitch = pitch;
        lerpSteps = Math.max(1, steps);
    }

    private void tickClientInterpolation() {
        if (lerpSteps <= 0) return;
        setPos(getX() + (lerpX - getX()) / lerpSteps,
                getY() + (lerpY - getY()) / lerpSteps, getZ() + (lerpZ - getZ()) / lerpSteps);
        setYRot(getYRot() + Mth.wrapDegrees(lerpYaw - getYRot()) / lerpSteps);
        setXRot(getXRot() + (lerpPitch - getXRot()) / lerpSteps);
        lerpSteps--;
    }
}
