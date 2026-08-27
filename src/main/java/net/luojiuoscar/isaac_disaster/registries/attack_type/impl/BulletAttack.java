package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.BulletAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
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
    private static final BulletAttackPattern PATTERN = new BulletAttackPattern();

    public BulletAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public BulletAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.BULLET.getId();
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext ctx = createAttackContext(player, player);
        if (ctx == null) return List.of();
        return PATTERN.generate(new AttackPatternContext(ctx, bulletCount));
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
        LivingEntity owner = ctx.getOwner();
        if (owner == null || owner.level().isClientSide()) return;

        TearBullet bullet = createBullet(ctx);
        finalizeShot(ctx, bullet);
    }

    protected TearBullet createBullet(AttackContext context) {
        LivingEntity owner = context.getOwner();
        double width = owner.getBbWidth();
        double forwardOffset = 0.4 * (width / 0.6);
        Vec3 look = context.getMainAxis();
        GeometryHelper.Rotation rotation = GeometryHelper.rotationFromMainAxis(look);
        Vec3 adjustedPos = context.usesExactSpawnPosition()
                ? context.getPos()
                : context.getPos().add(look.scale(forwardOffset));

        TearBullet bullet = getBulletObject(context);
        bullet.setAttackContext(context);

        bullet.setSpectral(isSpectral(owner));
        bullet.setPiercing(isPiercing(owner));
        bullet.setHoming(isHoming(owner));
        bullet.setControllable(isControllable(owner));

        bullet.getTriggers().addAll(context.copyTrigger());
        bullet.setTrajectories(context.getTrajectories());

        bullet.setBulletColor(context.getColorRl());

        bullet.moveTo(adjustedPos.x, adjustedPos.y, adjustedPos.z, rotation.yRot(), rotation.xRot());
        bullet.setPreflightStart(context.getPos());
        bullet.setVelocity(look.scale(context.getBulletSpeed()));
        bullet.setDeltaMovement(bullet.getVelocity());

        return bullet;
    }

    protected void finalizeShot(AttackContext context, TearBullet bullet) {
        if (!postShootEvent(context, bullet)) {
            return;
        }
        spawnBullet(context, bullet);
    }

    protected boolean postShootEvent(AttackContext context, TearBullet bullet) {
        LivingEntity owner = context.getOwner();
        if (owner == null || owner.level().isClientSide) {
            return true;
        }

        TearBulletShootEvent event =
                new TearBulletShootEvent(bullet, bullet.getOwner(), getId(), bullet.getTriggers(), bullet);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    protected void spawnBullet(AttackContext context, TearBullet bullet) {
        context.getOwner().level().addFreshEntity(bullet);
    }

    public TearBullet getBulletObject(AttackContext c){
        TearBullet bullet = new TearBullet(c);
        bullet.setScale(getBulletScale(c.getOwner(), c.getDamage()));
        return bullet;
    }
}
