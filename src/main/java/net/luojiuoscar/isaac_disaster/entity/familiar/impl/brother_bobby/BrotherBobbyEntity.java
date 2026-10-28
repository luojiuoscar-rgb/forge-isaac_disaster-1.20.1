package net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractBabyFamiliarEntity;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BrotherBobbyEntity extends AbstractBabyFamiliarEntity {
    public static final float BASE_SCALE = 0.6F;
    private static final EntityDataAccessor<Boolean> AIMING = SynchedEntityData.defineId(BrotherBobbyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> AIM_YAW = SynchedEntityData.defineId(BrotherBobbyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH = SynchedEntityData.defineId(BrotherBobbyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> LAST_SHOT = SynchedEntityData.defineId(BrotherBobbyEntity.class, EntityDataSerializers.LONG);
    private final BrotherBobbyCombat combat = new BrotherBobbyCombat();
    private Vec3 manualDirection = Vec3.ZERO;
    private float aimYaw, previousAimYaw, aimPitch, previousAimPitch;
    private boolean aimInitialized;

    public BrotherBobbyEntity(EntityType<? extends BrotherBobbyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(AIMING, false);
        entityData.define(AIM_YAW, 0F);
        entityData.define(AIM_PITCH, 0F);
        entityData.define(LAST_SHOT, -1L);
    }

    @Override
    protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
        return combat.fire(owner, this, direction, level().getGameTime());
    }

    @Override
    protected int attackIntervalTicks() { return BrotherBobbyCombat.SHOT_INTERVAL; }

    @Override
    protected void tickChainClient() { tickLook(); }

    @Override
    protected void aimAtDirection(Vec3 direction) {
        super.aimAtDirection(direction);
        manualDirection = direction;
        aim(direction);
    }

    @Override
    protected boolean continueAttackAim(long now) {
        long shot = entityData.get(LAST_SHOT);
        if (shot < 0 || now < shot || now - shot >= BrotherBobbyCombat.SHOT_INTERVAL) return false;
        aimAtDirection(manualDirection);
        return true;
    }

    @Override
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
        long shot = entityData.get(LAST_SHOT);
        double elapsed = level().getGameTime() - shot + partialTick;
        return shot < 0 || elapsed < 0 || elapsed >= 10 ? -1 : (float) elapsed;
    }

    @Override
    protected Vec3 attackOrigin() {
        // Neutral center: rig(24) + body(-6) + head(-3) + geometry center(-6) = 9 pixels.
        return position().add(0, (14.5 - 9) / 16 * BASE_SCALE * getVisualScale(), 0);
    }

    Vec3 muzzle(Vec3 axis) {
        return attackOrigin().add(axis.scale(5.5 / 16 * BASE_SCALE * getVisualScale()));
    }

    void recordShot(Vec3 axis, long now) {
        manualDirection = axis;
        aim(axis);
        entityData.set(LAST_SHOT, now);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.TEAR_BULLET_SHOT.get(),
                SoundSource.PLAYERS, 0.6F, 1F);
    }

    @Override
    protected void resetBabyAction() {
        manualDirection = Vec3.ZERO;
        aimInitialized = false;
        entityData.set(AIMING, false);
        entityData.set(LAST_SHOT, -1L);
    }

    @Override
    protected void onClientPositionReset() { aimInitialized = false; }
}
