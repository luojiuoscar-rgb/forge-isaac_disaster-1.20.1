package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


import java.util.List;

public class IsaacBomb extends PrimedTnt {
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(IsaacBomb.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_ORIGINAL =
            SynchedEntityData.defineId(IsaacBomb.class, EntityDataSerializers.BOOLEAN);

    private int power;
    public IsaacBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale) {
        this(pLevel, pX, pY, pZ, pOwner, power, scale, true);
    }
    public IsaacBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, int power, float scale, boolean isOriginal) {
        super(pLevel, pX, pY, pZ, pOwner);
        this.power = power;
        this.entityData.set(DATA_SCALE, scale);
        this.entityData.set(DATA_IS_ORIGINAL, isOriginal);
    }

    public boolean isOriginal(){
        return this.entityData.get(DATA_IS_ORIGINAL);
    }

    public void setOriginal(boolean isOriginal) {
        this.entityData.set(DATA_IS_ORIGINAL, isOriginal);
    }

    public int getPower() {
        return power;
    }

    public IsaacBomb(EntityType<? extends IsaacBomb> type, Level level) {
        super(type, level);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SCALE, 1.0f);
        this.entityData.define(DATA_IS_ORIGINAL, true);
    }

    public float getScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public void setScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
    }

    @Override
    protected void explode() {
        this.level().explode(this, this.getX(), this.getY(0.0625D), this.getZ(), power, Level.ExplosionInteraction.TNT);
    }

    public void setPower(int power) {
        this.power = power;
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
            LivingEntity target = getTrackingTarget();
            if (target != null) {
                steerTowards(target, 0.1); // 0.3 = 转向速度
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



    @Nullable
    public LivingEntity getTrackingTarget() {
        double range = 16.0;
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(range),
                e -> e != this.getOwner() && e.isAlive()
        );

        LivingEntity hostileAggro = null;      // 有敌意的敌对生物
        LivingEntity hostilePassive = null;    // 没敌意的敌对生物
        LivingEntity neutral = null;           // 中立
        LivingEntity playerTarget = null;      // 玩家

        for (LivingEntity e : nearby) {
            // ==== 敌对生物（Monster 类） ====
            if (e instanceof Monster monster) {
                boolean hasAggro = false;

                // 1. mob.getTarget() 是否指向 owner
                if (monster.getTarget() == this.getOwner()) {
                    hasAggro = true;
                }

                // 2. 如果是中立怪，如Enderman / Piglin / Bee等
                if (monster instanceof NeutralMob neutralMob && this.getOwner() instanceof Player player) {
                    if (neutralMob.isAngryAt(player)) {
                        hasAggro = true;
                    }
                }

                if (hasAggro) {
                    if (hostileAggro == null || this.distanceToSqr(e) < this.distanceToSqr(hostileAggro)) {
                        hostileAggro = e;
                    }
                } else {
                    if (hostilePassive == null || this.distanceToSqr(e) < this.distanceToSqr(hostilePassive)) {
                        hostilePassive = e;
                    }
                }
            }
            // ==== 中立生物（不攻击不反击的） ====
            else if (e instanceof PathfinderMob) {
                if (neutral == null || this.distanceToSqr(e) < this.distanceToSqr(neutral)) {
                    neutral = e;
                }
            }
            // ==== 玩家 ====
            else if (e instanceof Player otherPlayer) {
                if (this.getOwner() instanceof Player ownerPlayer) {
                    if (ownerPlayer.canHarmPlayer(otherPlayer) && !ownerPlayer.isAlliedTo(otherPlayer)) {
                        if (playerTarget == null || this.distanceToSqr(e) < this.distanceToSqr(playerTarget)) {
                            playerTarget = e;
                        }
                    }
                }
            }
        }

        if (hostileAggro != null) return hostileAggro;
        if (hostilePassive != null) return hostilePassive;
        if (neutral != null) return neutral;
        return playerTarget;
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





    private void onHitEntity(LivingEntity entity) {
        if (!this.level().isClientSide()) {

            this.setDeltaMovement(Vec3.ZERO);
            entity.addEffect(new MobEffectInstance(ModEffects.DIZZINESS.get(), 20, 0));
        }
    }


}
