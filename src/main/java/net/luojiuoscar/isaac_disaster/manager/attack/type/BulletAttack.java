package net.luojiuoscar.isaac_disaster.manager.attack.type;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.IAttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class BulletAttack implements IAttackType {
    @Override
    public int getId() {
        return ModAttackType.BULLET.getId();
    }

    @Override
    public double getPriority() {return ModAttackType.BULLET.getPriority();}


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

    public void handleAttack(LivingEntity entity, AttackContext context) {
        int count = entity instanceof Player player ? getBulletCount(player) : 1;

        if (count <= 1) {
            shotBullet(entity, context, entity.getXRot(), entity.getYRot());
        } else if (count == 2) {
            shot2Bullet(entity, context);
        } else {
            float angleInterval = Math.max(11 - count, 5) * 2;
            float curAngle = -angleInterval * (count - 1) / 2.0f;

            for (int i = 0; i < count; i++) {
                shotBullet(entity, context, entity.getXRot(), entity.getYRot() + curAngle);
                curAngle += angleInterval;
            }
        }
    }

    // =================== 子弹发射 ===================
    @Override
    public void handleShoot(LivingEntity shooter, AttackContext context, Vec3 offset, float xRot, float yRot) {
        if (shooter.level().isClientSide()) return;

        Vec3 eyePos = shooter.getEyePosition().add(0, shooter.getBbHeight() * -0.15, 0);
        Vec3 spawnPos = eyePos.add(offset);

        TearBullet bullet = createBullet(shooter, spawnPos, xRot, yRot, context);
        shooter.level().addFreshEntity(bullet);
    }

    private void shotBullet(LivingEntity entity, AttackContext context, float xRot, float yRot) {
        shoot(entity, context, Vec3.ZERO, xRot, yRot);
    }

    private void shot2Bullet(LivingEntity entity, AttackContext context) {
        Vec3 look = entity.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();

        shoot(entity, context, right.scale(0.25), entity.getXRot(), entity.getYRot());
        shoot(entity, context, right.scale(-0.25), entity.getXRot(), entity.getYRot());
    }

    private TearBullet createBullet(LivingEntity shooter, Vec3 spawnPos, float xRot, float yRot, AttackContext context) {
        double width = shooter.getBbWidth();
        double forwardOffset = 0.4 * (width / 0.6);
        Vec3 look = Vec3.directionFromRotation(xRot, yRot);
        Vec3 adjustedPos = spawnPos.add(look.scale(forwardOffset));

        TearBullet bullet = getBulletObject(shooter, xRot, yRot);

        bullet.setSpectral(isSpectral(shooter));
        bullet.setPiercing(isPiercing(shooter));
        bullet.setHoming(isHoming(shooter));
        bullet.setControllable(isControllable(shooter));

        bullet.setTriggerModules(context.getTriggerModuleQueue());
        bullet.setTrajectories(context.trajectories);

        // 从registry获取
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColor.BULLET_COLOR_KEY);

        BulletColor c = registry != null ? registry.getValue(context.colorRl) : ModBulletColor.BASE.get();
        c = c == null ? ModBulletColor.BASE.get() : c;

        bullet.setColor(c.color());
        bullet.setAlpha(c.alpha());


        bullet.moveTo(adjustedPos.x, adjustedPos.y, adjustedPos.z, yRot, xRot);
        bullet.setDeltaMovement(look.scale(getBulletSpeed(shooter)));

        if (!shooter.level().isClientSide)
            MinecraftForge.EVENT_BUS.post(
                    new TearBulletShootEvent(bullet, bullet.getOwner(), getId(), context.getTriggerModuleQueue(), bullet));

        return bullet;
    }

    public TearBullet getBulletObject(LivingEntity shooter, float xRot, float yRot){
        return new TearBullet(
                shooter.level(),
                shooter,
                getBulletLiftTime(shooter),
                getBulletSpeed(shooter),
                getBulletScale(shooter),
                getDamage(shooter),
                xRot,
                yRot
        );
    }

    // =================== 基础属性 ===================
    int getBulletLiftTime(LivingEntity entity) {
        double speed = getBulletSpeed(entity);
        double range = getRange(entity);
        return (int) Math.min(Math.max(1, range / speed), 200);
    }
}
