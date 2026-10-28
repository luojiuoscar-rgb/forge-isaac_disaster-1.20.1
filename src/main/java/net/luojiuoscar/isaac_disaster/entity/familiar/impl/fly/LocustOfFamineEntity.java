package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.entity.familiar.Locust;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Yellow locust: ordinary fly contact damage and a brief movement slow. */
public final class LocustOfFamineEntity extends BlueFlyEntity implements Locust {
    public LocustOfFamineEntity(EntityType<? extends LocustOfFamineEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0xD9BD42;
    }

    @Override
    protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                float attemptedDamage, boolean damageAccepted) {
        if (!damageAccepted || !target.isAlive()) return;
        // Wiki specifies a slow without duration; this mod uses two seconds of Slowness II.
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1), owner);
    }
}
