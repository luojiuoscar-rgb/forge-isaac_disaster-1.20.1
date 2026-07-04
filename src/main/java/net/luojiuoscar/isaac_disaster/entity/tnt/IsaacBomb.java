package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeExecutableEffect;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IsaacBomb extends PrimedTnt {
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(IsaacBomb.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Boolean> IS_ORIGINAL =
            SynchedEntityData.defineId(IsaacBomb.class, EntityDataSerializers.BOOLEAN);

    private BombData profile = BombData.DEFAULT;
    private int power;
    private float centerDamage = BombData.DEFAULT.centerDamage();
    private float damageRadius = BombData.DEFAULT.damageRadius();
    private float blockPower = BombData.DEFAULT.blockPower();
    private float blockResistanceMultiplier = BombData.DEFAULT.blockResistanceMultiplier();
    private final CompositeExecutableEffect cachedEffect = new CompositeExecutableEffect();

    /**
     * Creates an Isaac bomb with explicit legacy power and render scale values.
     *
     * <p>New call sites should prefer the profile constructor or {@link #applyProfile(BombData)};
     * this constructor is kept for older effects that still think in raw power and scale.</p>
     */
    public IsaacBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale) {
        this(pLevel, pX, pY, pZ, pOwner, power, scale, true);
    }

    /**
     * Creates an Isaac bomb with explicit legacy power, render scale, and origin marker values.
     */
    public IsaacBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale, boolean isOriginal) {
        super(pLevel, pX, pY, pZ, pOwner);
        applyProfile(BombData.fromPower(power));
        this.power = power;
        this.blockPower = power;
        this.entityData.set(SCALE, scale);
        this.entityData.set(IS_ORIGINAL, isOriginal);
    }

    /**
     * Creates an Isaac bomb from a built-in profile.
     */
    public IsaacBomb(Level level, double x, double y, double z, @Nullable LivingEntity owner, BombData profile) {
        super(level, x, y, z, owner);
        applyProfile(profile);
    }

    public boolean isOriginal(){
        return this.entityData.get(IS_ORIGINAL);
    }

    public void setOriginal(boolean isOriginal) {
        this.entityData.set(IS_ORIGINAL, isOriginal);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SCALE, BombData.NORMAL.size());
        this.entityData.define(IS_ORIGINAL, true);
    }

    public float getScale() {
        return this.entityData.get(SCALE);
    }

    public void setScale(float scale) {
        this.entityData.set(SCALE, scale);
    }


    public int getPower() {
        return power;
    }

    public IsaacBomb(EntityType<? extends IsaacBomb> type, Level level) {
        super(type, level);
        applyProfile(BombData.DEFAULT);
    }



    @Override
    protected void explode() {
        IsaacBombExplosion.explode(this);
    }

    /**
     * Sets the block destruction radius and the default block-breaking power together.
     *
     * <p>This preserves the old IsaacBomb meaning of power. Use {@link #setBlockPower(float)} after
     * this method when an effect should change block radius and block destruction independently.</p>
     */
    public void setPower(int power) {
        this.power = power;
        this.blockPower = power;
    }

    /**
     * Applies a built-in bomb profile to this entity.
     *
     * <p>This updates the block destruction radius, rendering scale, default fuse, center damage,
     * damage radius, and
     * block-breaking parameters together. Callers may still tweak individual values afterwards.</p>
     */
    public void applyProfile(BombData profile) {
        this.profile = profile == null ? BombData.DEFAULT : profile;
        this.power = this.profile.power();
        this.centerDamage = this.profile.centerDamage();
        this.damageRadius = this.profile.damageRadius();
        this.blockPower = this.profile.blockPower();
        this.blockResistanceMultiplier = this.profile.blockResistanceMultiplier();
        this.entityData.set(SCALE, this.profile.size());
        this.setFuse(this.profile.fuseTicks());
    }

    /**
     * Returns a snapshot profile for the bomb's current explosion parameters.
     *
     * <p>The built-in profile enum still identifies the bomb category, while mutable fields such as
     * center damage, damage radius, and block power allow item effects to alter one axis without
     * redefining the whole profile.</p>
     */
    public BombData getProfile() {
        return profile;
    }

    public float getCenterDamage() {
        return centerDamage;
    }

    public void setCenterDamage(float centerDamage) {
        this.centerDamage = centerDamage;
    }

    public float getDamageRadius() {
        return damageRadius;
    }

    public void setDamageRadius(float damageRadius) {
        this.damageRadius = damageRadius;
    }

    public float getBlockPower() {
        return blockPower;
    }

    public void setBlockPower(float blockPower) {
        this.blockPower = blockPower;
    }

    public float getBlockResistanceMultiplier() {
        return blockResistanceMultiplier;
    }

    public void setBlockResistanceMultiplier(float blockResistanceMultiplier) {
        this.blockResistanceMultiplier = blockResistanceMultiplier;
    }

    public void setOwner(LivingEntity entity) {
        this.owner = entity;
    }

    @Override
    public void tick() {
        super.tick();

        // 波比炸弹的追踪效果
        if(this.getOwner() instanceof ServerPlayer player
            && PlayerHelper.hasItem(ItemId.BOBBY_BOMB.getId(), player)){

            Vec3 forwardPos = this.position().add(this.getDeltaMovement().normalize().scale(3.0));

            LivingEntity target = EntityHelper.findNearestTrackingTarget(
                    level(),
                    getOwner(),
                    forwardPos, // 用前方位置作为中心
                    6,
                    null
            );

            if (target != null) {
                // steerTowards(target, 0.1);
                applyAttraction(target);
            }
        }


        // 检查速度是否大于阈值（体型过小的tnt也不会触发）
        double speed = this.getDeltaMovement().length();
        if (speed < 0.5 || getScale() < 0.5f) return;

        // 获取 TNT 的包围盒
        AABB tntBox = this.getBoundingBox();

        // 遍历附近的生物
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, tntBox.inflate(0.3), e -> e != this.getOwner());
        for (LivingEntity entity : entities) {
            // 命中目标（排除主人）
            if(entity instanceof Player player && player == this.getOwner()){
                return;
            }
            onHitEntity(entity);
            break; // 只处理第一个命中的生物
        }
    }


    // 朝向目标转向（保持原始速度大小不变）
    private void steerTowards(LivingEntity target, double steerStrength) {
        Vec3 currentVel = this.getDeltaMovement();
        if (currentVel.lengthSqr() < 0.0001) return; // 静止不追踪

        Vec3 directionToTarget = target.position().add(0, target.getBbHeight() * 0.5, 0) // 瞄准中心点
                .subtract(this.position())
                .normalize();

        // 使用线性插值进行平滑转向
        Vec3 newVel = currentVel.normalize().lerp(directionToTarget, steerStrength).normalize().scale(currentVel.length());

        this.setDeltaMovement(newVel);
    }

    private void applyAttraction(LivingEntity target) {
        Vec3 pos = this.position();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);

        Vec3 toTarget = targetPos.subtract(pos);
        double dist = toTarget.length();
        if (dist < 0.001) return;

        Vec3 dir = toTarget.normalize();

        double strength = 0.25;
        double influence = Math.min(1.0, 5.0 / dist); // 近距离更强

        Vec3 velocity = this.getDeltaMovement();

        Vec3 corrected = velocity.lerp(dir.scale(velocity.length()), strength * influence);

        this.setDeltaMovement(corrected);
    }

    /**
     * 击中生物给予眩晕效果
     */
    private void onHitEntity(LivingEntity entity) {
        if (!this.level().isClientSide()) {

            this.setDeltaMovement(Vec3.ZERO);
            entity.addEffect(new MobEffectInstance(ModEffects.DIZZINESS.get(), 20, 255));
        }
    }

    public CompositeExecutableEffect getCachedEffect() {
        return cachedEffect;
    }

    // 不保存到世界
    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
