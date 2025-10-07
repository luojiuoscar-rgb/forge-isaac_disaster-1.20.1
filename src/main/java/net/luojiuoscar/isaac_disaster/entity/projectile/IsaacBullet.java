package net.luojiuoscar.isaac_disaster.entity.projectile;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;

public class IsaacBullet extends Projectile {
    private int lifeTick;
    private static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(IsaacBullet.class, EntityDataSerializers.FLOAT);

    public IsaacBullet(EntityType<? extends IsaacBullet> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(true); // 禁用重力
    }

    public IsaacBullet(Level level, LivingEntity shooter, int lifeTick, double bulletSpeed, float scale) {
        super(ModEntities.TEAR_BULLET.get(), level);
        this.setOwner(shooter);
        this.lifeTick = lifeTick;
        this.setScale(scale);
        this.noPhysics = true;
        this.setNoGravity(true);

        this.moveTo(shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        this.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, (float) bulletSpeed, 0.0F);

        if (!level().isClientSide) {
            MinecraftForge.EVENT_BUS.post(new IsaacBulletShootEvent(this, shooter));
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            MinecraftForge.EVENT_BUS.post(new IsaacBulletTickEvent(this));

            // 生命周期处理
            if (--lifeTick <= 0) {
                if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletDiscardEvent(this))) {
                    this.discard();
                }
                return;
            }

            // 移动准备
            Vec3 start = this.position();
            Vec3 motion = this.getDeltaMovement();
            Vec3 end = start.add(motion);

            // 实体命中检测
            AABB searchBox = this.getBoundingBox()
                    .expandTowards(motion)
                    .inflate(getScale() * 0.5D);

            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    this.level(), this, start, end, searchBox,
                    (entity) -> entity.isAlive() && entity != this && entity != this.getOwner()
            );

            if (entityHit != null) {
                if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletHitEntityEvent(this, entityHit))) {
                    Entity target = entityHit.getEntity();
                    if (target instanceof LivingEntity living) {
                        DamageSource source;
                        if (this.getOwner() instanceof Player player)
                            source = this.level().damageSources().playerAttack(player);
                        else if (this.getOwner() instanceof LivingEntity owner)
                            source = this.level().damageSources().mobAttack(owner);
                        else
                            source = this.level().damageSources().generic();

                        living.hurt(source, 2.0f);
                    }

                    if (!MinecraftForge.EVENT_BUS.post(new IsaacBulletDiscardEvent(this)))
                        this.discard();
                }
                return;
            }

            // 没命中 → 继续移动
            this.move(MoverType.SELF, motion);
        } else {
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }


    public float getScale() {
        return this.entityData.get(SCALE);
    }

    public void setScale(float scale) {
        this.entityData.set(SCALE, scale);
        this.setBoundingBox(new AABB(
                this.getX() - scale * 0.25,
                this.getY() - scale * 0.25,
                this.getZ() - scale * 0.25,
                this.getX() + scale * 0.25,
                this.getY() + scale * 0.25,
                this.getZ() + scale * 0.25
        ));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifeTick = tag.getInt("life_tick");
        setScale(tag.getFloat("scale"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("life_tick", lifeTick);
        tag.putFloat("scale", getScale());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    /**
     * 优化逻辑：禁用原版行为
     */
    @Override
    protected boolean canHitEntity(Entity target) {
        // 自定义命中检测
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }
}
