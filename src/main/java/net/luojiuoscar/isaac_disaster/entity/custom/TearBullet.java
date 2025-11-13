package net.luojiuoscar.isaac_disaster.entity.custom;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletDiscardEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletTickEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.id.AttackTypeId;
import net.luojiuoscar.isaac_disaster.manager.id.BulletColorId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TearBullet extends Entity {

    // ======== 基础属性 ========
    private int lifeTick;
    private int totalLifeTick;
    private float damage;
    private UUID ownerUUID;
    private LivingEntity cachedOwner;
    private Double orbitAngle;

    // ======== 特性 ========
    public boolean isSpectral = false;
    public boolean isPiercing = false;
    public boolean isHoming = false;
    public boolean isControllable = false;

    public static final double HOMING_RANGE = 4.0;
    public static final double HOMING_SPEED = 1.0;
    public static final double HOMING_STEER = 0.6;

    public static final double CONTROL_RANGE = 64.0;
    public static final double CONTROL_STEER = 0.8;

    // ======== 状态 ========
    private final Set<UUID> damagedEntities = new HashSet<>();
    private final Set<Integer> hitEffectIds = new HashSet<>();

    // ======== 客户端同步属性 ========
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ALPHA =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.FLOAT);

    // ======== 构造函数 ========
    public TearBullet(EntityType<? extends TearBullet> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.isSpectral = false;
        this.isPiercing = false;
        this.isHoming = false;
        this.isControllable = false;
        this.damage = 1.0f;
        this.setColor(BulletColorId.BASE.getColor());
        this.setAlpha(1.0f);
        this.setScale(1.0f);
    }

    public TearBullet(Level level, LivingEntity shooter, int lifeTick, double bulletSpeed, float scale, float damage, float xRot, float yRot) {
        this(ModEntities.TEAR_BULLET.get(), level);

        this.ownerUUID = shooter.getUUID();
        this.cachedOwner = shooter;
        this.lifeTick = lifeTick;
        this.totalLifeTick = lifeTick;
        this.damage = damage;

        setScale(scale);

        moveTo(shooter.getX(), shooter.getEyeY(), shooter.getZ(), yRot, xRot);
        Vec3 look = Vec3.directionFromRotation(xRot, yRot);
        setDeltaMovement(look.scale(bulletSpeed));

        if (!level.isClientSide)
            MinecraftForge.EVENT_BUS.post(new TearBulletShootEvent(
                    getOwner(), AttackTypeId.BULLET.getId(), hitEffectIds, this));
    }


    // ======== 核心逻辑 ========
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            move(MoverType.SELF, getDeltaMovement());
            return;
        }

        MinecraftForge.EVENT_BUS.post(new TearBulletTickEvent(this));

        if (--lifeTick <= 0) {
            if (!MinecraftForge.EVENT_BUS.post(new TearBulletDiscardEvent(this))) discard();
            return;
        }

        Vec3 start = position();
        Vec3 motion = getDeltaMovement();
        Vec3 end = start.add(motion);

        boolean collided = handleBlockCollision(start, end);
        if (collided) return; // 撞到方块时取消移动

        handleEntityCollision(start, end, motion);

        if (tickCount % 4 == 0) handleSteering();

        move(MoverType.SELF, motion);
    }

    private boolean handleBlockCollision(Vec3 start, Vec3 end) {
        if (isSpectral) return false;

        BlockHitResult blockHit = level().clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)
        );

        if (blockHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level().getBlockState(pos);

        // 获取该方块的碰撞体积（VoxelShape）
        VoxelShape shape = state.getCollisionShape(level(), pos);

        if (shape.isEmpty() || shape.bounds().getSize() < 0.01) {
            return false;
        }

        if (!MinecraftForge.EVENT_BUS.post(new IsaacAttackHitBlockEvent(getOwner(), AttackTypeId.BULLET.getId(),
                hitEffectIds, blockHit))) {
            if (!MinecraftForge.EVENT_BUS.post(new TearBulletDiscardEvent(this))) {
                discard();
            }
        }

        return true;
    }

    private void handleSteering() {
        LivingEntity owner = getOwner();
        LivingEntity target = isHoming ? getTrackingTarget() : null;

        if (target == null && isControllable && owner != null) {
            Vec3 targetPos = owner.getEyePosition().add(Vec3.directionFromRotation(owner.getXRot(), owner.getYRot()).scale(CONTROL_RANGE));
            steerTowards(targetPos, CONTROL_STEER, false);
        }

        if (target != null) {
            steerTowards(target, HOMING_STEER);
            Vec3 vel = getDeltaMovement();
            if (vel.length() < HOMING_SPEED) setDeltaMovement(vel.normalize().scale(HOMING_SPEED));
        }
    }

    private void handleEntityCollision(Vec3 start, Vec3 end, Vec3 motion) {
        AABB box = getBoundingBox().expandTowards(motion).inflate(getScale() * 0.5);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level(), this, start, end, box,
                e -> e.isAlive() && e != this && e != getOwner());

        if (entityHit == null) return;

        Entity target = entityHit.getEntity();
        if (!(target instanceof LivingEntity living)) return;

        if (EntityHelper.isFriendlyToPlayer(living, getOwner()) || living == getOwner() || damagedEntities.contains(target.getUUID()))
            return;

        IsaacAttackBeforeHitEntityEvent beforeEvent = new IsaacAttackBeforeHitEntityEvent(
                getOwner(), AttackTypeId.BULLET.getId(), hitEffectIds, entityHit, damage);

        if (MinecraftForge.EVENT_BUS.post(beforeEvent)) return;

        double damageValue = beforeEvent.getDamage();
        if (living.invulnerableTime > 0) living.invulnerableTime = 0;
        living.hurt(makeDamageSource(), (float) damageValue);

        IsaacAttackAfterHitEvent event = new IsaacAttackAfterHitEvent(
                this, AttackTypeId.BULLET.getId(), hitEffectIds, entityHit, damageValue, living.getHealth());
        MinecraftForge.EVENT_BUS.post(event);

        damagedEntities.add(target.getUUID());

        if (!isPiercing && event.shouldDiscardAfterHit()) {
            if (!MinecraftForge.EVENT_BUS.post(new TearBulletDiscardEvent(this))) discard();
        }
    }

    public LivingEntity getTrackingTarget() {
        LivingEntity owner = getOwner();
        if (!(owner instanceof LivingEntity)) return null; // 安全检查

        Vec3 bulletPos = this.position();
        Vec3 forwardPos = bulletPos.add(this.getDeltaMovement().normalize().scale(3.0));

        return EntityHelper.findNearestTrackingTarget(
                level(),
                getOwner(),
                forwardPos, // 用前方位置作为中心
                HOMING_RANGE,
                e -> !damagedEntities.contains(e.getUUID())
        );
    }

    // ======== 追踪/转向方法 ========
    public void steerHorizontalOrbit(LivingEntity target, double steerStrength, double radius, double angularSpeed) {
        if (target == null) return;

        Vec3 ownerPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        if (orbitAngle == null) orbitAngle = Math.atan2(getZ() - ownerPos.z, getX() - ownerPos.x);
        double angle = orbitAngle + angularSpeed * tickCount;

        double targetX = ownerPos.x + radius * Math.cos(angle);
        double targetZ = ownerPos.z + radius * Math.sin(angle);

        double targetY = target.getY() + target.getBbHeight() * 0.5;
        targetY += (targetY - getY()) * 0.05;

        steerTowards(new Vec3(targetX, targetY, targetZ), steerStrength, true);
    }

    public void steerTowards(LivingEntity target, double steerStrength) {
        if (target == null) return;

        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        Vec3 toTarget = targetPos.subtract(position());

        Vec3 currentVel = getDeltaMovement();
        if (currentVel.lengthSqr() < 1e-6) return; // 避免零向量

        // 计算角度差，距离越近 steerStrength 越小，避免绕圈
        double distance = toTarget.length();
        double adaptiveSteer = steerStrength;
        if (distance < 1.0) adaptiveSteer *= distance; // 距离小 steering 小

        // 平滑转向目标方向
        Vec3 desiredVel = toTarget.normalize().scale(currentVel.length());
        Vec3 newVel = currentVel.add(desiredVel.subtract(currentVel).scale(adaptiveSteer));

        // 保持原有速度长度
        newVel = newVel.normalize().scale(currentVel.length());

        setDeltaMovement(newVel);
    }


    public void steerTowards(Vec3 targetPos, double steerStrength, boolean normalizeSpeed) {
        Vec3 currentVel = getDeltaMovement();
        if (currentVel.lengthSqr() < 1e-6) return;

        Vec3 toTarget = targetPos.subtract(position());
        double distance = toTarget.length();

        // 根据距离自适应 steer，避免绕圈
        double adaptiveSteer = steerStrength;
        if (distance < 1.0) adaptiveSteer *= distance;

        // 平滑转向
        Vec3 desiredVel = toTarget.normalize();
        Vec3 newVel = currentVel.normalize().add(desiredVel.subtract(currentVel.normalize()).scale(adaptiveSteer));

        // 保持原速度长度
        if (normalizeSpeed) newVel = newVel.normalize().scale(currentVel.length());

        setDeltaMovement(newVel);
    }

    // ======== DamageSource ========
    private DamageSource makeDamageSource() {
        if (!(level() instanceof ServerLevel serverLevel)) return this.damageSources().generic();

        var damageTypeHolder = serverLevel.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,
                        ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "tear")));

        return new DamageSource(damageTypeHolder, this, getOwner());
    }

    // ======== Owner ========
    @Nullable
    public LivingEntity getOwner() {
        if (cachedOwner == null && ownerUUID != null && level() instanceof ServerLevel server)
            cachedOwner = (LivingEntity) server.getEntity(ownerUUID);
        return cachedOwner;
    }

    public void setOwner(LivingEntity entity) {
        this.cachedOwner = entity;
        this.ownerUUID = entity == null ? null : entity.getUUID();
    }

    // ======== 同步数据 ========
    @Override
    protected void defineSynchedData() {
        entityData.define(SCALE, 1.0F);
        entityData.define(COLOR, 0xFFFFFF);
        entityData.define(ALPHA, 1.0F);
    }

    // ======== NBT读写 ========
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifeTick = tag.getInt("life_tick");
        setScale(tag.getFloat("scale"));
        isSpectral = tag.getBoolean("is_spectral");
        isPiercing = tag.getBoolean("is_piercing");
        if (tag.hasUUID("owner")) ownerUUID = tag.getUUID("owner");
        isHoming = tag.getBoolean("is_homing");
        isControllable = tag.getBoolean("is_controllable");

        damagedEntities.clear();
        if (tag.contains("DamagedEntities", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("DamagedEntities", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) damagedEntities.add(NbtUtils.loadUUID(listTag.getCompound(i)));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("life_tick", lifeTick);
        tag.putFloat("scale", getScale());
        tag.putBoolean("is_spectral", isSpectral);
        tag.putBoolean("is_piercing", isPiercing);
        if (ownerUUID != null) tag.putUUID("owner", ownerUUID);
        tag.putBoolean("is_homing", isHoming);
        tag.putBoolean("is_controllable", isControllable);

        ListTag listTag = new ListTag();
        for (UUID id : damagedEntities) listTag.add(NbtUtils.createUUID(id));
        tag.put("DamagedEntities", listTag);
    }

    // ======== 客户端 ========
    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() { return false; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public boolean isAttackable() { return false; }
    @Override
    public boolean canBeCollidedWith() { return false; }

    // ======== Getter/Setter ========
    public void setSpectral(boolean b) { this.isSpectral = b; }
    public void setPiercing(boolean b) { this.isPiercing = b; }
    public void setHoming(boolean b) { this.isHoming = b; }
    public void setControllable(boolean b) { this.isControllable = b; }

    public float getScale() { return this.entityData.get(SCALE); }
    public int getColor() { return this.entityData.get(COLOR); }
    public float getAlpha() { return this.entityData.get(ALPHA); }

    public void setScale(float scale) {
        this.entityData.set(SCALE, scale);
        this.setBoundingBox(new AABB(getX() - scale * 0.25, getY() - scale * 0.25, getZ() - scale * 0.25,
                getX() + scale * 0.25, getY() + scale * 0.25, getZ() + scale * 0.25));
    }

    public void setColor(int rgb) { this.entityData.set(COLOR, rgb); }
    public void setAlpha(float alpha) { this.entityData.set(ALPHA, Mth.clamp(alpha, 0.0F, 1.0F)); }
    public void setDamage(float damage) { this.damage = damage; }
    public float getDamage() { return damage; }

    // ======== Bullet Effects ========
    public void setHitEffectIds(Set<Integer> hitEffects) {
        this.hitEffectIds.clear();
        this.hitEffectIds.addAll(hitEffects);
    }
    public void clearBulletHitEffects() { hitEffectIds.clear(); }
    public Set<Integer> getHitEffectIds() { return hitEffectIds; }

    public Set<UUID> getDamagedEntities() {
        return damagedEntities;
    }
}
