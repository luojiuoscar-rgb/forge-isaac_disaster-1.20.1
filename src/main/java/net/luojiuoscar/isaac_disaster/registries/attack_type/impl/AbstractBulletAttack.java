package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractBulletAttack extends AttackType {

    @Override
    public ResourceLocation getRootId() {
        return ModAttackTypes.BULLET.getId();
    }

    protected AbstractBulletAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    protected AbstractBulletAttack(double priority) {
        super(priority);
    }

    // =================== 子弹发射 ===================
    @Override
    public void shoot(AttackContext ctx) {
        spawnBullet(ctx);
    }

    protected boolean spawnBullet(AttackContext ctx) {
        LivingEntity owner = ctx.getOwner();
        if (owner == null || owner.level().isClientSide()) return false;
        if (owner.level() instanceof ServerLevel level && !BulletRuntime.INSTANCE.canSpawn(level)) return false;

        BulletState optimized = createOptimizedState(ctx);
        if (optimized != null && owner.level() instanceof ServerLevel level) {
            return BulletRuntime.INSTANCE.spawn(level, optimized) != null;
        }

        IsaacDisaster.LOGGER.warn("Discarded optimized bullet for {}", getId());
        return false;
    }

    /** Supplies the concrete projectile state for the shared tear runtime. */
    protected abstract BulletState createOptimizedState(AttackContext context);

}
