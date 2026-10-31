package net.luojiuoscar.isaac_disaster.entity.familiar.base.baby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.chain.AbstractChainFamiliarEntity;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Chain-following babies share attack scheduling while defining their own triggers, attacks and presentation. */
public abstract class AbstractBabyFamiliarEntity extends AbstractChainFamiliarEntity {
    public static final float BASE_SCALE = 0.6F;
    public static final int ATTACK_DURATION_TICKS = 10;
    private static final EntityDataAccessor<Boolean> AIMING = SynchedEntityData.defineId(AbstractBabyFamiliarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> AIM_YAW = SynchedEntityData.defineId(AbstractBabyFamiliarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH = SynchedEntityData.defineId(AbstractBabyFamiliarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> LAST_ACTION = SynchedEntityData.defineId(AbstractBabyFamiliarEntity.class, EntityDataSerializers.LONG);
    private Vec3 attackDirection = Vec3.ZERO;
    private float aimYaw, previousAimYaw, aimPitch, previousAimPitch;
    private boolean aimInitialized;


    /** Scale of the unmodified model, before familiar visual modifiers. */
    public float getBaseModelScale() { return BASE_SCALE; }

    /** Action duration is independent of the successful-attack cooldown. */
    public int getAttackDurationTicks() { return ATTACK_DURATION_TICKS; }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(AIMING, false);
        entityData.define(AIM_YAW, 0F);
        entityData.define(AIM_PITCH, 0F);
        entityData.define(LAST_ACTION, -1L);
    }

    private long nextAttackTick = Long.MIN_VALUE;
    private long lastAttackTick = Long.MIN_VALUE;
    private long windupStartTick = Long.MIN_VALUE;

    protected AbstractBabyFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    /** Return true only after the concrete attack has actually been created. */
    protected abstract boolean performAttack(ServerPlayer owner, Vec3 direction);

    protected abstract int attackIntervalTicks();

    /** Held input starts a complete action; direct attack requests remain immediate. */
    protected int heldAttackWindupTicks() { return 0; }

    /** A one-shot request aimed at a world position, sharing the same cadence as held-player fire. */
    public final boolean attackTowards(Vec3 position) {
        return attackInDirection(position.subtract(attackOrigin()));
    }

    /** A one-shot request; direction magnitude does not change the concrete attack's speed. */
    public final boolean attackInDirection(Vec3 direction) {
        double lengthSqr = direction.lengthSqr();
        if (level().isClientSide || isRecallPending() || !Double.isFinite(lengthSqr) || lengthSqr < 1e-12
                || !(getOwner() instanceof ServerPlayer owner) || owner.isSpectator() || !isValidFamiliar()) return false;
        Vec3 axis = direction.normalize();
        aimAtDirection(axis);
        long now = level().getGameTime();
        if (now == lastAttackTick || now < nextAttackTick || !performAttack(owner, axis)) return false;
        lastAttackTick = now;
        nextAttackTick = now + Math.max(0, attackIntervalTicks());
        windupStartTick = Long.MIN_VALUE;
        onAttackSucceeded(owner, axis);
        return true;
    }

    @Override
    protected final void tickChainServer(ServerPlayer owner, Vec3 movement) {
        Vec3 requestedDirection = findAttackDirection(owner);
        if (windupStartTick != Long.MIN_VALUE || requestedDirection != null) {
            tickTriggeredAttack(requestedDirection);
        } else {
            if (!continueAttackAim(level().getGameTime())) {
                stopAttackAim();
                super.tickChainServer(owner, movement);
            }
        }
    }

    /** Supplies a new action's world-space aim; an already triggered action completes independently. */
    protected @Nullable Vec3 findAttackDirection(ServerPlayer owner) {
        return !owner.isSpectator() && PlayerHelper.isHoldingIsaacHead(owner)
                && owner.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.isHoldingRightClick()).orElse(false) ? owner.getLookAngle() : null;
    }

    private void tickTriggeredAttack(@Nullable Vec3 requestedDirection) {
        int windup = Math.max(0, heldAttackWindupTicks());
        if (windup == 0) {
            if (requestedDirection != null) attackInDirection(requestedDirection);
            return;
        }
        long now = level().getGameTime();
        if (windupStartTick != Long.MIN_VALUE) {
            aimAtDirection(attackDirection);
            if (now - windupStartTick >= windup && now >= nextAttackTick) {
                long started = windupStartTick;
                boolean fired = attackInDirection(attackDirection);
                windupStartTick = Long.MIN_VALUE;
                entityData.set(LAST_ACTION, fired ? started : -1L);
            }
            return;
        }
        // Prepare during the final windup ticks of cooldown so successful shots retain their original cadence.
        if (now == lastAttackTick || (now < nextAttackTick && nextAttackTick - now > windup)) {
            if (!continueAttackAim(now)) stopAttackAim();
            return;
        }
        if (requestedDirection == null || !Double.isFinite(requestedDirection.lengthSqr())
                || requestedDirection.lengthSqr() < 1e-12) return;
        aimAtDirection(requestedDirection.normalize());
        windupStartTick = now;
        entityData.set(LAST_ACTION, now);
    }

    @Override
    protected void tickChainClient() { tickLook(); }

    protected void aimAtDirection(Vec3 direction) {
        if (direction.horizontalDistanceSqr() > 1e-8)
            setYRot(getYRot() + Mth.clamp(Mth.wrapDegrees(yaw(direction) - getYRot()), -12, 12));
        attackDirection = direction;
        aim(direction);
    }

    protected boolean continueAttackAim(long now) {
        long shot = entityData.get(LAST_ACTION);
        if (shot < 0 || now < shot || now - shot >= Math.max(0, getAttackDurationTicks())) return false;
        aimAtDirection(attackDirection);
        return true;
    }

    protected void stopAttackAim() { entityData.set(AIMING, false); }

    private void aim(Vec3 direction) {
        if (direction.lengthSqr() < 1.0e-12) return;
        entityData.set(AIMING, true);
        if (direction.horizontalDistanceSqr() > 1.0e-8) entityData.set(AIM_YAW, yaw(direction));
        entityData.set(AIM_PITCH, (float) (-Mth.atan2(direction.y, direction.horizontalDistance()) * Mth.RAD_TO_DEG));
    }

    private void tickLook() {
        float targetYaw = entityData.get(AIMING) ? entityData.get(AIM_YAW) : getRenderYaw(1);
        float targetPitch = entityData.get(AIMING) ? entityData.get(AIM_PITCH) : 0;
        if (!aimInitialized) {
            aimYaw = getRenderYaw(1);
            aimPitch = 0;
            aimInitialized = true;
        }
        previousAimYaw = aimYaw;
        previousAimPitch = aimPitch;
        aimYaw += Mth.clamp(Mth.wrapDegrees(targetYaw - aimYaw), -18, 18);
        aimPitch += Mth.clamp((targetPitch - aimPitch) * 0.45F, -12, 12);
    }

    public float getRenderHeadYaw(float partialTick) {
        float worldYaw = level().isClientSide ? Mth.rotLerp(partialTick, previousAimYaw, aimYaw) : entityData.get(AIM_YAW);
        return Mth.clamp(Mth.wrapDegrees(worldYaw - getRenderYaw(partialTick)), -60, 60);
    }

    public float getRenderHeadPitch(float partialTick) {
        return Mth.clamp(level().isClientSide ? Mth.lerp(partialTick, previousAimPitch, aimPitch)
                : entityData.get(AIM_PITCH), -75, 75);
    }

    /** World time lets a new tracking client resume the action phase without replaying old shots. */
    public float getAttackAge(float partialTick) {
        long shot = entityData.get(LAST_ACTION);
        double elapsed = level().getGameTime() - shot + partialTick;
        return shot < 0 || elapsed < 0 || elapsed >= Math.max(0, getAttackDurationTicks()) ? -1 : (float) elapsed;
    }

    /** Default neutral head center for Bobby and Maggy; other rigs override this world-space origin. */
    protected Vec3 attackOrigin() {
        // Neutral center: rig(24) + body(-6) + head(-3) + geometry center(-6) = 9 pixels.
        return position().add(0, (14.5 - 9) / 16 * getBaseModelScale() * getVisualScale(), 0);
    }

    protected void onAttackSucceeded(ServerPlayer owner, Vec3 axis) {
        attackDirection = axis;
        aim(axis);
        entityData.set(LAST_ACTION, level().getGameTime());
    }

    protected void resetBabyAction() {
        windupStartTick = Long.MIN_VALUE;
        attackDirection = Vec3.ZERO;
        aimInitialized = false;
        entityData.set(AIMING, false);
        entityData.set(LAST_ACTION, -1L);
    }

    @Override
    protected void onClientPositionReset() { aimInitialized = false; }

    @Override
    protected final void resetChainAction() {
        resetBabyAction();
        // Recalling never grants a ready attack or a second attack in the same tick.
    }
}
