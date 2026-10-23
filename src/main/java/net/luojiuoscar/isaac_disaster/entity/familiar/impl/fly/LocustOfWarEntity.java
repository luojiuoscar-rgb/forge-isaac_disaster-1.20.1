package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Red locust: ordinary fly contact followed by one friendly-filtered explosion. */
public final class LocustOfWarEntity extends BlueFlyEntity {
    public LocustOfWarEntity(EntityType<? extends LocustOfWarEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0xD04030;
    }

    @Override
    protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                float attemptedDamage, boolean damageAccepted) {
        float centerDamage = (float) (20 * StatManager.DAMAGE.getBonus());
        LevelHelper.explodeCustom(owner.serverLevel(), hitPosition,
                level().damageSources().explosion(this, owner),
                ExplosionParameters.standard(4, centerDamage).withIgnoreFriendly(true));
    }
}
