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
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    private static final double IDLE_DISTANCE = 0.55;
    private static final double IDLE_SPACING = 0.45;
    private static final double IDLE_HEIGHT_FACTOR = 0.7;
    private static final double IDLE_LERP = 0.25;
    private static final double APPROACH_SPEED = 0.65;
    private static final double RETURN_SPEED = 0.45;
    private static final double HITBOX_INFLATE = 0.25;
    private static final float DAMAGE_MULTIPLIER = 2.0f;
    private static final int MIN_ATTACK_COOLDOWN = 10;
    private static final int MAX_ATTACK_COOLDOWN = 30;
    private static final int CONTACT_DAMAGE_INTERVAL = 4;
    private static final int ALIGN_TICKS = 3;
    private static final int RAISE_TICKS = 6;
    private static final int SLASH_TICKS = 4;
    private static final int RECOVER_TICKS = 5;
    private static final int ATTACK_PLAN_SYNC_TICKS = 1;
    private static final int RETURN_VISUAL_RECOVER_TICKS = 8;

    /*
     * Visual animation notes for the vanilla iron sword baked model renderer:
     * - ALIGN quickly twists the blade before the large raise begins.
     * - Positive visual pitch raises the blade from its neutral downward pose.
     * - Blade twist starts at 0 so the flat side faces the owner, then turns toward
     *   edge alignment during the attack and returns to 0 while flying home.
     * - MomKnifeRenderer applies these degree values directly to the item model PoseStack.
     */
    private static final float RAISE_VISUAL_PITCH = 155.0f;
    private static final float SLASH_VISUAL_PITCH = 25.0f;
    private static final float RECOVER_VISUAL_PITCH = 8.0f;
    private static final float ATTACK_EDGE_TWIST = 90.0f;
    private static final float RETURN_EDGE_TWIST = 45.0f;

    private static final EntityDataAccessor<Integer> KNIFE_STATE =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ATTACK_YAW =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);

    private MomKnifeState state = MomKnifeState.IDLE;
    @Nullable
    private UUID targetUUID;
    private Vec3 attackDirection = Vec3.ZERO;
    private int cooldown;
    private int contactDamageTimer;
    private int stateTicks;

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
        if (state == MomKnifeState.SLASH) {
            damageTouchingEnemies(owner);
        }
    }

    @Override
    public MomKnifeState getFamiliarState() {
        return state;
    }

    @Override
    public void setFamiliarState(MomKnifeState state) {
        MomKnifeState nextState = state == null ? MomKnifeState.IDLE : state;
        if (this.state != nextState) {
            stateTicks = 0;
        }
        this.state = nextState;
        if (!level().isClientSide) {
            entityData.set(KNIFE_STATE, this.state.ordinal());
        }
    }

    @Override
    public void tickFamiliarState() {
        LivingEntity owner = getOwner();
        if (owner == null) return;
        refreshStateFromSyncedData();
        stateTicks++;

        switch (state) {
            case IDLE -> tickIdle(owner);
            case APPROACH -> tickApproach(owner);
            case ALIGN -> tickAlign(owner);
            case RAISE -> tickRaise(owner);
            case SLASH -> tickSlash(owner);
            case RECOVER -> tickRecover(owner);
            case RETURN -> tickReturn(owner);
        }
    }

    private void tickIdle(LivingEntity owner) {
        Vec3 idlePos = getIdlePosition(owner);
        moveTowards(idlePos, IDLE_LERP);
        faceOwnerBack(owner);

        if (level().isClientSide) return;

        LivingEntity target = cooldown <= 0 ? findBackTarget(owner) : null;
        if (target != null) {
            beginAttack(target);
            changeFamiliarState(MomKnifeState.APPROACH);
        }
    }

    private void tickApproach(LivingEntity owner) {
        if (waitForAttackPlan()) return;

        LivingEntity target = getTarget();
        if (holdClientOrReturn(owner, target)) return;

        Vec3 approachPos = getApproachPosition(owner, target);
        flyTowards(approachPos, APPROACH_SPEED);
        faceAttackDirection(owner, target, 18.0f);

        if (!level().isClientSide && (position().distanceToSqr(approachPos) < 0.18 || touches(target))) {
            changeFamiliarState(MomKnifeState.ALIGN);
        }
    }

    /**
     * Quickly turns the blade edge toward the target before the visible raise and slash start.
     */
    private void tickAlign(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (holdClientOrReturn(owner, target)) return;

        moveTowards(getApproachPosition(owner, target), 0.55);
        faceAttackDirection(owner, target, 90.0f);

        if (!level().isClientSide && stateTicks >= ALIGN_TICKS) {
            changeFamiliarState(MomKnifeState.RAISE);
        }
    }

    private void tickRaise(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (holdClientOrReturn(owner, target)) return;

        moveTowards(getRaisePosition(owner, target), 0.35);

        if (!level().isClientSide && stateTicks >= RAISE_TICKS) {
            changeFamiliarState(MomKnifeState.SLASH);
        }
    }

    private void tickSlash(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (target != null) {
            float progress = smoothProgress(stateTicks, SLASH_TICKS);
            Vec3 start = getRaisePosition(owner, target);
            Vec3 end = getSlashPosition(owner, target);
            moveTowards(start.lerp(end, progress), 0.85);
        } else {
            setDeltaMovement(Vec3.ZERO);
        }

        if (!level().isClientSide && stateTicks >= SLASH_TICKS) {
            changeFamiliarState(MomKnifeState.RECOVER);
        }
    }

    private void tickRecover(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            moveTowards(getSlashPosition(owner, target), 0.45);
        }

        if (!level().isClientSide && stateTicks >= RECOVER_TICKS) {
            cooldown = getRandomAttackCooldown();
            beginReturn();
        }
    }

    private void tickReturn(LivingEntity owner) {
        Vec3 idlePos = getIdlePosition(owner);
        flyTowards(idlePos, RETURN_SPEED);
        faceOwnerBack(owner);

        if (position().distanceToSqr(idlePos) < 0.12) {
            if (!level().isClientSide) {
                changeFamiliarState(MomKnifeState.IDLE);
            }
            setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * Finds a non-friendly living entity behind the owner using the knife's formation slot behavior.
     */
    @Nullable
    private LivingEntity findBackTarget(LivingEntity owner) {
        AABB searchBox = getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> candidates = level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                target -> isValidTarget(owner, target) && isBehindOwner(owner, target)
        );

        if (candidates.isEmpty()) return null;
        if (getFormationIndex() == 0) {
            return candidates.stream()
                    .min(Comparator.comparingDouble(this::distanceToSqr))
                    .orElse(null);
        }
        return candidates.get(random.nextInt(candidates.size()));
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

    private Vec3 getApproachPosition(LivingEntity owner, LivingEntity target) {
        Vec3 direction = getAttackDirection(owner, target);
        AABB targetBox = target.getBoundingBox();
        double radius = Math.max(targetBox.getXsize(), targetBox.getZsize()) * 0.5;
        return targetBox.getCenter()
                .subtract(direction.scale(radius + 0.85))
                .add(0, target.getBbHeight() * 0.12, 0);
    }

    private Vec3 getRaisePosition(LivingEntity owner, LivingEntity target) {
        Vec3 direction = getAttackDirection(owner, target);
        AABB targetBox = target.getBoundingBox();
        double radius = Math.max(targetBox.getXsize(), targetBox.getZsize()) * 0.5;
        return targetBox.getCenter()
                .subtract(direction.scale(radius + 0.45))
                .add(0, Math.min(1.2, target.getBbHeight() * 0.45 + 0.35), 0);
    }

    private Vec3 getSlashPosition(LivingEntity owner, LivingEntity target) {
        Vec3 direction = getAttackDirection(owner, target);
        AABB targetBox = target.getBoundingBox();
        double radius = Math.max(targetBox.getXsize(), targetBox.getZsize()) * 0.25;
        return targetBox.getCenter()
                .add(direction.scale(radius))
                .add(0, -Math.min(0.6, target.getBbHeight() * 0.25), 0);
    }

    private Vec3 getAttackDirection(LivingEntity owner, LivingEntity target) {
        Vec3 syncedDirection = getSyncedAttackDirection();
        if (syncedDirection.lengthSqr() > 1e-6) {
            attackDirection = syncedDirection;
            return attackDirection;
        }
        if (level().isClientSide) return Vec3.ZERO;

        if (attackDirection.lengthSqr() > 1e-6) return attackDirection;

        Vec3 direction = target.getBoundingBox().getCenter().subtract(position()).multiply(1, 0, 1);
        if (direction.lengthSqr() < 1e-6) {
            direction = Vec3.directionFromRotation(0, owner.getYRot()).multiply(1, 0, 1);
        }
        setAttackDirection(direction);
        return attackDirection;
    }

    private double getSideOffset() {
        int index = getFormationIndex();
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
     * Damages non-friendly living entities touching the knife while the slash state is active.
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
            target.hurt(createDamageSource(owner), getDamage(owner));
        }
        contactDamageTimer = CONTACT_DAMAGE_INTERVAL;
    }

    /**
     * Returns Mom's knife slash damage from the owner's current total attack damage attribute.
     */
    private float getDamage(LivingEntity owner) {
        return (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * DAMAGE_MULTIPLIER;
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

    /**
     * Locks attack target data so this knife launches from its current slot instead of the owner's center line.
     */
    private void beginAttack(LivingEntity target) {
        setTarget(target);
        lockAttackDirection(target);
    }

    /**
     * Stores a stable horizontal attack direction for the current swing sequence.
     */
    private void lockAttackDirection(LivingEntity target) {
        Vec3 direction = target.getBoundingBox().getCenter().subtract(position()).multiply(1, 0, 1);
        if (direction.lengthSqr() < 1e-6) {
            direction = Vec3.directionFromRotation(0, getYRot()).multiply(1, 0, 1);
        }
        setAttackDirection(direction);
    }

    private void beginReturn() {
        clearAttackDirection();
        if (!level().isClientSide) {
            clearTarget();
        }
        changeFamiliarState(MomKnifeState.RETURN);
    }

    /**
     * Gives synced attack data one tick to reach clients before movement starts.
     */
    private boolean waitForAttackPlan() {
        if (stateTicks <= ATTACK_PLAN_SYNC_TICKS) {
            setDeltaMovement(Vec3.ZERO);
            return true;
        }
        if (level().isClientSide && getSyncedAttackDirection().lengthSqr() <= 1e-6) {
            setDeltaMovement(Vec3.ZERO);
            return true;
        }
        return false;
    }

    /**
     * Lets the server own attack cancellation while clients wait for temporary missing target data.
     */
    private boolean holdClientOrReturn(LivingEntity owner, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            if (!level().isClientSide) {
                beginReturn();
            } else {
                setDeltaMovement(Vec3.ZERO);
            }
            return true;
        }

        if (!level().isClientSide && !isValidTarget(owner, target)) {
            beginReturn();
            return true;
        }
        return false;
    }

    /**
     * Stores a stable attack direction and exposes it to clients as one horizontal yaw value.
     */
    private void setAttackDirection(Vec3 direction) {
        if (direction.lengthSqr() < 1e-6) return;
        attackDirection = direction.normalize();
        if (!level().isClientSide) {
            entityData.set(ATTACK_YAW, yawFromDirection(attackDirection));
        }
    }

    /**
     * Clears the current attack plan when the knife is no longer performing an attack.
     */
    private void clearAttackDirection() {
        attackDirection = Vec3.ZERO;
        if (!level().isClientSide) {
            entityData.set(ATTACK_YAW, Float.NaN);
        }
    }

    /**
     * Reads the synchronized attack direction used by client-side movement prediction.
     */
    private Vec3 getSyncedAttackDirection() {
        float yaw = entityData.get(ATTACK_YAW);
        if (Float.isNaN(yaw)) return Vec3.ZERO;
        return Vec3.directionFromRotation(0.0f, yaw).multiply(1, 0, 1).normalize();
    }

    /**
     * Converts a horizontal direction vector to the equivalent Minecraft entity yaw.
     */
    private float yawFromDirection(Vec3 direction) {
        return (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0f;
    }

    /**
     * Returns a post-attack cooldown from the configured random tick range.
     */
    private int getRandomAttackCooldown() {
        return MIN_ATTACK_COOLDOWN + random.nextInt(MAX_ATTACK_COOLDOWN - MIN_ATTACK_COOLDOWN + 1);
    }

    private void refreshStateFromSyncedData() {
        if (!level().isClientSide) return;
        int syncedState = Mth.clamp(entityData.get(KNIFE_STATE), 0, MomKnifeState.values().length - 1);
        MomKnifeState nextState = MomKnifeState.values()[syncedState];
        if (state != nextState) {
            state = nextState;
            stateTicks = 0;
            if (nextState == MomKnifeState.APPROACH || nextState == MomKnifeState.RETURN ||
                    nextState == MomKnifeState.IDLE) {
                attackDirection = Vec3.ZERO;
            }
        }
    }

    /**
     * Creates an indirect player attack source where the knife is direct source and the owner is causing source.
     */
    private DamageSource createDamageSource(LivingEntity owner) {
        if (level() instanceof ServerLevel serverLevel) {
            return new DamageSource(serverLevel.registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DamageTypes.PLAYER_ATTACK), this, owner);
        }
        return damageSources().generic();
    }

    private void faceOwnerBack(LivingEntity owner) {
        updateRenderYaw(owner.getYRot() + 180.0f, 12.0f);
    }

    private void faceTarget(LivingEntity target, float maxStep) {
        Vec3 direction = target.position().subtract(position());
        if (direction.multiply(1, 0, 1).lengthSqr() < 1e-6) return;
        updateRenderYaw(yawFromDirection(direction), maxStep);
    }

    private void faceAttackDirection(LivingEntity owner, LivingEntity target, float maxStep) {
        Vec3 direction = getAttackDirection(owner, target);
        if (direction.lengthSqr() < 1e-6) return;
        updateRenderYaw(yawFromDirection(direction), maxStep);
    }

    private void updateRenderYaw(float targetYaw, float maxStep) {
        float currentYaw = getYRot();
        float delta = Mth.wrapDegrees(targetYaw - currentYaw);
        setYRot(currentYaw + Mth.clamp(delta, -maxStep, maxStep));
    }

    private float smoothProgress(float ticks, int duration) {
        float progress = Mth.clamp(ticks / (float) Math.max(1, duration), 0.0f, 1.0f);
        return progress * progress * (3.0f - 2.0f * progress);
    }

    private float visualTicks(float partialTick) {
        return Math.max(0.0f, stateTicks - 1.0f + partialTick);
    }

    /**
     * Returns the model pitch in degrees used to sell the raise and slash animation without changing hit logic.
     */
    public float getVisualPitch(float partialTick) {
        float visualTicks = visualTicks(partialTick);
        return switch (state) {
            case ALIGN -> 0.0f;
            case RAISE -> Mth.lerp(smoothProgress(visualTicks, RAISE_TICKS), 0.0f, RAISE_VISUAL_PITCH);
            case SLASH -> Mth.lerp(smoothProgress(visualTicks, SLASH_TICKS), RAISE_VISUAL_PITCH, SLASH_VISUAL_PITCH);
            case RECOVER -> Mth.lerp(smoothProgress(visualTicks, RECOVER_TICKS),
                    SLASH_VISUAL_PITCH, RECOVER_VISUAL_PITCH);
            case RETURN -> Mth.lerp(smoothProgress(visualTicks, RETURN_VISUAL_RECOVER_TICKS),
                    RECOVER_VISUAL_PITCH, 0.0f);
            default -> 0.0f;
        };
    }

    /**
     * Returns the model twist in degrees that turns the blade from a passive flat-side pose into an attack edge pose.
     */
    public float getVisualBladeTwist(float partialTick) {
        float visualTicks = visualTicks(partialTick);
        return switch (state) {
            case ALIGN -> Mth.lerp(smoothProgress(visualTicks, ALIGN_TICKS), 0.0f, ATTACK_EDGE_TWIST);
            case RAISE, SLASH -> ATTACK_EDGE_TWIST;
            case RECOVER -> Mth.lerp(smoothProgress(visualTicks, RECOVER_TICKS), ATTACK_EDGE_TWIST, RETURN_EDGE_TWIST);
            case RETURN -> Mth.lerp(smoothProgress(visualTicks, RETURN_VISUAL_RECOVER_TICKS), RETURN_EDGE_TWIST, 0.0f);
            default -> 0.0f;
        };
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(KNIFE_STATE, MomKnifeState.IDLE.ordinal());
        entityData.define(TARGET_ENTITY_ID, -1);
        entityData.define(ATTACK_YAW, Float.NaN);
    }

    public enum MomKnifeState {
        IDLE,
        APPROACH,
        ALIGN,
        RAISE,
        SLASH,
        RECOVER,
        RETURN
    }
}
