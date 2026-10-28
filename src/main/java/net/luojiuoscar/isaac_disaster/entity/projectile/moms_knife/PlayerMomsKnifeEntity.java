package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsKnifeBarrage;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup.Slot;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ReturningKnifeTrajectory;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PlayerMomsKnifeEntity extends Entity implements IBulletObject, IEntityAdditionalSpawnData {
    public enum Mode { RETURNING, ONE_WAY }

    private static final EntityDataAccessor<Byte> MODE =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> HELD_SLOT =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> HELD_SOURCE =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Vector3f> LOCAL_OFFSET =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> BASE_DIRECTION =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> DIRECTION =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> TRAJECTORY_OFFSET =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> TRAJECTORY_DIRECTION =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> OWNER =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RANGE =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> LAUNCHED_AT =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<String> RETURNING_TRAJECTORY =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> FLIGHT_DURATION =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FAST_MOVING =
            SynchedEntityData.defineId(PlayerMomsKnifeEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable private Frame previous;
    @Nullable private ExecutableEffectContext barrageOnReturn;
    private final PlayerMomsKnifeTracking tracking = new PlayerMomsKnifeTracking();
    private PlayerMomsKnifeAttackState attack = new PlayerMomsKnifeAttackState(null, Vec3.ZERO,
            PlayerMomsKnifeMotion.FORWARD, 0, Vec3.ZERO);
    private double remainingDistance;
    private double oneWaySpeed;
    private long lastDamageTick = Long.MIN_VALUE;
    private Vec3 renderFrom = PlayerMomsKnifeMotion.FORWARD;
    private Vec3 renderTo = PlayerMomsKnifeMotion.FORWARD;
    private long directionUpdatedAt;
    private Vec3 renderOffsetFrom = Vec3.ZERO;
    private Vec3 renderOffsetTo = Vec3.ZERO;
    private long offsetUpdatedAt;
    @Nullable private Vec3 interpolationTarget;
    private int interpolationSteps;
    private List<PlayerMomsKnifeTrail.Sample> currentSweep = List.of();
    @Nullable private PlayerMomsKnifeMotion.Blade previousVisualBlade;
    @Nullable private Vec3 previousOneWayOwnerCenter;

    public PlayerMomsKnifeEntity(EntityType<? extends PlayerMomsKnifeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void initialize(ServerPlayer owner) {
        initialize(owner, 0, Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
    }

    public void initialize(ServerPlayer owner, int slot, Vec3 offset, Vec3 direction) {
        initialize(owner, new Slot(ModAttackTypes.MOMS_KNIFE.getId(), slot), offset, direction);
    }

    void initialize(ServerPlayer owner, Slot slot, Vec3 offset, Vec3 direction) {
        entityData.set(OWNER, owner.getId());
        setPos(PlayerMomsKnifeMotion.idlePosition(owner, 1));
        attack = new PlayerMomsKnifeAttackState(basicContext(owner), position(), owner.getLookAngle(), 0, Vec3.ZERO);
        configureHeld(slot, offset, direction);
        setYRot(owner.getYRot());
        setXRot(owner.getXRot());
        setOldPosAndRot();
    }

    public int getHeldSlot() { return entityData.get(HELD_SLOT); }
    public ResourceLocation getSourceId() { return ResourceLocation.parse(entityData.get(HELD_SOURCE)); }
    Slot getHeldAttackSlot() {
        return new Slot(getSourceId(), getHeldSlot());
    }

    private Vec3 localOffset() {
        Vector3f value = entityData.get(LOCAL_OFFSET);
        return new Vec3(value.x, value.y, value.z);
    }

    private Vec3 baseDirection() {
        Vector3f value = entityData.get(BASE_DIRECTION);
        return PlayerMomsKnifeMotion.unit(new Vec3(value.x, value.y, value.z));
    }

    private Vec3 heldAnchor(Player owner, float partialTick) {
        return PlayerMomsKnifeMotion.idlePosition(owner, partialTick).add(PlayerMomsKnifeMotion.offsetToWorld(
                localOffset(), level().isClientSide ? owner.getViewYRot(partialTick) : owner.getYRot(),
                level().isClientSide ? owner.getViewXRot(partialTick) : owner.getXRot()));
    }

    public void configureHeld(int slot, Vec3 offset, Vec3 direction) {
        configureHeld(new Slot(ModAttackTypes.MOMS_KNIFE.getId(), slot), offset, direction);
    }

    void configureHeld(Slot slot, Vec3 offset, Vec3 direction) {
        if (isInFlight()) return;
        direction = PlayerMomsKnifeMotion.unit(direction);
        Vector3f nextOffset = new Vector3f((float) offset.x, (float) offset.y, (float) offset.z);
        Vector3f nextDirection = new Vector3f((float) direction.x, (float) direction.y, (float) direction.z);
        if (getHeldAttackSlot().equals(slot) && entityData.get(LOCAL_OFFSET).equals(nextOffset)
                && entityData.get(BASE_DIRECTION).equals(nextDirection)) return;
        entityData.set(HELD_SLOT, slot.index());
        entityData.set(HELD_SOURCE, slot.sourceId().toString());
        entityData.set(LOCAL_OFFSET, nextOffset);
        entityData.set(BASE_DIRECTION, nextDirection);
        setDirection(direction);
        Player owner = getOwner();
        if (owner != null) setPosition(heldAnchor(owner, 1));
        previous = null;
        setOldPosAndRot();
    }

    private static AttackContext basicContext(ServerPlayer owner) {
        return AttackContext.builder(owner, owner).attackType(ModAttackTypes.MOMS_KNIFE.get()).build();
    }

    public boolean initializeOneWay(ServerPlayer owner, double range, double speed) {
        Vec3 origin = PlayerMomsKnifeMotion.idlePosition(owner, 1);
        return initializeOneWay(owner, origin, origin.add(owner.getLookAngle()), range, speed);
    }

    public boolean initializeOneWay(ServerPlayer owner, Vec3 origin, Vec3 aimPoint, double range, double speed) {
        return initializeOneWay(owner, origin, aimPoint, range, speed, basicContext(owner));
    }

    public boolean initializeOneWay(ServerPlayer owner, Vec3 origin, Vec3 aimPoint, double range, double speed,
                                    AttackContext context) {
        if (!Double.isFinite(range) || !Double.isFinite(speed) || range <= 0 || speed <= 0) return false;
        if (!Double.isFinite(origin.lengthSqr()) || !Double.isFinite(aimPoint.lengthSqr())) return false;
        if (context.getOwner() != owner) return false;
        entityData.set(MODE, (byte) Mode.ONE_WAY.ordinal());
        entityData.set(HELD_SLOT, -1);
        entityData.set(OWNER, owner.getId());
        setPos(origin);
        remainingDistance = range;
        oneWaySpeed = speed;
        entityData.set(RANGE, (float) range);
        entityData.set(SPEED, (float) speed);
        Vec3 direction = aimPoint.subtract(origin);
        if (direction.lengthSqr() < 1.0E-12) direction = owner.getLookAngle();
        setDirection(direction);
        attack = new PlayerMomsKnifeAttackState(context, origin, direction, range,
                PlayerMomsKnifeMotion.unit(direction).scale(speed));
        TrajectoryEvaluator.initialize(this);
        setDeltaMovement(attack.velocity);
        MomKnifePose pose = PlayerMomsKnifeMotion.pose(direction, owner.getYRot());
        setYRot(pose.yaw() - 180);
        setXRot(pose.pitch() + 90);
        setOldPosAndRot();
        return true;
    }

    public Mode getMode() {
        return entityData.get(MODE) == Mode.ONE_WAY.ordinal() ? Mode.ONE_WAY : Mode.RETURNING;
    }

    @Nullable
    public Player getOwner() {
        return level().getEntity(entityData.get(OWNER)) instanceof Player player ? player : null;
    }

    public boolean isInFlight() {
        return getMode() == Mode.ONE_WAY || entityData.get(RANGE) > 0;
    }

    public boolean launch(double range, double speed) {
        return getOwner() instanceof ServerPlayer owner && launch(range, speed, basicContext(owner));
    }

    public boolean launch(double range, double speed, AttackContext context) {
        return launch(range, speed, context, false);
    }

    public boolean launch(double range, double speed, AttackContext context, boolean triggerBarrage) {
        if (isInFlight() || !Double.isFinite(range) || !Double.isFinite(speed) || range <= 0 || speed <= 0
                || (float) Math.min(8, range) <= 0 || !Float.isFinite((float) speed) || (float) speed <= 0) return false;
        Player owner = getOwner();
        if (owner == null || context.getOwner() != owner) return false;
        tracking.clear();
        entityData.set(FAST_MOVING, false);
        Vec3 local = PlayerMomsKnifeMotion.toLocal(context.getMainAxis(), owner.getYRot(), owner.getXRot());
        entityData.set(BASE_DIRECTION, new Vector3f((float) local.x, (float) local.y, (float) local.z));
        setDirection(local);
        entityData.set(RANGE, (float) Math.min(8, range));
        entityData.set(SPEED, (float) speed);
        entityData.set(LAUNCHED_AT, level().getGameTime());
        previous = frame(owner);
        Vec3 world = PlayerMomsKnifeMotion.toWorld(local, owner.getYRot(), owner.getXRot());
        Vec3 velocity = world.scale(entityData.get(SPEED));
        attack = new PlayerMomsKnifeAttackState(context, position(), world, entityData.get(RANGE), velocity);
        entityData.set(RETURNING_TRAJECTORY, PlayerMomsKnifeTrajectory.selectReturning(this));
        ReturningKnifeTrajectory profile = returningTrajectory();
        double duration = profile == null ? 0 : profile.duration(
                PlayerMomsKnifeMotion.duration(entityData.get(RANGE), entityData.get(SPEED)));
        entityData.set(FLIGHT_DURATION, Double.isFinite(duration) && duration > 0
                ? (float) Math.min(Float.MAX_VALUE, duration) : 0.0F);
        clearTrajectoryPose();
        attack.trajectory.updateAnchor(owner);
        TrajectoryEvaluator.initialize(this);
        setDeltaMovement(velocity);
        barrageOnReturn = null;
        if (triggerBarrage) {
            AttackContext snapshot = context.copyConfiguration();
            snapshot.freeze();
            barrageOnReturn = new ExecutableEffectContext(owner);
            barrageOnReturn.set(ContextKeys.ATTACK_CONTEXT, snapshot);
        }
        return true;
    }

    public double getFlightDistance(float partialTick) {
        double straightDuration = PlayerMomsKnifeMotion.duration(entityData.get(RANGE), entityData.get(SPEED));
        double duration = getFlightDuration();
        return PlayerMomsKnifeMotion.distance(entityData.get(RANGE), entityData.get(SPEED),
                duration > 0 ? getFlightElapsedTicks(partialTick) * straightDuration / duration : 0);
    }

    public double getFlightDuration() {
        float frozen = entityData.get(FLIGHT_DURATION);
        return frozen > 0 ? frozen : PlayerMomsKnifeMotion.duration(entityData.get(RANGE), entityData.get(SPEED));
    }

    public double getFlightElapsedTicks(float partialTick) {
        return level().getGameTime() + partialTick - entityData.get(LAUNCHED_AT);
    }

    public boolean isFastMoving() { return entityData.get(FAST_MOVING); }

    @Nullable
    private ReturningKnifeTrajectory returningTrajectory() {
        return getMode() == Mode.RETURNING && isInFlight()
                ? PlayerMomsKnifeTrajectory.returning(entityData.get(RETURNING_TRAJECTORY)) : null;
    }

    private PlayerMomsKnifeMotion.Blade returningBlade(Player owner, float partialTick, ReturningKnifeTrajectory profile) {
        float interpolation = level().isClientSide ? partialTick : 1;
        Vec3 held = localOffset().add(0, 0, Math.max(0.6, owner.getBbWidth()));
        var local = profile.sampleLocal(held, baseDirection(), entityData.get(RANGE), getFlightDuration(),
                getFlightElapsedTicks(partialTick));
        Vec3 center = owner.getEyePosition(interpolation).add(0, -0.15 * owner.getBbHeight(), 0);
        return transformBlade(local, center, level().isClientSide ? owner.getViewYRot(partialTick) : owner.getYRot(),
                level().isClientSide ? owner.getViewXRot(partialTick) : owner.getXRot());
    }

    private static PlayerMomsKnifeMotion.Blade transformBlade(PlayerMomsKnifeMotion.Blade local,
                                                              Vec3 center, float yaw, float pitch) {
        return new PlayerMomsKnifeMotion.Blade(
                center.add(PlayerMomsKnifeMotion.offsetToWorld(local.anchor(), yaw, pitch)),
                center.add(PlayerMomsKnifeMotion.offsetToWorld(local.base(), yaw, pitch)),
                center.add(PlayerMomsKnifeMotion.offsetToWorld(local.tip(), yaw, pitch)), local.radius());
    }

    private Vec3 direction() {
        Vector3f value = entityData.get(DIRECTION);
        return PlayerMomsKnifeMotion.unit(new Vec3(value.x, value.y, value.z));
    }

    private void setDirection(Vec3 direction) {
        direction = PlayerMomsKnifeMotion.unit(direction);
        entityData.set(DIRECTION, new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
    }

    private Vec3 trajectoryOffset() {
        Vector3f value = entityData.get(TRAJECTORY_OFFSET);
        return new Vec3(value.x, value.y, value.z);
    }

    private Vec3 posedDirection() {
        Vector3f value = entityData.get(TRAJECTORY_DIRECTION);
        return value.lengthSquared() > 1e-12 ? PlayerMomsKnifeMotion.unit(new Vec3(value.x, value.y, value.z)) : direction();
    }

    private void clearTrajectoryPose() {
        entityData.set(TRAJECTORY_OFFSET, new Vector3f());
        entityData.set(TRAJECTORY_DIRECTION, new Vector3f());
    }

    private void applyTrajectoryPose(Player owner, List<PlayerMomsKnifeMotion.Blade> path, boolean active) {
        if (!active) {
            clearTrajectoryPose();
            return;
        }
        var end = path.get(path.size() - 1);
        Vec3 world = end.tip().subtract(end.base()).normalize();
        Vec3 local = getMode() == Mode.RETURNING
                ? PlayerMomsKnifeMotion.toLocal(world, owner.getYRot(), owner.getXRot()) : world;
        Vec3 offset = getMode() == Mode.RETURNING
                ? PlayerMomsKnifeMotion.offsetToLocal(end.anchor().subtract(heldAnchor(owner, 1))
                        .subtract(world.scale(getFlightDistance(0))), owner.getYRot(), owner.getXRot()) : Vec3.ZERO;
        entityData.set(TRAJECTORY_DIRECTION, new Vector3f((float) local.x, (float) local.y, (float) local.z));
        entityData.set(TRAJECTORY_OFFSET, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
    }

    public Vec3 getRenderDirection(Player owner, float partialTick) {
        ReturningKnifeTrajectory profile = returningTrajectory();
        if (profile != null) {
            var blade = returningBlade(owner, partialTick, profile);
            return PlayerMomsKnifeMotion.unit(blade.tip().subtract(blade.base()));
        }
        Vec3 direction = level().isClientSide
                ? PlayerMomsKnifeMotion.interpolateDirection(renderFrom, renderTo,
                        level().getGameTime() + partialTick - directionUpdatedAt) : posedDirection();
        return getMode() == Mode.RETURNING
                ? PlayerMomsKnifeMotion.toWorld(direction,
                        level().isClientSide ? owner.getViewYRot(partialTick) : owner.getYRot(),
                        level().isClientSide ? owner.getViewXRot(partialTick) : owner.getXRot())
                : direction;
    }

    public Vec3 getRenderAnchor(Player owner, float partialTick) {
        ReturningKnifeTrajectory profile = returningTrajectory();
        if (profile != null) return returningBlade(owner, partialTick, profile).anchor();
        Vec3 offset = level().isClientSide ? renderOffsetFrom.lerp(renderOffsetTo,
                Mth.clamp(level().getGameTime() + partialTick - offsetUpdatedAt, 0, 1)) : trajectoryOffset();
        return getMode() == Mode.RETURNING
                ? heldAnchor(owner, partialTick)
                        .add(getRenderDirection(owner, partialTick).scale(getFlightDistance(partialTick)))
                        .add(PlayerMomsKnifeMotion.offsetToWorld(offset,
                                level().isClientSide ? owner.getViewYRot(partialTick) : owner.getYRot(),
                                level().isClientSide ? owner.getViewXRot(partialTick) : owner.getXRot()))
                : getPosition(partialTick);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (level().isClientSide && (accessor.equals(LAUNCHED_AT)
                || accessor.equals(RANGE) && entityData.get(RANGE) <= 0)) {
            attack.trail.clear();
            previous = null;
            previousVisualBlade = null;
        }
        if ((accessor.equals(DIRECTION) || accessor.equals(TRAJECTORY_DIRECTION)) && level().isClientSide) {
            renderFrom = PlayerMomsKnifeMotion.interpolateDirection(renderFrom, renderTo,
                    level().getGameTime() - directionUpdatedAt);
            renderTo = posedDirection();
            directionUpdatedAt = level().getGameTime();
        }
        if (accessor.equals(TRAJECTORY_OFFSET) && level().isClientSide) {
            renderOffsetFrom = renderOffsetFrom.lerp(renderOffsetTo,
                    Mth.clamp(level().getGameTime() - offsetUpdatedAt, 0, 1));
            renderOffsetTo = trajectoryOffset();
            offsetUpdatedAt = level().getGameTime();
        }
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, (byte) Mode.RETURNING.ordinal());
        entityData.define(HELD_SLOT, 0);
        entityData.define(HELD_SOURCE, ModAttackTypes.MOMS_KNIFE.getId().toString());
        entityData.define(LOCAL_OFFSET, new Vector3f());
        entityData.define(BASE_DIRECTION, new Vector3f(0, 0, 1));
        entityData.define(DIRECTION, new Vector3f(0, 0, 1));
        entityData.define(TRAJECTORY_OFFSET, new Vector3f());
        entityData.define(TRAJECTORY_DIRECTION, new Vector3f());
        entityData.define(OWNER, -1);
        entityData.define(RANGE, 0.0F);
        entityData.define(SPEED, 1.0F);
        entityData.define(LAUNCHED_AT, 0L);
        entityData.define(RETURNING_TRAJECTORY, "");
        entityData.define(FLIGHT_DURATION, 0.0F);
        entityData.define(FAST_MOVING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (getMode() == Mode.ONE_WAY && interpolationTarget != null && interpolationSteps > 0) {
                setPos(position().lerp(interpolationTarget, 1.0 / interpolationSteps--));
            }
            tickVisualTrail();
            return;
        }
        Player candidate = getOwner();
        if (!(candidate instanceof ServerPlayer owner) || !MomsKnifeAttack.isSelectedAndReady(owner)
                || owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> !ability.getAttackTypes().containsKey(ModAttackTypes.MOMS_KNIFE.getId())
                        || getMode() == Mode.RETURNING
                        && !getUUID().equals(ability.getRuntimeEntities().getId(getSourceId(), getHeldSlot())))
                .orElse(true)) {
            discard();
            return;
        }

        boolean homing = owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getHoming() > 0).orElse(false);
        if (getMode() == Mode.ONE_WAY) tickOneWay(owner, homing);
        else tickReturning(owner, homing);
    }

    private Frame renderFrame(Player owner, float partialTick) {
        Vec3 center = owner.getEyePosition(partialTick).add(0, -0.15 * owner.getBbHeight(), 0);
        return new Frame(heldAnchor(owner, partialTick), owner.getViewYRot(partialTick), owner.getViewXRot(partialTick),
                direction(), level().getGameTime() + partialTick, center);
    }

    private void tickVisualTrail() {
        Player owner = getOwner();
        if (owner == null || !owner.isAlive() || owner.isSpectator()
                || !(owner.getMainHandItem().getItem() instanceof IsaacHead) || !isInFlight()) {
            attack.trail.clear();
            previous = null;
            previousVisualBlade = null;
            return;
        }
        Frame current = renderFrame(owner, 0);
        if (previous != null && current.time() > previous.time()) {
            if (current.time() - previous.time() <= 1 && current.center().distanceToSqr(previous.center()) <= 32 * 32) {
                attack.trail.recordPath(visualSweep(owner, current, 0), current.time(), isFastMoving());
            } else attack.trail.clear();
        }
        previous = current;
        previousVisualBlade = visualBlade(owner, 0);
    }

    private PlayerMomsKnifeMotion.Blade visualBlade(Player owner, float partialTick) {
        return PlayerMomsKnifeMotion.blade(getRenderAnchor(owner, partialTick), getRenderDirection(owner, partialTick));
    }

    private List<PlayerMomsKnifeTrail.Sample> visualSweep(Player owner, Frame current, float partialTick) {
        if (previous == null || previousVisualBlade == null) return List.of();
        if (returningTrajectory() != null) {
            sweep(previous, current);
            return currentSweep;
        }
        var blade = visualBlade(owner, partialTick);
        var path = sweepOneWay(previousVisualBlade.anchor(),
                PlayerMomsKnifeMotion.unit(previousVisualBlade.tip().subtract(previousVisualBlade.base())), blade.anchor(),
                PlayerMomsKnifeMotion.unit(blade.tip().subtract(blade.base())));
        return PlayerMomsKnifeTrail.timed(path, previous.time(), current.time());
    }

    public PlayerMomsKnifeTrail getRenderTrail(Player owner, float partialTick) {
        PlayerMomsKnifeTrail result = new PlayerMomsKnifeTrail();
        double now = level().getGameTime() + partialTick;
        if (!isInFlight()) return result;
        Frame current = renderFrame(owner, partialTick);
        if (previous != null && current.center().distanceToSqr(previous.center()) > 32 * 32) return result;
        result.append(attack.trail.samples(), now, PlayerMomsKnifeTrail.LIFETIME);
        if (previous != null && current.time() > previous.time() && current.time() - previous.time() <= 1) {
            result.recordPath(visualSweep(owner, current, partialTick), now, isFastMoving());
        }
        return result;
    }

    private void tickReturning(ServerPlayer owner, boolean homing) {
        Vec3 facing = worldDirection();
        Vec3 idle = heldAnchor(owner, 1);
        Vec3 local = direction();
        ReturningKnifeTrajectory profile = returningTrajectory();
        if (isInFlight() && homing && (profile == null || !profile.suppressesHoming())) {
            Vec3 world = PlayerMomsKnifeMotion.toWorld(local, owner.getYRot(), owner.getXRot());
            Vec3 point = tracking.targetPoint(owner, idle.add(world.scale(getFlightDistance(0))), world, level().getGameTime());
            Vec3 desired = point == null || point.distanceToSqr(idle) < 1.0E-12 ? baseDirection()
                    : PlayerMomsKnifeMotion.toLocal(point.subtract(idle), owner.getYRot(), owner.getXRot());
            setDirection(PlayerMomsKnifeMotion.steer(local, desired));
        } else {
            tracking.clear();
            setDirection(baseDirection());
        }

        Frame current = frame(owner);
        Frame from = previous;
        // A teleport or a chunk reload must not turn the previous blade into a cross-world damage trail.
        if (from == null || current.time() - from.time() > 1
                || current.idle().distanceToSqr(from.idle()) > 32 * 32) {
            attack.trail.clear();
            from = current;
            Vec3 safe = current.idle().add(PlayerMomsKnifeMotion.toWorld(current.localDirection(),
                    current.yaw(), current.pitch()).scale(getFlightDistance(0)));
            attack.trajectory.translateOrigin(safe.subtract(position()));
            setPosition(safe);
            clearTrajectoryPose();
            facing = PlayerMomsKnifeMotion.toWorld(current.localDirection(), current.yaw(), current.pitch());
        }
        double elapsed = current.time() - entityData.get(LAUNCHED_AT);
        double duration = getFlightDuration();
        List<PlayerMomsKnifeMotion.Blade> basePath = sweep(from, current);
        List<PlayerMomsKnifeMotion.Blade> path = isInFlight() && profile == null
                ? PlayerMomsKnifeTrajectory.apply(this, basePath, facing, elapsed >= duration) : basePath;
        if (isRemoved()) return;
        applyTrajectoryPose(owner, path, profile != null || path != basePath);
        setPos(path.get(path.size() - 1).anchor());
        previous = current;
        var motion = path == basePath && !currentSweep.isEmpty() ? currentSweep
                : PlayerMomsKnifeTrail.timed(path, from.time(), current.time());
        contacts(owner, path, isInFlight(), motion, from.center(), current.center());
        if (isRemoved()) return;

        if (isInFlight() && barrageOnReturn != null && elapsed >= duration / 2) {
            ExecutableEffectContext effect = barrageOnReturn;
            barrageOnReturn = null;
            effect.set(ContextKeys.AMPLIFIER, (double) MomsKnifeBarrage.countForWaitTicks(
                    level().getGameTime() - entityData.get(LAUNCHED_AT)));
            ModExecutableEffects.MOMS_KNIFE_BARRAGE.get().apply(effect);
            if (isRemoved()) return;
        }
        if (isInFlight() && elapsed >= duration) {
            attack.trail.clear();
            entityData.set(FAST_MOVING, false);
            entityData.set(RANGE, 0.0F);
            entityData.set(RETURNING_TRAJECTORY, "");
            entityData.set(FLIGHT_DURATION, 0.0F);
            setDirection(baseDirection());
            clearTrajectoryPose();
            tracking.clear();
            attack.setVelocity(Vec3.ZERO);
            setDeltaMovement(Vec3.ZERO);
        }
    }

    private void tickOneWay(ServerPlayer owner, boolean homing) {
        Vec3 ownerCenter = owner.getEyePosition().add(0, -0.15 * owner.getBbHeight(), 0);
        if (previousOneWayOwnerCenter != null && ownerCenter.distanceToSqr(previousOneWayOwnerCenter) > 32 * 32) {
            attack.trail.clear();
        }
        previousOneWayOwnerCenter = ownerCenter;
        Vec3 from = position();
        Vec3 facing = worldDirection();
        Vec3 beforeDirection = direction();
        Vec3 nextDirection = beforeDirection;
        Vec3 target = null;
        if (homing) {
            target = tracking.targetPoint(owner, from, beforeDirection, level().getGameTime());
            if (target != null) nextDirection = PlayerMomsKnifeMotion.steer(beforeDirection, target.subtract(from));
        } else tracking.clear();
        if (target == null && isControllable()) {
            Vec3 controlPoint = owner.getEyePosition().add(PlayerMomsKnifeMotion.unit(owner.getLookAngle()).scale(64));
            nextDirection = PlayerMomsKnifeMotion.unit(PlayerMomsKnifeMotion.controlVelocity(from,
                    beforeDirection.scale(oneWaySpeed), controlPoint,
                    PlayerHelper.hasItem(ModPassiveItems.TRACTOR_BEAM.getId(), owner)));
        }
        double step = PlayerMomsKnifeMotion.oneWayStep(remainingDistance, oneWaySpeed);
        if (step <= 0) {
            discard();
            return;
        }
        Vec3 to = from.add(nextDirection.scale(step));
        setDirection(nextDirection);
        List<PlayerMomsKnifeMotion.Blade> basePath = sweepOneWay(from, beforeDirection, to, nextDirection);
        List<PlayerMomsKnifeMotion.Blade> path = PlayerMomsKnifeTrajectory.apply(this, basePath, facing, false);
        if (isRemoved()) return;
        path = PlayerMomsKnifeTrajectory.limitDistance(path, remainingDistance);
        applyTrajectoryPose(owner, path, path != basePath);
        MomKnifePose pose = PlayerMomsKnifeMotion.pose(worldDirection(), getYRot());
        setYRot(pose.yaw() - 180);
        setXRot(pose.pitch() + 90);
        setPos(path.get(path.size() - 1).anchor());
        double now = level().getGameTime();
        contacts(owner, path, true, PlayerMomsKnifeTrail.timed(path, now - 1, now), Vec3.ZERO, Vec3.ZERO);
        if (isRemoved()) return;
        remainingDistance = Math.max(0, remainingDistance - PlayerMomsKnifeTrajectory.distance(path));
        if (remainingDistance < 1e-9) discard();
    }

    private Frame frame(Player owner) {
        return new Frame(heldAnchor(owner, 1), owner.getYRot(), owner.getXRot(), direction(),
                level().getGameTime(), owner.getEyePosition().add(0, -0.15 * owner.getBbHeight(), 0));
    }

    private List<PlayerMomsKnifeMotion.Blade> sweep(Frame from, Frame to) {
        currentSweep = List.of();
        double turn = Math.max(Math.abs(Mth.wrapDegrees(to.yaw() - from.yaw())),
                Math.abs(to.pitch() - from.pitch()));
        turn = Math.max(turn, Math.toDegrees(Math.acos(Mth.clamp(from.localDirection().dot(to.localDirection()), -1, 1))));
        int steps = Math.max(1, (int) Math.ceil(turn / 5));
        ReturningKnifeTrajectory profile = returningTrajectory();
        if (profile != null) {
            double fromAge = from.time() - entityData.get(LAUNCHED_AT);
            double toAge = to.time() - entityData.get(LAUNCHED_AT);
            Vec3 fromHeld = PlayerMomsKnifeMotion.offsetToLocal(from.idle().subtract(from.center()), from.yaw(), from.pitch());
            Vec3 toHeld = PlayerMomsKnifeMotion.offsetToLocal(to.idle().subtract(to.center()), to.yaw(), to.pitch());
            currentSweep = PlayerMomsKnifeTrajectory.returningSamples(profile, fromAge, toAge, getFlightDuration(), steps,
                    entityData.get(LAUNCHED_AT), fraction -> {
                var local = profile.sampleLocal(fromHeld.lerp(toHeld, fraction), baseDirection(), entityData.get(RANGE),
                        getFlightDuration(), fromAge + (toAge - fromAge) * fraction);
                return transformBlade(local, from.center().lerp(to.center(), fraction),
                        Mth.rotLerp((float) fraction, from.yaw(), to.yaw()),
                        Mth.lerp((float) fraction, from.pitch(), to.pitch()));
            });
            return currentSweep.stream().map(PlayerMomsKnifeTrail.Sample::blade).toList();
        }
        List<Double> fractions = new ArrayList<>();
        for (int i = 0; i <= steps; i++) fractions.add((double) i / steps);
        double duration = PlayerMomsKnifeMotion.duration(entityData.get(RANGE), entityData.get(SPEED));
        // Include the exact apex even when a short throw completes entirely between two server ticks.
        double apex = entityData.get(LAUNCHED_AT) + duration / 2;
        if (isInFlight() && to.time() > from.time() && apex > from.time() && apex < to.time()) {
            fractions.add((apex - from.time()) / (to.time() - from.time()));
            fractions.sort(Double::compare);
        }
        List<PlayerMomsKnifeMotion.Blade> path = new ArrayList<>(fractions.size());
        List<PlayerMomsKnifeTrail.Sample> timed = new ArrayList<>(fractions.size());
        for (double fraction : fractions) {
            double time = Mth.lerp(fraction, from.time(), to.time());
            double distance = PlayerMomsKnifeMotion.distance(entityData.get(RANGE), entityData.get(SPEED),
                    time - entityData.get(LAUNCHED_AT));
            Vec3 local = PlayerMomsKnifeMotion.interpolateDirection(from.localDirection(), to.localDirection(), fraction);
            Vec3 direction = PlayerMomsKnifeMotion.toWorld(local,
                    Mth.rotLerp((float) fraction, from.yaw(), to.yaw()),
                    Mth.lerp((float) fraction, from.pitch(), to.pitch()));
            Vec3 anchor = from.idle().lerp(to.idle(), fraction).add(direction.scale(distance));
            var blade = PlayerMomsKnifeMotion.blade(anchor, direction);
            path.add(blade);
            timed.add(new PlayerMomsKnifeTrail.Sample(time, blade));
        }
        currentSweep = timed;
        return path;
    }

    private static List<PlayerMomsKnifeMotion.Blade> sweepOneWay(Vec3 from, Vec3 fromDirection, Vec3 to, Vec3 toDirection) {
        double angle = Math.toDegrees(Math.acos(Mth.clamp(fromDirection.dot(toDirection), -1, 1)));
        int steps = Math.max(1, (int) Math.ceil(angle / 5));
        List<PlayerMomsKnifeMotion.Blade> path = new ArrayList<>(steps + 1);
        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / steps;
            path.add(PlayerMomsKnifeMotion.blade(from.lerp(to, fraction),
                    PlayerMomsKnifeMotion.interpolateDirection(fromDirection, toDirection, fraction)));
        }
        return path;
    }

    private void contacts(ServerPlayer owner, List<PlayerMomsKnifeMotion.Blade> path, boolean blocks,
                          List<PlayerMomsKnifeTrail.Sample> motion, Vec3 fromOwner, Vec3 toOwner) {
        attack.advance(path, level().getGameTime());
        boolean fast = isInFlight() && attack.trail.recordMotion(motion, fromOwner, toOwner, level().getGameTime());
        if (!isInFlight()) attack.trail.trim(level().getGameTime(), PlayerMomsKnifeTrail.LIFETIME);
        entityData.set(FAST_MOVING, fast);
        setDeltaMovement(attack.velocity);
        if (blocks) {
            for (BlockHitResult hit : PlayerMomsKnifeBlockContacts.find(owner.serverLevel(), path, getHitBlockPositions())) {
                if (isRemoved() || owner.level() != level() || !MomsKnifeAttack.isSelectedAndReady(owner)) return;
                if (!markBlockHit(hit.getBlockPos())) continue;
                setLastBlockHit(hit);
                MinecraftForge.EVENT_BUS.post(new IsaacAttackHitBlockEvent(this, owner, getTypeId(), getTriggers(), hit));
            }
        }
        if (!isRemoved() && owner.level() == level() && MomsKnifeAttack.isSelectedAndReady(owner)) {
            damage(owner);
        }
    }

    private void damage(ServerPlayer owner) {
        if (lastDamageTick == level().getGameTime()) return;
        lastDamageTick = level().getGameTime();
        AABB bounds = attack.contactPath.bounds;
        AABB historyBounds = attack.trail.bounds();
        if (historyBounds != null) bounds = bounds.minmax(historyBounds);
        float damage = getDamage();
        if (!Float.isFinite(damage) || damage <= 0) return;
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, bounds,
                target -> target.isAlive() && !target.isRemoved() && !target.isSpectator()
                        && !EntityHelper.isFriendly(target, owner))) {
            if (attack.contactPath.contacts(target.getBoundingBox()) || attack.trail.contacts(target.getBoundingBox())) {
                if (target.hurt(ModDamageSources.momsKnife(level(), this, owner), damage)) getDamagedEntities().add(target.getUUID());
                if (isRemoved()) return;
            }
        }
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        Player owner = getOwner();
        tracking.clear();
        attack.trail.clear();
        entityData.set(FAST_MOVING, false);
        barrageOnReturn = null;
        if (!level().isClientSide && owner != null && getMode() == Mode.RETURNING) {
            owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability ->
                    ability.getRuntimeEntities().unregister(getSourceId(), getHeldSlot(), getUUID()));
        }
        super.remove(reason);
    }

    @Override
    public SplitSequence getSplitSequence() { return attack.splits; }

    @Override
    public AttackContext getAttackContext() { return attack.context; }

    @Override
    public ResourceLocation getTypeId() { return ModAttackTypes.MOMS_KNIFE.getId(); }

    @Override
    public ResourceLocation getRootTypeId() { return getTypeId(); }

    @Override
    public SplitTriggerCounts getSplitTriggerCounts() { return attack.splitCounts; }

    @Override
    public void recordSplitTrigger(SplitTriggerType type) {}

    @Override
    public float getDamage() {
        Player owner = getOwner();
        return owner == null ? 0 : (float) (2 * owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    @Override
    public Vec3 getVelocity() { return attack.velocity; }

    @Override
    public Vec3 getAcceleration() { return attack.acceleration; }

    @Override
    public double getTraveled() { return attack.traveled; }

    @Override
    public double getRange() { return attack.range; }

    @Override
    public Vec3 getPosition() { return position(); }

    @Override
    public Object getShooter() { return attack.context == null ? getOwner() : attack.context.getShooter(); }

    @Override
    public boolean noGravity() { return true; }

    @Override
    public boolean isHoming() {
        Player owner = getOwner();
        return owner != null && owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getHoming() > 0).orElse(false);
    }

    @Override
    public boolean isSpectral() { return true; }

    @Override
    public boolean isControllable() {
        Player owner = getOwner();
        return getMode() == Mode.ONE_WAY && owner != null && owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getControllable() > 0).orElse(false);
    }

    @Override
    public boolean isPiercing() { return true; }

    @Override
    public ResourceLocation getColorId() { return attack.context == null ? null : attack.context.getColorRl(); }

    @Override
    public TrajectoryRuntime getTrajectoryRuntime() { return attack.trajectory; }

    @Override
    public double getRemainingTrajectoryDistance() {
        return getMode() == Mode.RETURNING ? Double.POSITIVE_INFINITY : remainingDistance;
    }

    @Override
    public int getTrajectoryAge() { return attack.age; }

    @Override
    public CompositeTrigger getTriggers() { return attack.triggers; }

    @Override
    public BlockHitResult getLastBlockHit() { return attack.collisions.lastBlockHit(); }

    @Override
    public void setLastBlockHit(BlockHitResult hit) { attack.collisions.lastBlockHit(hit); }

    @Override
    public Set<BlockPos> getHitBlockPositions() { return attack.collisions.hitBlockPositions(); }

    @Override
    public boolean markBlockHit(BlockPos position) { return attack.collisions.markBlockHit(position); }

    @Override
    public DamagedEntities getDamagedEntities() { return attack.collisions.damagedEntities(); }

    @Override
    public Level getBulletLevel() { return level(); }

    private Vec3 worldDirection() {
        Player owner = getOwner();
        return getMode() == Mode.RETURNING && owner != null
                ? PlayerMomsKnifeMotion.toWorld(posedDirection(), owner.getYRot(), owner.getXRot()) : posedDirection();
    }

    @Override
    public Vec3 getCenter() {
        return position().add(worldDirection().scale((MomKnifePose.BLADE_BASE_DISTANCE + MomKnifePose.BLADE_TIP_DISTANCE) / 2));
    }

    @Override
    public void setCenter(Vec3 center) { setPosition(position().add(center.subtract(getCenter()))); }

    @Override
    public void setPosition(Vec3 position) {
        setPos(position);
        attack.trajectory.kinematics().position(position);
    }

    @Override
    public void setVelocity(Vec3 velocity) {
        if (!Double.isFinite(velocity.lengthSqr())) return;
        attack.setVelocity(velocity);
        setDeltaMovement(velocity);
        if (velocity.lengthSqr() < 1.0E-12) return;
        Player owner = getOwner();
        setDirection(getMode() == Mode.RETURNING && owner != null
                ? PlayerMomsKnifeMotion.toLocal(velocity, owner.getYRot(), owner.getXRot()) : velocity);
        if (getMode() == Mode.ONE_WAY) oneWaySpeed = velocity.length();
    }

    @Override
    public float getCollisionHeight() {
        return (float) (Math.abs(worldDirection().y) * (MomKnifePose.BLADE_TIP_DISTANCE - MomKnifePose.BLADE_BASE_DISTANCE)
                + 2 * PlayerMomsKnifeMotion.ENTITY_HIT_RADIUS);
    }

    @Override
    public float getCollisionWidth() {
        Vec3 direction = worldDirection();
        return (float) (Math.max(Math.abs(direction.x), Math.abs(direction.z))
                * (MomKnifePose.BLADE_TIP_DISTANCE - MomKnifePose.BLADE_BASE_DISTANCE) + 2 * PlayerMomsKnifeMotion.ENTITY_HIT_RADIUS);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        new PlayerMomsKnifeSpawnData(entityData.get(MODE), entityData.get(OWNER), direction(),
                entityData.get(RANGE), entityData.get(SPEED), entityData.get(LAUNCHED_AT), getYRot(), getXRot(),
                getHeldSlot(), localOffset(), baseDirection(), getSourceId(), trajectoryOffset(),
                vector(entityData.get(TRAJECTORY_DIRECTION)), entityData.get(RETURNING_TRAJECTORY),
                entityData.get(FLIGHT_DURATION), attack.trail.samples(), isFastMoving()).write(buffer);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        var data = PlayerMomsKnifeSpawnData.read(buffer);
        entityData.set(MODE, data.mode());
        entityData.set(OWNER, data.owner());
        entityData.set(HELD_SLOT, data.heldSlot());
        entityData.set(HELD_SOURCE, data.heldSource().toString());
        entityData.set(LOCAL_OFFSET, new Vector3f((float) data.localOffset().x, (float) data.localOffset().y, (float) data.localOffset().z));
        entityData.set(BASE_DIRECTION, new Vector3f((float) data.baseDirection().x, (float) data.baseDirection().y, (float) data.baseDirection().z));
        setDirection(data.direction());
        entityData.set(RANGE, data.range());
        entityData.set(SPEED, data.speed());
        entityData.set(LAUNCHED_AT, data.launchedAt());
        entityData.set(RETURNING_TRAJECTORY, data.returningTrajectory());
        entityData.set(FLIGHT_DURATION, data.flightDuration());
        entityData.set(FAST_MOVING, data.fastMoving());
        entityData.set(TRAJECTORY_OFFSET, new Vector3f((float) data.trajectoryOffset().x,
                (float) data.trajectoryOffset().y, (float) data.trajectoryOffset().z));
        entityData.set(TRAJECTORY_DIRECTION, new Vector3f((float) data.trajectoryDirection().x,
                (float) data.trajectoryDirection().y, (float) data.trajectoryDirection().z));
        renderFrom = renderTo = posedDirection();
        renderOffsetFrom = renderOffsetTo = trajectoryOffset();
        offsetUpdatedAt = level().getGameTime();
        directionUpdatedAt = level().getGameTime();
        interpolationTarget = null;
        interpolationSteps = 0;
        setYRot(data.yaw());
        setXRot(data.pitch());
        Vec3 initialVelocity = getMode() == Mode.ONE_WAY ? direction().scale(data.speed()) : Vec3.ZERO;
        attack = new PlayerMomsKnifeAttackState(null, position(), direction(), data.range(), initialVelocity);
        attack.trail.append(data.trail(), level().getGameTime(), PlayerMomsKnifeTrail.LIFETIME);
        setDeltaMovement(initialVelocity);
        setOldPosAndRot();
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {}

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public boolean isAttackable() { return false; }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        AABB bounds;
        if (getMode() == Mode.ONE_WAY) {
            bounds = getBoundingBox().inflate(MomKnifePose.BLADE_TIP_DISTANCE);
            if (interpolationTarget != null) bounds = bounds.minmax(bounds.move(interpolationTarget.subtract(position())));
        } else {
            Player owner = getOwner();
            if (owner == null) bounds = getBoundingBox().inflate(1);
            else {
                double reach = Math.max(0.6, owner.getBbWidth()) + entityData.get(RANGE)
                        + localOffset().length() + MomKnifePose.BLADE_TIP_DISTANCE + MomKnifePose.BLADE_RADIUS;
                AABB ownerBounds = owner.getBoundingBox();
                AABB movingOwner = ownerBounds.minmax(ownerBounds.move(owner.getPosition(0).subtract(owner.position())));
                bounds = getBoundingBox().minmax(movingOwner.inflate(reach));
            }
        }
        AABB historyBounds = attack.trail.bounds();
        return historyBounds == null ? bounds : bounds.minmax(historyBounds);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double reach = getType().clientTrackingRange() * 16 * getViewScale();
        return distance < reach * reach;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (teleport || !level().isClientSide || getMode() == Mode.RETURNING) {
            super.lerpTo(x, y, z, yaw, pitch, steps, teleport);
            interpolationTarget = null;
            if (teleport) {
                attack.trail.clear();
                previous = null;
                previousVisualBlade = null;
                setOldPosAndRot();
            }
        } else {
            interpolationTarget = new Vec3(x, y, z);
            interpolationSteps = Math.max(1, steps);
            setYRot(yaw);
            setXRot(pitch);
        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private record Frame(Vec3 idle, float yaw, float pitch, Vec3 localDirection, double time, Vec3 center) {}

    private static Vec3 vector(Vector3f value) { return new Vec3(value.x, value.y, value.z); }
}
