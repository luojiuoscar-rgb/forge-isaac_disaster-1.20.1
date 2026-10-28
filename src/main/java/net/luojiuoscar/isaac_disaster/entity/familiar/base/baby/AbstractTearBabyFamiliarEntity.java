package net.luojiuoscar.isaac_disaster.entity.familiar.base.baby;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
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
import net.minecraftforge.registries.RegistryObject;

/** Ordinary tears and synchronized gaze; rig-dependent defaults can be overridden independently of the attack. */
public abstract class AbstractTearBabyFamiliarEntity extends AbstractBabyFamiliarEntity {
    public static final float BASE_SCALE = 0.6F;
    public static final int ATTACK_DURATION_TICKS = 10;
    private static final EntityDataAccessor<Boolean> AIMING = SynchedEntityData.defineId(AbstractTearBabyFamiliarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> AIM_YAW = SynchedEntityData.defineId(AbstractTearBabyFamiliarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH = SynchedEntityData.defineId(AbstractTearBabyFamiliarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> LAST_SHOT = SynchedEntityData.defineId(AbstractTearBabyFamiliarEntity.class, EntityDataSerializers.LONG);
    private Vec3 attackDirection = Vec3.ZERO;
    private float aimYaw, previousAimYaw, aimPitch, previousAimPitch;
    private boolean aimInitialized;

    protected AbstractTearBabyFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected abstract double tearDamageCoefficient();

    protected abstract RegistryObject<BulletColor> tearColor();

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
        entityData.define(LAST_SHOT, -1L);
    }

    @Override
    protected boolean performAttack(ServerPlayer owner, Vec3 direction) {
        double configuredRange = GameplayConfig.DEFAULT_BULLET_RANGE.get();
        double range = Double.isFinite(configuredRange) ? Math.max(12, 1.5 * configuredRange) : 12;
        double damage = tearDamageCoefficient() * Math.max(0, StatManager.DAMAGE.getBonus()) * Math.max(0, 1 + getEnhancement());
        var color = tearColor();
        BulletState state = BulletState.builder()
                .owner(owner).shooter(this)
                .position(attackOrigin()).velocity(direction).baseSpeed(1)
                .range(range)
                // Age expires before movement, so N movement ticks need lifetime N+1.
                .lifetime((int) Math.min(Integer.MAX_VALUE, Math.ceil(range) + 1))
                .damage(Double.isFinite(damage) ? damage : 0)
                .colorId(color.getId()).color(color.get().color()).alpha(color.get().alpha())
                .renderScale(1).collisionWidth(0.2).collisionHeight(0.2)
                .noGravity(true).build();
        return BulletRuntime.INSTANCE.spawn(owner.serverLevel(), state) != null;
    }

    @Override
    protected void tickChainClient() { tickLook(); }

    @Override
    protected void aimAtDirection(Vec3 direction) {
        super.aimAtDirection(direction);
        attackDirection = direction;
        aim(direction);
    }

    @Override
    protected boolean continueAttackAim(long now) {
        long shot = entityData.get(LAST_SHOT);
        if (shot < 0 || now < shot || now - shot >= Math.max(0, getAttackDurationTicks())) return false;
        aimAtDirection(attackDirection);
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
        return shot < 0 || elapsed < 0 || elapsed >= Math.max(0, getAttackDurationTicks()) ? -1 : (float) elapsed;
    }

    /** Default neutral head center for Bobby and Maggy; other rigs override this world-space origin. */
    @Override
    protected Vec3 attackOrigin() {
        // Neutral center: rig(24) + body(-6) + head(-3) + geometry center(-6) = 9 pixels.
        return position().add(0, (14.5 - 9) / 16 * getBaseModelScale() * getVisualScale(), 0);
    }

    @Override
    protected void onAttackSucceeded(ServerPlayer owner, Vec3 axis) {
        attackDirection = axis;
        aim(axis);
        entityData.set(LAST_SHOT, level().getGameTime());
        playAttackSound();
    }

    protected void playAttackSound() {
        level().playSound(null, getX(), getY(), getZ(), ModSounds.TEAR_BULLET_SHOT.get(),
                SoundSource.PLAYERS, 0.6F, 1F);
    }

    @Override
    protected void resetBabyAction() {
        attackDirection = Vec3.ZERO;
        aimInitialized = false;
        entityData.set(AIMING, false);
        entityData.set(LAST_SHOT, -1L);
    }

    @Override
    protected void onClientPositionReset() { aimInitialized = false; }
}
