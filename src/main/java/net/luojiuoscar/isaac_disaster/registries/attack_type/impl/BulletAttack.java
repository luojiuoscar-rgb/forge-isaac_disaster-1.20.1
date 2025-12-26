package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class BulletAttack extends AttackType {
    public BulletAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackType.BULLET.getId();
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext ctx = getOneAttackContext(player, player);

        List<AttackContext> contexts = new ArrayList<>();
        if (ctx == null) return contexts;

        if (bulletCount == 2) {
            Vec3 look = player.getLookAngle();
            Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();

            AttackContext c = ctx.copy();
            c.setPos(ctx.getPos().add(right.scale(0.25)));
            contexts.add(c);

            c = ctx.copy();
            c.setPos(ctx.getPos().add(right.scale(-0.25)));
            contexts.add(c);

        } else {
            float angleInterval = Math.max(11 - bulletCount, 5) * 2;
            float curAngle = -angleInterval * (bulletCount - 1) / 2.0f;

            for (int i = 0; i < bulletCount; i++) {
                AttackContext c = ctx.copy();
                c.setYRot(ctx.getYRot() + curAngle);
                contexts.add(c);

                curAngle += angleInterval;
            }
        }

        return contexts;
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

    public void performAttack(List<AttackContext> ctxList) {
        for (AttackContext c : ctxList){
            shoot(c);
        }
    }

    // =================== 子弹发射 ===================
    @Override
    public void shoot(AttackContext ctx) {
        if (ctx.getOwner().level().isClientSide()) return;

        TearBullet bullet = createBullet(ctx);
        ctx.getOwner().level().addFreshEntity(bullet);
    }

    protected TearBullet createBullet(AttackContext context) {
        LivingEntity owner = context.getOwner();
        double width = owner.getBbWidth();
        double forwardOffset = 0.4 * (width / 0.6);
        Vec3 look = Vec3.directionFromRotation(context.getXRot(), context.getYRot());
        Vec3 adjustedPos = context.getPos().add(look.scale(forwardOffset));

        TearBullet bullet = getBulletObject(context);

        bullet.setSpectral(isSpectral(owner));
        bullet.setPiercing(isPiercing(owner));
        bullet.setHoming(isHoming(owner));
        bullet.setControllable(isControllable(owner));

        bullet.setTriggerModules(context.getTriggerModuleQueue());
        bullet.setTrajectories(context.trajectories);

        bullet.setBulletColor(context.colorRl);

        bullet.moveTo(adjustedPos.x, adjustedPos.y, adjustedPos.z, context.getYRot(), context.getXRot());
        bullet.setDeltaMovement(look.scale(getBulletSpeed(owner)));

        if (!owner.level().isClientSide)
            MinecraftForge.EVENT_BUS.post(
                    new TearBulletShootEvent(bullet, bullet.getOwner(), getId(), context.getTriggerModuleQueue(), bullet));

        return bullet;
    }

    public TearBullet getBulletObject(AttackContext c){
        LivingEntity owner = c.getOwner();

        return new TearBullet(
                owner.level(),
                c.getOwner(),
                c.getShooter(),
                getBulletLiftTime(owner),
                getBulletSpeed(owner),
                getBulletScale(owner),
                getDamage(owner),
                c.getXRot(),
                c.getYRot(),
                c.getPos()
        );
    }

    // =================== 基础属性 ===================
    int getBulletLiftTime(LivingEntity entity) {
        double speed = getBulletSpeed(entity);
        double range = getRange(entity);
        return (int) Math.min(Math.max(1, range / speed), 200);
    }
}
