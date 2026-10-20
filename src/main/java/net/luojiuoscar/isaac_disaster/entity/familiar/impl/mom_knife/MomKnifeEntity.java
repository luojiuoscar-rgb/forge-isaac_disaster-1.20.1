package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** A locked target owns the rise/aim/thrust cycle; the owner owns the return slot. */
public class MomKnifeEntity extends AbstractIsaacFamiliarEntity {
    public enum State { FOLLOW, RISE, AIM, THRUST, PAUSE, RETRACT, RETURN }
    private static final State[] STATES = State.values();

    private static final EntityDataAccessor<Byte> BEHAVIOR =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> POSE_YAW =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> POSE_PITCH =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> POSE_TWIST =
            SynchedEntityData.defineId(MomKnifeEntity.class, EntityDataSerializers.FLOAT);

    private final MomKnifeMotion motion = new MomKnifeMotion();
    private final Set<UUID> hitVictims = new HashSet<>();
    @Nullable private UUID targetId;
    private Vec3 riseOrigin = Vec3.ZERO;
    private double riseHeight;
    private int attackLayerCount = 1;
    private MomKnifePose aimStart = new MomKnifePose(0, 0, 0);
    private MomKnifePose launchPose = new MomKnifePose(0, 0, 0);
    private Vec3 launchOrigin = Vec3.ZERO;
    private double remainingFlight;
    private MomKnifePose returnStart = new MomKnifePose(0, 0, 0);
    private long stateStarted;
    private int cooldown;
    private long nextTargetSearch = Long.MIN_VALUE;
    @Nullable private MomKnifeTargeting targeting;

    public MomKnifeEntity(EntityType<? extends MomKnifeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double sqrDistance) {
        return sqrDistance <= 32.0 * 32.0;
    }

