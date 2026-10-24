package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.entity.familiar.DisposableFamiliar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Blue fly appearance and contact effect; all flight behavior lives in the fly base. */
public class BlueFlyEntity extends AbstractFlyFamiliarEntity implements DisposableFamiliar {
    public BlueFlyEntity(EntityType<? extends BlueFlyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0x4077E0;
    }

    @Override
    protected float getContactDamage(ServerPlayer owner, LivingEntity target) {
        return (float) (2.0 * owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }

    @Override
    protected void onContactHit(ServerPlayer owner, LivingEntity target, Vec3 hitPosition,
                                float attemptedDamage, boolean damageAccepted) {
        // Blue flies have no additional contact effect.
    }
}
