package net.luojiuoscar.isaac_disaster.entity;

import net.luojiuoscar.isaac_disaster.event.custom.*;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IsaacBullet extends Entity {
    private int lifeTick;
    private UUID ownerUUID;
    private LivingEntity cachedOwner;
    private float damage;


    private boolean isSpectral = false; // 灵体
    private boolean isPiercing = false; // 穿刺
    private boolean isHoming = false; // 追踪
    private static final double HOMING_RANGE = 6.0; // 最大追踪范围
    private static final double HOMING_SPEED = 1.0;  // 追踪时最小速度
    private static final double HOMING_STEER = 0.6;  // 追踪时转向强度
    private boolean isControllable = false; // 可控泪弹（受玩家准星方向影响）
    private static final double CONTROL_RANGE = 64.0; // 可控泪弹目标点距离玩家前方的距离
    private static final double CONTROL_STEER = 0.8;  // 可控泪弹转向强度



    // 记录已击中过的实体，防止重复伤害
    private final List<UUID> damagedEntities = new ArrayList<>();

    // 同步属性以供客户端渲染使用
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(IsaacBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(IsaacBullet.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ALPHA =
            SynchedEntityData.defineId(IsaacBullet.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FILTER_COLOR =
            SynchedEntityData.defineId(IsaacBullet.class, EntityDataSerializers.INT);







    public IsaacBullet(EntityType<? extends IsaacBullet> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public IsaacBullet(Level level, LivingEntity shooter, int lifeTick, double bulletSpeed,
                       float scale, boolean spectral, boolean piercing, boolean homing, boolean controllable,
                       float damage) {
        this(ModEntities.TEAR_BULLET.get(), level);
        this.ownerUUID = shooter.getUUID();
        this.cachedOwner = shooter;
        this.lifeTick = lifeTick;
        this.isSpectral = spectral;
        this.isPiercing = piercing;
        this.isHoming = homing;
        this.isControllable = controllable;
        this.noPhysics = true;
        this.damage = damage;

        this.setScale(scale);

        this.moveTo(shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ(),
                shooter.getYRot(), shooter.getXRot());

        Vec3 look = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
        this.setDeltaMovement(look.scale(bulletSpeed));

        if (!level.isClientSide)
            MinecraftForge.EVENT_BUS.post(new IsaacBulletShootEvent(this, shooter));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            MinecraftForge.EVENT_BUS.post(new IsaacBulletTickEvent(this));

            if (--lifeTick <= 0) {
                if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletDiscardEvent(this))) discard();
                return;
            }

            Vec3 start = position();
            Vec3 motion = getDeltaMovement();
            Vec3 end = start.add(motion);

            // 方块碰撞检测（非灵体弹）
            if (!isSpectral) {
                BlockHitResult blockHit = level().clip(new ClipContext(
                        start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
                ));

                if (blockHit.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = level().getBlockState(pos);

                    // 判断是否可穿透
                    if (!state.is(TagManager.PENETRABLE_BLOCKS)) {
                        if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletHitBlockEvent(this, blockHit))) {
                            if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletDiscardEvent(this))) discard();
                        }
                        return;
                    }
                }

            }

            // 每4tick更新方向逻辑
            if (this.tickCount % 4 == 0) {
                LivingEntity owner = getOwner();
                LivingEntity target = null;

                // 优先：追踪目标
                if (isHoming) {
                    target = getTrackingTarget();
                }

                // 若无追踪目标且为可控泪弹
                if (target == null && isControllable && owner != null) {
                    Vec3 lookDir = Vec3.directionFromRotation(owner.getXRot(), owner.getYRot());
                    Vec3 targetPos = owner.getEyePosition().add(lookDir.scale(CONTROL_RANGE));
                    steerTowards(targetPos, CONTROL_STEER, false);
                }

                // 若存在追踪目标
                if (target != null) {
                    steerTowards(target, HOMING_STEER);
                    Vec3 vel = getDeltaMovement();
                    if (vel.length() < HOMING_SPEED) {
                        setDeltaMovement(vel.normalize().scale(HOMING_SPEED));
                    }
                }
            }


            // 实体碰撞检测
            AABB box = getBoundingBox().expandTowards(motion).inflate(getScale() * 0.5);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    level(), this, start, end, box,
                    e -> e.isAlive() && e != this && e != getOwner()
            );

            if (entityHit != null) {
                Entity target = entityHit.getEntity();
                UUID targetId = target.getUUID();

                // 已经命中过的实体跳过
                if (damagedEntities.contains(targetId)) {
                    move(MoverType.SELF, motion);
                    return;
                }

                // 新命中目标
                if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletHitEntityEvent(this, entityHit))) {
                    if (target instanceof LivingEntity living) {
                        DamageSource src = makeDamageSource();
                        // 暂时清除受伤无敌帧
                        int oldHurtResistant = living.invulnerableTime;
                        living.invulnerableTime = 0;

                        // 造成伤害
                        living.hurt(src, damage);
                        // 恢复状态
                        living.invulnerableTime = oldHurtResistant;
                    }

                    // 记录命中过的目标
                    damagedEntities.add(targetId);

                    // 非穿刺弹命中后立即消失
                    if (!isPiercing)
                        if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletDiscardEvent(this))) discard();
                }

                if (!isPiercing)
                    return;
            }

            move(MoverType.SELF, motion);
        }else{
            move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    @Nullable
    public LivingEntity getTrackingTarget() {
        double range = HOMING_RANGE;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(range),
                e -> e.isAlive()
                        && e != this.getOwner()
                        && !damagedEntities.contains(e.getUUID()) // 不追踪已命中过的目标
        );

        LivingEntity hostileAggro = null;      // 有敌意的敌对生物
        LivingEntity hostilePassive = null;    // 没敌意的敌对生物
        LivingEntity neutral = null;           // 中立
        LivingEntity playerTarget = null;      // 玩家

        for (LivingEntity e : nearby) {
            if (e instanceof Monster monster) {
                boolean hasAggro = false;
                if (monster.getTarget() == this.getOwner()) hasAggro = true;
                if (monster instanceof NeutralMob neutralMob && this.getOwner() instanceof Player player)
                    if (neutralMob.isAngryAt(player)) hasAggro = true;

                if (hasAggro) {
                    if (hostileAggro == null || this.distanceToSqr(e) < this.distanceToSqr(hostileAggro))
                        hostileAggro = e;
                } else {
                    if (hostilePassive == null || this.distanceToSqr(e) < this.distanceToSqr(hostilePassive))
                        hostilePassive = e;
                }
            } else if (e instanceof PathfinderMob) {
                if (neutral == null || this.distanceToSqr(e) < this.distanceToSqr(neutral))
                    neutral = e;
            } else if (e instanceof Player otherPlayer) {
                if (this.getOwner() instanceof Player ownerPlayer)
                    if (ownerPlayer.canHarmPlayer(otherPlayer) && !ownerPlayer.isAlliedTo(otherPlayer))
                        if (playerTarget == null || this.distanceToSqr(e) < this.distanceToSqr(playerTarget))
                            playerTarget = e;
            }
        }

        if (hostileAggro != null) return hostileAggro;
        if (hostilePassive != null) return hostilePassive;
        if (neutral != null) return neutral;
        return playerTarget;
    }


    private void steerTowards(LivingEntity target, double steerStrength) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        double distSqr = this.position().distanceToSqr(targetPos);
        if (distSqr < 1.0) return; // 太近就不再追踪

        // 简单预测（提升平滑性）
        Vec3 predicted = targetPos.add(target.getDeltaMovement().scale(3.0));

        // 使用平滑加速度式追踪
        Vec3 currentVel = this.getDeltaMovement();
        Vec3 desiredVel = predicted.subtract(this.position()).normalize().scale(currentVel.length());
        Vec3 steering = desiredVel.subtract(currentVel).scale(steerStrength);

        Vec3 newVel = currentVel.add(steering).normalize().scale(currentVel.length());
        this.setDeltaMovement(newVel);
    }
    private void steerTowards(Vec3 targetPos, double steerStrength, boolean normalizeSpeed) {
        Vec3 currentVel = this.getDeltaMovement();
        if (currentVel.lengthSqr() < 0.0001) return;

        Vec3 directionToTarget = targetPos.subtract(this.position()).normalize();
        Vec3 newVel = currentVel.normalize()
                .lerp(directionToTarget, steerStrength)
                .normalize();

        if (normalizeSpeed) {
            // 保持当前速度大小
            newVel = newVel.scale(currentVel.length());
        }

        this.setDeltaMovement(newVel);
    }


    private DamageSource makeDamageSource() {
        if (!(level() instanceof ServerLevel serverLevel))
            return level().damageSources().generic();

        return new DamageSource(serverLevel.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE,
                        ResourceLocation.fromNamespaceAndPath("isaac_disaster", "tear"))));
    }
    @Nullable
    public LivingEntity getOwner() {
        if (cachedOwner == null && ownerUUID != null)
            if (level() instanceof ServerLevel server)
                cachedOwner = (LivingEntity) server.getEntity(ownerUUID);
        return cachedOwner;
    }

    public void setOwner(LivingEntity entity) {
        this.cachedOwner = entity;
        this.ownerUUID = entity == null ? null : entity.getUUID();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(COLOR, 0xFFFFFF); // 白色
        this.entityData.define(ALPHA, 1.0F);
        this.entityData.define(FILTER_COLOR, 0xFFFFFF); // 初始无滤镜
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifeTick = tag.getInt("life_tick");
        setScale(tag.getFloat("scale"));
        isSpectral = tag.getBoolean("is_spectral");
        isPiercing = tag.getBoolean("is_piercing");
        if (tag.hasUUID("owner")) ownerUUID = tag.getUUID("owner");
        isHoming = tag.getBoolean("is_homing");
        isControllable = tag.getBoolean("is_controllable");


        // 读取已命中过的实体
        damagedEntities.clear();
        if (tag.contains("DamagedEntities", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("DamagedEntities", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                damagedEntities.add(NbtUtils.loadUUID(listTag.getCompound(i)));
            }
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


        // 保存命中过的实体
        ListTag listTag = new ListTag();
        for (UUID id : damagedEntities) {
            listTag.add(NbtUtils.createUUID(id)); // 将 UUID 转为 CompoundTag
        }
        tag.put("DamagedEntities", listTag);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
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

    public void setSpectral(boolean b) { this.isSpectral = b; }
    public boolean isSpectral() { return this.isSpectral; }
    public void setPiercing(boolean b) { this.isPiercing = b; }
    public boolean isPiercing() { return this.isPiercing; }
    public void setHoming(boolean b) { this.isHoming = b; }
    public boolean isHoming() { return this.isHoming; }
    public void setControllable(boolean b) { this.isControllable = b; }
    public boolean isControllable() { return this.isControllable; }

    public float getScale() {
        return this.entityData.get(SCALE);
    }
    public int getColor() {
        return this.entityData.get(COLOR);
    }
    public float getAlpha() {
        return this.entityData.get(ALPHA);
    }
    public int getFilterColor() {
        return this.entityData.get(FILTER_COLOR);
    }

    public void setScale(float scale) {
        this.entityData.set(SCALE, scale);
        this.setBoundingBox(new AABB(
                getX() - scale * 0.25,
                getY() - scale * 0.25,
                getZ() - scale * 0.25,
                getX() + scale * 0.25,
                getY() + scale * 0.25,
                getZ() + scale * 0.25
        ));
    }
    public void setColor(int rgb) {
        this.entityData.set(COLOR, rgb);
    }
    public void setAlpha(float alpha) {
        this.entityData.set(ALPHA, Mth.clamp(alpha, 0.0F, 1.0F));
    }
    public void setFilterColor(int rgb) {
        this.entityData.set(FILTER_COLOR, rgb);
    }
    public void setDamage(float damage){
        this.damage = damage;
    }
    public float getDamage() {
        return damage;
    }
}
