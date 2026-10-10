package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.BulletAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BulletAttack extends AbstractBulletAttack implements BasicAttackType {
    private static final BulletAttackPattern PATTERN = new BulletAttackPattern();

    public BulletAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public BulletAttack(double priority) {
        super(priority);
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext ctx = createAttackContext(player, player);
        if (ctx == null) return List.of();
        return PATTERN.generate(new AttackPatternContext(ctx, bulletCount));
    }

    @Override
    public void performAttack(List<AttackContext> ctxList) {
        for (AttackContext context : ctxList) {
            shoot(context);
        }
    }

    @Override
    protected BulletState createOptimizedState(AttackContext context) {
        context = context.bindAttackTypeOrCopy(this);
        context.freeze();
        LivingEntity owner = context.getOwner();
        Vec3 look = context.getMainAxis();
        double forwardOffset = 0.4 * (owner.getBbWidth() / 0.6);
        Vec3 position = context.usesFixedLaunchTransform() ? context.getPos() : context.getPos().add(look.scale(forwardOffset));
        int lifetime = (int) Math.min(Math.max(1, context.getBulletRange() / context.getBulletSpeed()), 200);
        double scale = context.getBulletScale();
        double collisionHeight = scale * 0.2D;
        return BulletState.from(context).position(position.add(0.0D, collisionHeight * 0.5D, 0.0D)).lifetime(lifetime)
                .renderScale(scale).collisionWidth(scale * 0.2).collisionHeight(collisionHeight)
                .spectral(isSpectral(owner)).piercing(isPiercing(owner)).homing(isHoming(owner))
                .controllable(isControllable(owner)).controlRange(64.0D).controlSteer(0.8D).build();
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

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.BULLET.getId();
    }
}
