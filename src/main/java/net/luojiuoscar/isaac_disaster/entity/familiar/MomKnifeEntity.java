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
    private static final double FORMATION_BASE_PADDING = 0.65;
    private static final double FORMATION_RING_SPACING = 0.55;
    private static final int FORMATION_FIRST_RING_CAPACITY = 5;
    private static final int FORMATION_RING_CAPACITY_STEP = 4;
    private static final double FORMATION_ARC_DEGREES = 160.0;
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
    private static final int ATTACK_WINDUP_TICKS = 1;
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
    private static final EntityDataAccessor<Integer> VISUAL_PLAN_SEQUENCE =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> VISUAL_PLAN_X =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VISUAL_PLAN_Y =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VISUAL_PLAN_Z =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> VISUAL_PLAN_YAW =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);

    private MomKnifeState state = MomKnifeState.IDLE;
    @Nullable
    private UUID targetUUID;
    private Vec3 attackDirection = Vec3.ZERO;
    private int cooldown;
    private int contactDamageTimer;
    private int stateTicks;
    private int formationRing;
    private float formationAngle;
    private Vec3 pendingServerMovement = Vec3.ZERO;
    private float pendingServerYaw;
    private boolean hasPendingServerPlan;
    private Vec3 nextServerMovement = Vec3.ZERO;
    private float nextServerYaw;
    private int visualPlanSequence;
    private Vec3 previousVisualPosition = Vec3.ZERO;
    private Vec3 visualPosition = Vec3.ZERO;
    private float previousVisualYaw;
    private float visualYaw;
    private Vec3 queuedVisualPosition = Vec3.ZERO;
    private float queuedVisualYaw;
    private int queuedVisualPlanSequence;
    private int lastVisualPlanSequence;
    private boolean hasVisualPlan;
    private boolean hasQueuedVisualPlan;

    public MomKnifeEntity(EntityType<? extends MomKnifeEntity> type, Level level) {
        super(type, level);
    }

    public MomKnifeEntity(Level level, LivingEntity owner) {
        this(ModEntities.MOM_KNIFE.get(), level);
        setOwner(owner);
    }

    /**
     * Updates the generic formation index and caches the knife-specific local formation slot on the server.
     */
    @Override
    public void setFormationIndex(int formationIndex) {
        int clampedIndex = Math.max(0, formationIndex);
        boolean changed = getFormationIndex() != clampedIndex;
        super.setFormationIndex(clampedIndex);
        if (!level().isClientSide && changed) {
            cacheFormationSlot(clampedIndex);
        }
    }

    @Override
    protected void tickFamiliar() {
        LivingEntity owner = getOwner();
        if (owner == null) return;

        if (level().isClientSide) {
            tickClientVisualState();
            return;
        }

        beginServerPlanningTick();
        if (cooldown > 0) cooldown--;
        if (contactDamageTimer > 0) contactDamageTimer--;

        tickFamiliarState();
        if (state == MomKnifeState.SLASH) {
            damageTouchingEnemies(owner);
        }
        publishVisualPlan();
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

    /**
     * Advances the client-only animation state without running movement, target selection, or damage logic.
     */
    private void tickClientVisualState() {
        refreshStateFromSyncedData();
        stateTicks++;
        initializeVisualPlanIfNeeded();
        queueSyncedVisualPlan();
        previousVisualPosition = visualPosition;
        previousVisualYaw = visualYaw;
        consumeQueuedVisualPlan();
    }

    /**
     * Applies the previous server-authored movement before computing the next one.
     */
    private void beginServerPlanningTick() {
        if (hasPendingServerPlan) {
            applyServerPlan(pendingServerMovement, pendingServerYaw);
        } else {
            stopMotion();
        }
        nextServerMovement = Vec3.ZERO;
        nextServerYaw = getYRot();
    }

    /**
     * Publishes the next visual position before the server executes that same movement next tick.
     */
    private void publishVisualPlan() {
        pendingServerMovement = nextServerMovement;
        pendingServerYaw = nextServerYaw;
        hasPendingServerPlan = true;
        visualPlanSequence = visualPlanSequence == Integer.MAX_VALUE ? 1 : visualPlanSequence + 1;

        Vec3 nextVisualPosition = position().add(pendingServerMovement);
        entityData.set(VISUAL_PLAN_X, (float) nextVisualPosition.x);
        entityData.set(VISUAL_PLAN_Y, (float) nextVisualPosition.y);
        entityData.set(VISUAL_PLAN_Z, (float) nextVisualPosition.z);
        entityData.set(VISUAL_PLAN_YAW, pendingServerYaw);
        entityData.set(VISUAL_PLAN_SEQUENCE, visualPlanSequence);
    }

    /**
     * Executes a movement/yaw pair on the server's real entity.
     */
    private void applyServerPlan(Vec3 movement, float yaw) {
        setYRot(yaw);
        if (movement.lengthSqr() < 1e-10) {
            stopMotion();
            return;
        }

        setDeltaMovement(movement);
        move(MoverType.SELF, movement);
    }

    /**
     * Copies the newest synced visual plan into a client-side queue without changing the active render segment.
     */
    private void queueSyncedVisualPlan() {
        int sequence = entityData.get(VISUAL_PLAN_SEQUENCE);
        if (sequence == 0 || sequence == queuedVisualPlanSequence || sequence == lastVisualPlanSequence) return;

        queuedVisualPlanSequence = sequence;
        queuedVisualPosition = new Vec3(
                entityData.get(VISUAL_PLAN_X),
                entityData.get(VISUAL_PLAN_Y),
                entityData.get(VISUAL_PLAN_Z)
        );
        queuedVisualYaw = entityData.get(VISUAL_PLAN_YAW);
        hasQueuedVisualPlan = true;
    }

    /**
     * Moves the queued server plan into the active interpolation segment at the client tick boundary.
     */
    private void consumeQueuedVisualPlan() {
        if (!hasQueuedVisualPlan || queuedVisualPlanSequence == lastVisualPlanSequence) return;

        lastVisualPlanSequence = queuedVisualPlanSequence;
        visualPosition = queuedVisualPosition;
        visualYaw = queuedVisualYaw;
        hasQueuedVisualPlan = false;
    }

    /**
     * Returns the render-only offset from the vanilla synchronized entity position to the planned visual position.
     */
    public Vec3 getVisualRenderOffset(float partialTick) {
        if (!level().isClientSide || !hasVisualPlan) return Vec3.ZERO;

        Vec3 entityRenderPosition = new Vec3(
                Mth.lerp(partialTick, xOld, getX()),
                Mth.lerp(partialTick, yOld, getY()),
                Mth.lerp(partialTick, zOld, getZ())
        );
        Vec3 visualRenderPosition = previousVisualPosition.lerp(visualPosition, partialTick);
        return visualRenderPosition.subtract(entityRenderPosition);
    }

    /**
     * Returns the render-only yaw selected by the same server-authored visual plan as the position.
     */
    public float getVisualRenderYaw(float partialTick) {
        if (!level().isClientSide || !hasVisualPlan) {
            return Mth.lerp(partialTick, yRotO, getYRot());
        }
        return Mth.rotLerp(partialTick, previousVisualYaw, visualYaw);
    }

    /**
     * Seeds client-side visual interpolation from the vanilla entity state until the first server plan arrives.
     */
    private void initializeVisualPlanIfNeeded() {
        if (hasVisualPlan) return;
        visualPosition = position();
        previousVisualPosition = visualPosition;
        visualYaw = getYRot();
        previousVisualYaw = visualYaw;
        hasVisualPlan = true;
    }

    private void tickIdle(LivingEntity owner) {
        Vec3 idlePos = getIdlePosition(owner);
        moveTowards(idlePos, IDLE_LERP);
        faceFormationCenter(owner);

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

        if (position().distanceToSqr(approachPos) < 0.18 || touches(target)) {
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

        if (stateTicks >= ALIGN_TICKS) {
            changeFamiliarState(MomKnifeState.RAISE);
        }
    }

    private void tickRaise(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (holdClientOrReturn(owner, target)) return;

        moveTowards(getRaisePosition(owner, target), 0.35);

        if (stateTicks >= RAISE_TICKS) {
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
            stopMotion();
        }

        if (stateTicks >= SLASH_TICKS) {
            changeFamiliarState(MomKnifeState.RECOVER);
        }
    }

    private void tickRecover(LivingEntity owner) {
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            moveTowards(getSlashPosition(owner, target), 0.45);
        }

        if (stateTicks >= RECOVER_TICKS) {
            cooldown = getRandomAttackCooldown();
            beginReturn();
        }
    }

    private void tickReturn(LivingEntity owner) {
        Vec3 idlePos = getIdlePosition(owner);
        flyTowards(idlePos, RETURN_SPEED);
        faceFormationCenter(owner);

        if (position().distanceToSqr(idlePos) < 0.12) {
            changeFamiliarState(MomKnifeState.IDLE);
            stopMotion();
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
        Vec3 forward = Vec3.directionFromRotation(0, owner.getYRot()).multiply(1, 0, 1).normalize();
        Vec3 back = forward.scale(-1.0);
        Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize();
        double angle = Math.toRadians(formationAngle);
        double radius = getFormationRadius(owner, formationRing);
        Vec3 horizontalOffset = back.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).normalize().scale(radius);
        double height = owner.getBbHeight() * IDLE_HEIGHT_FACTOR;
        return owner.position().add(horizontalOffset).add(0, height, 0);
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
        if (attackDirection.lengthSqr() > 1e-6) return attackDirection;

        Vec3 direction = target.getBoundingBox().getCenter().subtract(position()).multiply(1, 0, 1);
        if (direction.lengthSqr() < 1e-6) {
            direction = Vec3.directionFromRotation(0, owner.getYRot()).multiply(1, 0, 1);
        }
        setAttackDirection(direction);
        return attackDirection;
    }

    /**
     * Caches this knife's stable local slot in the owner's rear semicircle formation.
     */
    private void cacheFormationSlot(int formationIndex) {
        int remainingIndex = Math.max(0, formationIndex);
        int ring = 0;
        while (true) {
            int capacity = getFormationCapacity(ring);
            if (remainingIndex < capacity) {
                formationRing = ring;
                formationAngle = getFormationAngle(remainingIndex, capacity);
                return;
            }
            remainingIndex -= capacity;
            ring++;
        }
    }

    /**
     * Returns the radius of a rear semicircle ring after adapting to the owner's current width.
     */
    private double getFormationRadius(LivingEntity owner, int ring) {
        double baseRadius = Math.max(owner.getBbWidth() * 0.5 + FORMATION_BASE_PADDING, FORMATION_BASE_PADDING);
        return baseRadius + ring * FORMATION_RING_SPACING;
    }

    /**
     * Returns how many knives fit in the requested ring.
     * The capacity is intentionally independent from owner size so assigned ring slots stay stable.
     */
    private int getFormationCapacity(int ring) {
        return FORMATION_FIRST_RING_CAPACITY + ring * FORMATION_RING_CAPACITY_STEP;
    }

    /**
     * Converts a ring-local index into an angle offset from the owner's direct rear direction.
     */
    private float getFormationAngle(int localIndex, int capacity) {
        if (localIndex <= 0 || capacity <= 1) return 0.0f;
        int order = (localIndex + 1) / 2;
        int direction = localIndex % 2 == 1 ? -1 : 1;
        double maxOffset = FORMATION_ARC_DEGREES * 0.5;
        double angleStep = maxOffset / Math.max(1, (capacity - 1) / 2);
        return (float) (direction * order * angleStep);
    }

    private void moveTowards(Vec3 targetPos, double lerp) {
        if (level().isClientSide) return;
        Vec3 next = position().lerp(targetPos, lerp);
        queueMovement(next.subtract(position()));
    }

    private void flyTowards(Vec3 targetPos, double speed) {
        if (level().isClientSide) return;
        Vec3 toTarget = targetPos.subtract(position());
        if (toTarget.lengthSqr() < 1e-6) {
            stopMotion();
            return;
        }

        Vec3 movement = toTarget.normalize().scale(Math.min(speed, toTarget.length()));
        queueMovement(movement);
    }

    /**
     * Queues the movement that the server will execute next tick and clients will render as the next visual frame.
     */
    private void queueMovement(Vec3 movement) {
        if (level().isClientSide) return;
        if (movement.lengthSqr() < 1e-10) {
            stopMotion();
            return;
        }

        nextServerMovement = movement;
    }

    /**
     * Clears residual velocity and cancels the queued next movement.
     */
    private void stopMotion() {
        setDeltaMovement(Vec3.ZERO);
        if (!level().isClientSide) {
            nextServerMovement = Vec3.ZERO;
        }
    }

    @Nullable
    private LivingEntity getTarget() {
        if (targetUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return serverLevel.getEntity(targetUUID) instanceof LivingEntity livingEntity ? livingEntity : null;
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
        clearTarget();
        changeFamiliarState(MomKnifeState.RETURN);
    }

    /**
     * Keeps a short launch wind-up before movement starts after target and direction are locked.
     */
    private boolean waitForAttackPlan() {
        if (stateTicks <= ATTACK_WINDUP_TICKS) {
            stopMotion();
            return true;
        }
        return false;
    }

    /**
     * Cancels the attack and returns to the owner when the locked target is no longer usable.
     */
    private boolean holdClientOrReturn(LivingEntity owner, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            beginReturn();
            return true;
        }

        if (!isValidTarget(owner, target)) {
            beginReturn();
            return true;
        }
        return false;
    }

    /**
     * Stores a stable server-side attack direction for the current swing sequence.
     */
    private void setAttackDirection(Vec3 direction) {
        if (direction.lengthSqr() < 1e-6) return;
        attackDirection = direction.normalize();
    }

    /**
     * Clears the current locked attack direction when the knife is no longer performing an attack.
     */
    private void clearAttackDirection() {
        attackDirection = Vec3.ZERO;
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

    private void faceFormationCenter(LivingEntity owner) {
        Vec3 direction = owner.getBoundingBox().getCenter().subtract(position()).multiply(1, 0, 1);
        if (direction.lengthSqr() < 1e-6) {
            updateRenderYaw(owner.getYRot() + 180.0f, 12.0f);
            return;
        }
        updateRenderYaw(yawFromDirection(direction), 12.0f);
    }

    private void faceAttackDirection(LivingEntity owner, LivingEntity target, float maxStep) {
        Vec3 direction = getAttackDirection(owner, target);
        if (direction.lengthSqr() < 1e-6) return;
        updateRenderYaw(yawFromDirection(direction), maxStep);
    }

    private void updateRenderYaw(float targetYaw, float maxStep) {
        if (level().isClientSide) return;
        float currentYaw = nextServerYaw;
        float delta = Mth.wrapDegrees(targetYaw - currentYaw);
        nextServerYaw = currentYaw + Mth.clamp(delta, -maxStep, maxStep);
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
        entityData.define(VISUAL_PLAN_SEQUENCE, 0);
        entityData.define(VISUAL_PLAN_X, 0.0f);
        entityData.define(VISUAL_PLAN_Y, 0.0f);
        entityData.define(VISUAL_PLAN_Z, 0.0f);
        entityData.define(VISUAL_PLAN_YAW, 0.0f);
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
