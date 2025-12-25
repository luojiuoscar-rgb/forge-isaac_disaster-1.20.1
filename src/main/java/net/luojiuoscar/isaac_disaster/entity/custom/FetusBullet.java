package net.luojiuoscar.isaac_disaster.entity.custom;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FetusBullet extends TearBullet{
    private int attackInterval = 0;

    public FetusBullet(Level level, LivingEntity shooter, int lifeTick, double bulletSpeed, float scale, float damage, float xRot, float yRot) {
        super(ModEntities.FETUS_BULLET.get(), level, shooter, lifeTick, bulletSpeed, scale, damage, xRot, yRot);
        homingRange = 6.0;
    }

    public FetusBullet(EntityType<? extends TearBullet> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick(){
        super.tick();
        if (this.attackInterval > 0) attackInterval--;
    }

    @Override
    protected boolean makeDamage(LivingEntity victim, float damage){
        if (attackInterval <= 0){
            victim.invulnerableTime = 0;
            victim.hurt(getDamageSource(), damage);
            attackInterval = 5;
            return true;
        }
        return false;
    }

    @Override
    public LivingEntity getTrackingTarget() {
        Vec3 bulletPos = this.position();
        Vec3 forwardPos = bulletPos.add(getVelocity().normalize().scale(1));

        return EntityHelper.findNearestTrackingTarget(
                level(),
                getOwner(),
                forwardPos,
                getHomingRange(),
                e -> !damagedEntities.contains(e.getUUID())
        );
    }

    @Override
    protected boolean handleSteering() {
        LivingEntity owner = getOwner();
        LivingEntity target = isHoming ? getTrackingTarget() : null;

        if (target == null && isControllable && owner != null) {
            Vec3 targetPos = owner.getEyePosition()
                    .add(Vec3.directionFromRotation(owner.getXRot(), owner.getYRot()).scale(getControlRange()));
            steerTowards(targetPos, getControlSteer(), true);
            return true;
        }

        if (target != null) {
            // 计算到目标的距离
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Vec3 toTarget = targetPos.subtract(position());
            double distance = toTarget.length();

            // 根据距离动态调节速度
            double minSpeed = 0.25;
            double maxSpeed = getHomingSpeed();
            double adjustedSpeed;

            // 距离越小，速度越慢，越远就接近原始速度
            double slowDownRange = getHomingRange(); // 开始减速的距离
            if (distance < slowDownRange) {
                adjustedSpeed = Mth.clamp(maxSpeed * (distance / slowDownRange), minSpeed, maxSpeed);
            } else {
                adjustedSpeed = maxSpeed;
            }

            // 让子弹朝目标转向
            steerTowards(target, getHomingSteer());

            // 调整速度
            Vec3 currentVel = getVelocity();
            setVelocity(currentVel.normalize().scale(adjustedSpeed));

            return true;
        }

        return false;
    }

    @Override
    protected AABB getAABB(Vec3 motion){
        return getBoundingBox().expandTowards(motion).inflate(getScale());
    }

    @Override
    public boolean noGravity(){
        return true;
    }
}
