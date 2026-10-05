package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import java.util.Set;

public interface IBulletObject {
    /**
     * Returns the live split sequence owned by this projectile. Split execution advances module
     * trigger counts on this sequence after a successful split; callers must not retain it.
     */
    SplitSequence getSplitSequence();

    AttackContext getAttackContext();

    ResourceLocation getTypeId();

    ResourceLocation getRootTypeId();

    /**
     * Returns this attack's position in a multi-projectile sequence. Ordinary projectiles use zero;
     * sequence-aware attacks, such as Brimstone, may expose their actual position.
     */
    default int getAttackSequenceIndex() { return 0; }

    SplitTriggerCounts getSplitTriggerCounts();

    void recordSplitTrigger(SplitTriggerType type);

    float getDamage();

    Vec3 getVelocity();

    /** Returns the current acceleration used by the shared trajectory evaluator. */
    default Vec3 getAcceleration() { return Vec3.ZERO; }

    double getTraveled();

    double getRange();

    Vec3 getPosition();

    @Nullable
    LivingEntity getOwner();

    @Nullable
    Object getShooter();


    boolean noGravity();

    boolean isHoming();

    boolean isSpectral();

    boolean isControllable();

    boolean isPiercing();

    ResourceLocation getColorId();

    /** The stable runtime owned by this projectile, never a transient copy. */
    TrajectoryRuntime getTrajectoryRuntime();

    default List<TrajectorySpec> getTrajectorySpecs() { return getTrajectoryRuntime().specs(); }

    default int getTrajectoryAge() { return 0; }

    /**
     * Returns the live trigger collection for runtime event extensions. Callers that only need to
     * inspect or transfer triggers should use {@link CompositeTrigger#copy()}.
     */
    CompositeTrigger getTriggers();

    @Nullable
    BlockHitResult getLastBlockHit();

    void setLastBlockHit(@Nullable BlockHitResult lastBlockHit);

    /** Returns the immutable block positions already contacted during this shot. */
    Set<BlockPos> getHitBlockPositions();

    /** Records one block contact and reports whether it is new for this shot. */
    boolean markBlockHit(@Nullable BlockPos position);

    DamagedEntities getDamagedEntities();

    /** Returns the level in which this bullet is simulated. */
    default Level getBulletLevel() {
        LivingEntity owner = getOwner();
        return owner == null ? null : owner.level();
    }

    /** Returns the collision center of the bullet. */
    default Vec3 getCenter() { return getPosition(); }

    /** Writes a collision-center position using the backend coordinate convention. */
    void setCenter(Vec3 center);

    /** Replaces the authoritative position. */
    void setPosition(Vec3 position);

    /** Replaces the authoritative velocity. */
    void setVelocity(Vec3 velocity);

    /** Redirects both visible velocity and the trajectory primary axis after a bounce or effect. */
    default void redirectTrajectory(Vec3 velocity) {
        if (velocity == null) return;
        setVelocity(velocity);
        getTrajectoryRuntime().redirect(getPosition(), velocity);
    }

    /** Returns the vertical collision-box size used by contact positioning. */
    default float getCollisionHeight() { return 0.25F; }

    /** Returns the horizontal collision-box size used by swept collision effects. */
    default float getCollisionWidth() { return getCollisionHeight(); }

    /** Returns the configured homing search range. */
    default double getHomingRange() { return 4.0D; }

    /** Nudges the bullet out of a collision surface to avoid immediate re-hit. */
    default void pushOutOfBlock(Vec3 outwardNormal) {
        if (outwardNormal == null || outwardNormal.lengthSqr() < 1.0E-12D) return;
        setCenter(getCenter().add(outwardNormal.normalize().scale(1.0E-4D)));
    }


}