    public State getBehaviorState() {
        return STATES[Mth.clamp(entityData.get(BEHAVIOR), 0, STATES.length - 1)];
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BEHAVIOR, (byte) State.FOLLOW.ordinal());
        entityData.define(POSE_YAW, 0.0F);
        entityData.define(POSE_PITCH, 0.0F);
        entityData.define(POSE_TWIST, 0.0F);
    }

    @Override
    protected void tickFamiliar() {
        if (level().isClientSide) {
            tickClient();
            return;
        }
        if (!(getOwner() instanceof ServerPlayer owner) || !owner.isAlive()) {
            moveToPoint(position());
            return;
        }
        if (cooldown > 0) cooldown--;
        long now = level().getGameTime();
        switch (getBehaviorState()) {
            case FOLLOW -> follow(owner, now);
            case RISE -> rise(owner, now);
            case AIM -> aim(owner, now);
            case THRUST -> thrust(owner, now);
            case PAUSE -> pause(now);
            case RETRACT -> retract(owner, now);
            case RETURN -> returnToOwner(owner, now);
        }
    }

    private void follow(ServerPlayer owner, long now) {
        moveToPoint(MomKnifeMotion.follow(position(), slot(owner)));
        setPose(new MomKnifePose(MomKnifeMotion.facingYaw(position(), owner.position(),
                entityData.get(POSE_YAW)), 0, 0));
        if (cooldown > 0 || now < nextTargetSearch) return;
        nextTargetSearch = now + MomKnifeTargeting.SCAN_INTERVAL;
        int layers = layerCount(owner);
        LivingEntity target = findTarget(owner, layers);
        if (target == null) return;
        hitVictims.clear();
        targetId = target.getUUID();
        attackLayerCount = layers;
        riseOrigin = position();
        riseHeight = MomKnifeAttack.riseHeight(random.nextDouble());
        aimStart = getRenderPose(0);
        changeState(State.RISE, now);
    }

    private void rise(ServerPlayer owner, long now) {
        LivingEntity target = target();
        if (!MomKnifeTargeting.validTarget(owner, target)) {
            beginReturn(now);
            return;
        }
        moveToPoint(MomKnifeAttack.rise(riseOrigin, riseHeight, age(now)));
        setPose(aimStart);
        if (age(now) >= MomKnifeAttack.RISE_TICKS) {
            aimStart = getRenderPose(0);
            changeState(State.AIM, now);
        }
    }

    private void aim(ServerPlayer owner, long now) {
        LivingEntity target = target();
        if (!MomKnifeTargeting.validTarget(owner, target)) {
            beginReturn(now);
            return;
        }
        moveToPoint(position());
        setPose(MomKnifeAttack.aim(position(), target.getBoundingBox().getCenter(), aimStart, age(now)));
        if (age(now) >= MomKnifeAttack.AIM_TICKS) {
            launchPose = getRenderPose(0);
            launchOrigin = position();
            remainingFlight = MomKnifeAttack.thrustDistance(attackLayerCount);
            changeState(State.THRUST, now);
        }
    }

    private void thrust(ServerPlayer owner, long now) {
        Vec3 current = position();
        Vec3 next = MomKnifeAttack.advance(current, launchPose, remainingFlight);
        setPose(launchPose);
        remainingFlight -= next.distanceTo(current);
        moveToPoint(next);
        damageTarget(owner, current, next);
        if (remainingFlight <= 1e-6) beginPause(now);
    }

    private void pause(long now) {
        setPose(launchPose);
        moveToPoint(position());
        if (age(now) >= MomKnifeAttack.PAUSE_TICKS) beginRetract(now);
    }

    private void beginPause(long now) {
        // The outward leg has ended; retraction may hit the same victim once again.
        hitVictims.clear();
        remainingFlight = 0;
        setPose(launchPose);
        changeState(State.PAUSE, now);
        moveToPoint(position());
    }

    private void beginRetract(long now) {
        setPose(launchPose);
        changeState(State.RETRACT, now);
        moveToPoint(position());
    }

    private void retract(ServerPlayer owner, long now) {
        Vec3 current = position();
        Vec3 next = MomKnifeAttack.retract(current, launchOrigin);
        setPose(launchPose);
        moveToPoint(next);
        damageTarget(owner, current, next);
        if (position().equals(launchOrigin)) beginReturn(now);
    }

    private void damageTarget(ServerPlayer owner, Vec3 current, Vec3 next) {
        LivingEntity target = target();
        if (target == null || hitVictims.contains(target.getUUID()) || !MomKnifeTargeting.validTarget(owner, target)
                || !MomKnifeAttack.contacts(current, next, launchPose, target.getBoundingBox())) return;
        float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2;
        if (!isRemoved() && !isRecallPending() && Float.isFinite(damage) && damage > 0
                && target.hurt(ModDamageSources.familiar(level(), this, owner), damage)) {
            hitVictims.add(target.getUUID());
        }
    }

    private void beginReturn(long now) {
        hitVictims.clear();
        targetId = null;
        attackLayerCount = 1;
        riseOrigin = Vec3.ZERO;
        riseHeight = 0;
        remainingFlight = 0;
        launchOrigin = Vec3.ZERO;
        returnStart = getRenderPose(0);
        cooldown = MomKnifeAttack.MIN_COOLDOWN
                + random.nextInt(MomKnifeAttack.MAX_COOLDOWN - MomKnifeAttack.MIN_COOLDOWN + 1);
        changeState(State.RETURN, now);
        moveToPoint(position());
    }

    private void returnToOwner(ServerPlayer owner, long now) {
        Vec3 goal = slot(owner);
        moveToPoint(MomKnifeMotion.fly(position(), goal, MomKnifeAttack.RETURN_SPEED));
        float ownerYaw = MomKnifeMotion.facingYaw(position(), owner.position(), returnStart.yaw());
        setPose(MomKnifeAttack.returnPose(ownerYaw, returnStart, age(now)));
        if (position().distanceToSqr(goal) < 1e-6 && age(now) >= MomKnifeAttack.RETURN_TICKS) {
            changeState(State.FOLLOW, now);
        }
    }

    private Vec3 slot(LivingEntity owner) {
        return MomKnifeMotion.formationPosition(owner.position(), owner.getBbWidth(), owner.getBbHeight(),
                owner.getYRot(), getFormationIndex());
    }

    @Override
    protected boolean exceedsRecallDistance(Player owner) {
        if (!super.exceedsRecallDistance(owner)) return false;
        double extent = slot(owner).distanceTo(owner.position()) + MomKnifeAttack.MAX_RISE_HEIGHT + 1;
        State state = getBehaviorState();
        if (state != State.FOLLOW && state != State.RETURN) {
            extent += MomKnifeAttack.thrustDistance(attackLayerCount);
        }
        return distanceToSqr(owner) > extent * extent;
    }

    private void moveToPoint(Vec3 next) {
        setDeltaMovement(next.subtract(position()));
        setPos(next);
    }

    private void setPose(MomKnifePose pose) {
        entityData.set(POSE_YAW, pose.yaw());
        entityData.set(POSE_PITCH, pose.pitch());
        entityData.set(POSE_TWIST, pose.twist());
    }

    private void changeState(State next, long now) {
        stateStarted = now;
        entityData.set(BEHAVIOR, (byte) next.ordinal());
    }

    private long age(long now) {
        return Math.max(0, now - stateStarted);
    }

    @Nullable
    private LivingEntity target() {
        if (targetId == null || !(level() instanceof ServerLevel server)) return null;
        return server.getEntity(targetId) instanceof LivingEntity living ? living : null;
    }

    private int layerCount(ServerPlayer owner) {
        int knifeCount = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .map(data -> data.getEntry(ModFamiliarEntities.MOM_KNIFE.getId())
                        .map(entry -> entry.getEntityCount()).orElse(1)).orElse(1);
        return MomKnifeMotion.formationLayers(knifeCount);
    }

    @Nullable
    private LivingEntity findTarget(ServerPlayer owner, int layers) {
        targeting = MomKnifeTargeting.get(owner);
        return targeting.select(owner, position(), getFormationIndex(), random, layers);
    }

    @Override
    protected void onRecalled() {
        hitVictims.clear();
        targetId = null;
        attackLayerCount = 1;
        riseOrigin = Vec3.ZERO;
        riseHeight = 0;
        remainingFlight = 0;
        launchOrigin = Vec3.ZERO;
        returnStart = new MomKnifePose(entityData.get(POSE_YAW), 0, 0);
        cooldown = MomKnifeAttack.MIN_COOLDOWN;
        nextTargetSearch = Long.MIN_VALUE;
        targeting = null;
        setPose(new MomKnifePose(entityData.get(POSE_YAW), 0, 0));
        changeState(State.FOLLOW, level().getGameTime());
        motion.reset();
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (teleport || !level().isClientSide) {
            super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
            motion.reset();
            setOldPosAndRot();
            return;
        }
        // Vanilla updates the codec first; rotation-only packets retain its authoritative base.
        motion.acceptPosition(getPositionCodec().decode(0, 0, 0), steps);
    }

    private void tickClient() {
        Vec3 before = position();
        Vec3 next = motion.tickPosition(before);
        if (next != before) setPos(next);
        State state = getBehaviorState();
        boolean attacking = state == State.RISE || state == State.AIM
                || state == State.THRUST || state == State.PAUSE || state == State.RETRACT;
        float desiredYaw = entityData.get(POSE_YAW);
        LivingEntity owner = getOwner();
        if (state == State.FOLLOW && owner != null) {
            desiredYaw = MomKnifeMotion.facingYaw(position(), owner.position(), desiredYaw);
        }
        // Return uses the server's eased pose, rather than turning toward its travel velocity.
        motion.turnTo(desiredYaw, attacking ? 90 : state == State.RETURN ? 45 : 18);
        motion.turnPitchTo(entityData.get(POSE_PITCH), attacking ? 60 : 22.5F);
        motion.turnTwistTo(entityData.get(POSE_TWIST), attacking ? 90 : 45);
    }

    /** Display angles interpolate locally; the shared handle-tail pivot stays fixed. */
    public MomKnifePose getRenderPose(float partialTick) {
        return level().isClientSide
                ? new MomKnifePose(motion.renderYaw(partialTick), motion.renderPitch(partialTick),
                        motion.renderTwist(partialTick))
                : new MomKnifePose(entityData.get(POSE_YAW), entityData.get(POSE_PITCH),
                        entityData.get(POSE_TWIST));
    }
}
