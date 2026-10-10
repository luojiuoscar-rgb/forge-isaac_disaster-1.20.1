package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletShootEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

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

    /** Supplies the concrete projectile state for the shared tear runtime. */
    protected abstract BulletState createOptimizedState(AttackContext context);

}
