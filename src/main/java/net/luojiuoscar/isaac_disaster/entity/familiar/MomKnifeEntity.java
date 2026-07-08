package net.luojiuoscar.isaac_disaster.entity.familiar;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.state.FamiliarStateMachine;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Floating knife familiar for the Mom set that protects the owner's back.
 */
public class MomKnifeEntity extends AbstractIsaacFamiliarEntity
        implements FamiliarStateMachine<MomKnifeEntity.MomKnifeState> {
    private static final double SEARCH_RADIUS = 3.5;
    private static final double BACK_DOT_THRESHOLD = -0.25;
    private static final double IDLE_DISTANCE = 1.1;
    private static final double IDLE_SPACING = 0.45;
    private static final double IDLE_HEIGHT_FACTOR = 0.55;
    private static final double IDLE_LERP = 0.35;
    private static final double LAUNCH_SPEED = 0.8;
    private static final double RETURN_SPEED = 0.45;
    private static final double HITBOX_INFLATE = 0.25;
    private static final float DAMAGE = 6.0f;
    private static final int ATTACK_COOLDOWN = 20;
    private static final int CONTACT_DAMAGE_INTERVAL = 4;

    private static final EntityDataAccessor<Integer> KNIFE_STATE =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.INT);

    private MomKnifeState state = MomKnifeState.IDLE;
    @Nullable
    private UUID targetUUID;
    private int cooldown;
    private int contactDamageTimer;

    public MomKnifeEntity(EntityType<? extends MomKnifeEntity> type, Level level) {
        super(type, level);
    }

    public MomKnifeEntity(Level level, LivingEntity owner) {
        this(ModEntities.MOM_KNIFE.get(), level);
        setOwner(owner);
    }

    @Override
    protected void tickFamiliar() {
        LivingEntity owner = getOwner();
        if (owner == null) return;

        if (cooldown > 0) cooldown--;
        if (contactDamageTimer > 0) contactDamageTimer--;

        tickFamiliarState();
        damageTouchingEnemies(owner);
    }

    @Override
    public MomKnifeState getFamiliarState() {
        return state;
    }

    @Override
    public void setFamiliarState(MomKnifeState state) {
        this.state = state == null ? MomKnifeState.IDLE : state;
        entityData.set(KNIFE_STATE, this.state.ordinal());
    }

    @Override
    public void tickFamiliarState() {
        LivingEntity owner = getOwner();
        if (owner == null) return;
        refreshStateFromSyncedData();

        switch (state) {
            case IDLE -> tickIdle(owner);
            case LAUNCH -> tickLaunch(owner);
            case RETURN -> tickReturn(owner);
        }
    }

    private void tickIdle(LivingEntity owner) {
        Vec3 idlePos = getIdlePosition(owner);
        moveTowards(idlePos, IDLE_LERP);
        updateRenderYaw(owner.getYRot() + 180.0f);

        if (level().isClientSide) return;

        LivingEntity target = cooldown <= 0 ? findBackTarget(owner) : null;
        if (target != null) {
            setTarget(target);
            changeFamiliarState(MomKnifeState.LAUNCH);
        }
    }

    private void tickLaunch(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || (!level().isClientSide && !isValidTarget(owner, target))) {
            if (level().isClientSide) {
                tickReturn(owner);
            } else {
                changeFamiliarState(MomKnifeState.RETURN);
                clearTarget();
            }
            return;
        }

        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        flyTowards(targetPos, LAUNCH_SPEED);

        if (!level().isClientSide && touches(target)) {
            clearTarget();
            cooldown = ATTACK_COOLDOWN;
            changeFamiliarState(MomKnifeState.RETURN);
        }
    }

    private void tickReturn(LivingEntity owner) {
        Vec3 idlePos = getIdlePosition(owner);
        flyTowards(idlePos, RETURN_SPEED);

        if (position().distanceToSqr(idlePos) < 0.12) {
            if (!level().isClientSide) {
                changeFamiliarState(MomKnifeState.IDLE);
            }
            setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * Finds the nearest non-friendly living entity behind the owner.
     */
    @Nullable
    private LivingEntity findBackTarget(LivingEntity owner) {
        AABB searchBox = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> candidates = level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                target -> isValidTarget(owner, target) && isBehindOwner(owner, target)
        );

        return candidates.stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(owner)))
                .orElse(null);
    }

    private boolean isValidTarget(LivingEntity owner, LivingEntity target) {
        return target != owner
                && target.isAlive()
                && !target.isInvulnerable()
                && !EntityHelper.isFriendly(target, owner);
    }

    private boolean isBehindOwner(LivingEntity owner, LivingEntity target) {
        Vec3 forward = Vec3.directionFromRotation(0, owner.getYRot()).normalize();
        Vec3 toTarget = target.position().subtract(owner.position()).multiply(1, 0, 1);
        if (toTarget.lengthSqr() < 1e-6) return false;
        return forward.dot(toTarget.normalize()) < BACK_DOT_THRESHOLD;
    }

    private Vec3 getIdlePosition(LivingEntity owner) {
        Vec3 forward = Vec3.directionFromRotation(0, owner.getYRot()).normalize();
        Vec3 back = forward.scale(-IDLE_DISTANCE);
        Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize().scale(getSideOffset());
        double height = owner.getBbHeight() * IDLE_HEIGHT_FACTOR;
        return owner.position().add(back).add(side).add(0, height, 0);
    }

    private double getSideOffset() {
        int index = getFamiliarIndex();
        if (index <= 0) return 0;
        int order = (index + 1) / 2;
        return (index % 2 == 1 ? -1 : 1) * order * IDLE_SPACING;
    }

    private void moveTowards(Vec3 targetPos, double lerp) {
        Vec3 next = position().lerp(targetPos, lerp);
        setPos(next);
        setDeltaMovement(Vec3.ZERO);
    }

    private void flyTowards(Vec3 targetPos, double speed) {
        Vec3 toTarget = targetPos.subtract(position());
        if (toTarget.lengthSqr() < 1e-6) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }

        Vec3 movement = toTarget.normalize().scale(Math.min(speed, toTarget.length()));
        setDeltaMovement(movement);
        move(MoverType.SELF, movement);
        updateRenderYaw((float) (Mth.atan2(movement.z, movement.x) * Mth.RAD_TO_DEG) - 90.0f);
    }

    @Nullable
    private LivingEntity getTarget() {
        int targetId = entityData.get(TARGET_ENTITY_ID);
        if (targetId != -1) {
            Entity entity = level().getEntity(targetId);
            if (entity instanceof LivingEntity livingEntity) {
                targetUUID = livingEntity.getUUID();
                return livingEntity;
            }
        }

        if (targetUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        LivingEntity target = serverLevel.getEntity(targetUUID) instanceof LivingEntity livingEntity ? livingEntity : null;
        if (target != null) {
            entityData.set(TARGET_ENTITY_ID, target.getId());
        }
        return target;
    }

    /**
     * Damages every non-friendly living entity touching the knife, independent of the current movement state.
     */
    private void damageTouchingEnemies(LivingEntity owner) {
        if (level().isClientSide || contactDamageTimer > 0) return;

        AABB damageBox = getBoundingBox().inflate(HITBOX_INFLATE);
        List<LivingEntity> targets = level().getEntitiesOfClass(
                LivingEntity.class,
                damageBox,
                target -> isValidTarget(owner, target) && touches(target)
        );
        if (targets.isEmpty()) return;

        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            target.hurt(createDamageSource(owner), DAMAGE);
        }
        contactDamageTimer = CONTACT_DAMAGE_INTERVAL;
    }

    private boolean touches(LivingEntity target) {
        return getBoundingBox().inflate(HITBOX_INFLATE).intersects(target.getBoundingBox());
    }

    private void setTarget(@Nullable LivingEntity target) {
        targetUUID = target == null ? null : target.getUUID();
        entityData.set(TARGET_ENTITY_ID, target == null ? -1 : target.getId());
    }

    private void clearTarget() {
        setTarget(null);
    }

    private void refreshStateFromSyncedData() {
        if (!level().isClientSide) return;
        int syncedState = Mth.clamp(entityData.get(KNIFE_STATE), 0, MomKnifeState.values().length - 1);
        state = MomKnifeState.values()[syncedState];
    }

    private DamageSource createDamageSource(LivingEntity owner) {
        if (level() instanceof ServerLevel serverLevel) {
            return new DamageSource(serverLevel.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.MOB_ATTACK), this, owner);
        }
        return damageSources().generic();
    }

    private void updateRenderYaw(float yaw) {
        setYRot(yaw);
        yRotO = yaw;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(KNIFE_STATE, MomKnifeState.IDLE.ordinal());
        entityData.define(TARGET_ENTITY_ID, -1);
    }

    public enum MomKnifeState {
        IDLE,
        LAUNCH,
        RETURN
    }
}
