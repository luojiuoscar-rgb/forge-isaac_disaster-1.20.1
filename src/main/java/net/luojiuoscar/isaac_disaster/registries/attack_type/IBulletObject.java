package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface IBulletObject {
    SplitSequence getSplitSequence();

    AttackContext getAttackContext();

    BulletSourceType getSourceType();

    SplitTriggerCounts getSplitTriggerCounts();

    void recordSplitTrigger(SplitTriggerType type);

    float getDamage();

    Vec3 getVelocity();

    double getTraveled();

    double getRange();

    Vec3 getPosition();

    @Nullable
    LivingEntity getOwner();

    @Nullable
    Object getShooter();

    Vec3 getPrevShooterPos();

    double getStartYRot();

    double getStartXRot();

    boolean noGravity();

    boolean isHoming();

    boolean isSpectral();

    boolean isControllable();

    boolean isPiercing();

    ResourceLocation getColorId();

    Map<ResourceLocation, Integer> getTrajectories();

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
