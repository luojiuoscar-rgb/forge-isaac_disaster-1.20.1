package net.luojiuoscar.isaac_disaster.manager.attack.types;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.IAttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.AttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.BulletColor;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;

public class BulletAttack implements IAttackType {
    @Override
    public int getId() {
        return AttackType.BULLET.getId();
    }

    @Override
    public double getPriority() {
        return AttackType.BULLET.getPriority();
    }

    @Override
    public void makeSound(LivingEntity entity){
        entity.level().playSound(
                null,
                entity.blockPosition(),
                ModSounds.TEAR_BULLET_SHOT.get(),
                SoundSource.PLAYERS,
                0.6f,
                1.0f
        );
    }

    public void handleAttack(LivingEntity entity, int colorId, Set<Integer> hitEffects, Set<Integer> trajectories) {
        int count = entity instanceof Player player ? getBulletCount(player) : 1;

        if (count <= 1) {
            shotBullet(entity, colorId, hitEffects, trajectories);
        } else if (count == 2) {
            shot2Bullet(entity, colorId, hitEffects, trajectories);
        } else {
            float angleInterval = Math.max(11 - count, 5) * 2;
            float curAngle = -angleInterval * (count - 1) / 2.0f;

            for (int i = 0; i < count; i++) {
                shotBullet(entity, entity.getXRot(), entity.getYRot() + curAngle, colorId, hitEffects, trajectories);
                curAngle += angleInterval;
            }
        }
    }

    // =================== 子弹发射 ===================
    private TearBullet createBullet(LivingEntity shooter, Vec3 spawnPos, float xRot, float yRot, int colorId,
                                    Set<Integer> hitEffects, Set<Integer> trajectories) {
        double width = shooter.getBbWidth();
        double forwardOffset = 0.4 * (width / 0.6);
        Vec3 look = Vec3.directionFromRotation(xRot, yRot);
        Vec3 adjustedPos = spawnPos.add(look.scale(forwardOffset));

        TearBullet bullet = new TearBullet(
                shooter.level(),
                shooter,
                getBulletLiftTime(shooter),
                getBulletSpeed(shooter),
                getBulletScale(shooter),
                getDamage(shooter),
                xRot,
                yRot
        );

        bullet.setSpectral(isSpectral(shooter));
        bullet.setPiercing(isPiercing(shooter));
        bullet.setHoming(isHoming(shooter));
        bullet.setControllable(isControllable(shooter));

        bullet.setHitEffectIds(hitEffects);
        bullet.setTrajectories(trajectories);

        bullet.setColor(BulletColor.getColorById(colorId));
        bullet.setAlpha(BulletColor.getAlphaById(colorId));

        bullet.moveTo(adjustedPos.x, adjustedPos.y, adjustedPos.z, yRot, xRot);
        bullet.setDeltaMovement(look.scale(getBulletSpeed(shooter)));

        if (!shooter.level().isClientSide)
            MinecraftForge.EVENT_BUS.post(new TearBulletShootEvent(bullet.getOwner(), getId(), hitEffects, bullet));

        return bullet;
    }

    private void shotBullet(LivingEntity entity, int colorId, Set<Integer> hitEffects, Set<Integer> trajectories) {
        shotBullet(entity, entity.getXRot(), entity.getYRot(), colorId, hitEffects, trajectories);
    }

    private void shotBullet(LivingEntity entity, float xRot, float yRot, int colorId, Set<Integer> hitEffects, Set<Integer> trajectories) {
        if (entity.level().isClientSide()) return;
        Vec3 eyePos = entity.getEyePosition().add(0, entity.getBbHeight() * -0.15, 0);
        TearBullet bullet = createBullet(entity, eyePos, xRot, yRot, colorId, hitEffects, trajectories);
        entity.level().addFreshEntity(bullet);
    }

    private void shot2Bullet(LivingEntity entity, int colorId, Set<Integer> hitEffects, Set<Integer> trajectories) {
        if (entity.level().isClientSide()) return;
        Vec3 look = entity.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 eyePos = entity.getEyePosition().add(0, entity.getBbHeight() * -0.15, 0);

        TearBullet bullet1 = createBullet(
                entity, eyePos.add(right.scale(0.25)),
                entity.getXRot(), entity.getYRot(), colorId, hitEffects, trajectories);
        TearBullet bullet2 = createBullet(
                entity, eyePos.add(right.scale(-0.25)),
                entity.getXRot(), entity.getYRot(), colorId, hitEffects, trajectories);

        entity.level().addFreshEntity(bullet1);
        entity.level().addFreshEntity(bullet2);
    }

    // =================== 基础属性 ===================
    private int getBulletLiftTime(LivingEntity entity) {
        double speed = getBulletSpeed(entity);
        double range = getRange(entity);
        return (int) Math.min(Math.max(1, range / speed), 200);
    }
}
