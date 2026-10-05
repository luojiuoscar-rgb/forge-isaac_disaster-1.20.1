package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.BulletAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class BulletAttack extends AttackType {
    @Override public ResourceLocation getRootId() { return ModAttackTypes.BULLET.getId(); }
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

        BulletState optimized = createOptimizedState(ctx);
        if (optimized != null && owner.level() instanceof ServerLevel level) {
            TearBulletShootEvent event = new TearBulletShootEvent(optimized, owner, getId(), optimized.getTriggers());
            if (!MinecraftForge.EVENT_BUS.post(event)) BulletRuntime.INSTANCE.spawn(level, optimized);
            return;
        }
        IsaacDisaster.LOGGER.warn("Discarded optimized bullet for {}", getId());
    }

    /** Builds the entity-free state used by ordinary and split-created tear attacks. */
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

}
