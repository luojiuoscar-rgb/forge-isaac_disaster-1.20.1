package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.ModEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class IsaacBomb extends PrimedTnt {
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(IsaacBomb.class, EntityDataSerializers.FLOAT);

    private float power;

    public IsaacBomb(Level pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner, float power, float scale) {
        super(pLevel, pX, pY, pZ, pOwner);
        this.power = power;
        this.entityData.set(DATA_SCALE, scale);
    }

    public IsaacBomb(EntityType<? extends IsaacBomb> type, Level level) {
        super(type, level);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SCALE, 1.0f); // 默认值，必须定义
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

    public void setPower(float power) {
        this.power = power;
    }

    public void setOwner(LivingEntity entity) {
        try {
            Field ownerField = PrimedTnt.class.getDeclaredField("owner");
            ownerField.setAccessible(true);
            ownerField.set(this, entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 检查速度是否大于阈值
        double speed = this.getDeltaMovement().length();
        if (speed < 0.1) return;

        // 获取 TNT 的包围盒
        AABB tntBox = this.getBoundingBox();

        // 遍历附近的生物
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, tntBox.inflate(0.3), e -> e != this.getOwner());
        for (LivingEntity entity : entities) {
            // 命中目标
            onHitEntity(entity);
            break; // 只处理第一个命中的生物
        }
    }

    private void onHitEntity(LivingEntity entity) {
        if (!this.level().isClientSide) {

            this.setDeltaMovement(Vec3.ZERO);
            entity.addEffect(new MobEffectInstance(ModEffects.DIZZINESS.get(), 20, 0));
        }
    }








    public static void spawnBomb(Vec3 position, Player player, Vec3 tntVelocity, int fuse, int power, float scale){
        IsaacBomb tnt = ModEntity.ISAAC_BOMB.get().create(player.level());
        if (tnt == null) return;

        tnt.moveTo(position.x, position.y, position.z, 0, 0);
        tnt.setOwner(player);
        tnt.setFuse(fuse);
        tnt.setPower(power);
        tnt.setScale(scale);
        tnt.setDeltaMovement(tntVelocity);

        player.level().addFreshEntity(tnt);
    }




    public static void throwBomb(Player player, int fuse, int power) {
        throwBomb(player, fuse, power, 0.98f);
    }

    public static void throwBomb(Player player, int fuse, int power, float scale){
        // 获取玩家朝向向量
        Vec3 lookVec = player.getLookAngle();
        // 获取玩家当前速度
        Vec3 playerVelocity = player.getDeltaMovement();

        // 计算TNT生成位置（玩家眼睛位置略微偏移）
        Vec3 spawnPos = player.getEyePosition()
                .add(lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);

        // 计算TNT初速度：结合玩家朝向和玩家自身速度
        double throwStrength = 1.3; // 投掷力度
        double velocityInheritance = 1.0; // 继承玩家速度的比例

        Vec3 tntVelocity = new Vec3(
                lookVec.x * throwStrength + playerVelocity.x * velocityInheritance,
                lookVec.y * throwStrength + playerVelocity.y * velocityInheritance + 0.25, // 略微向上
                lookVec.z * throwStrength + playerVelocity.z * velocityInheritance
        );

        IsaacBomb.spawnBomb(spawnPos, player, tntVelocity, fuse, power, scale);
    }
}
