package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.entity.familiar.Locust;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** Black locust: one 4D contact hit without an additional contact effect. */
public final class LocustOfDeathEntity extends BlueFlyEntity implements Locust {
    public LocustOfDeathEntity(EntityType<? extends LocustOfDeathEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0x292929;
    }

    @Override
    protected float getContactDamage(ServerPlayer owner, LivingEntity target) {
        return (float) (4.0 * owner.getAttributeValue(Attributes.ATTACK_DAMAGE));
    }
}
