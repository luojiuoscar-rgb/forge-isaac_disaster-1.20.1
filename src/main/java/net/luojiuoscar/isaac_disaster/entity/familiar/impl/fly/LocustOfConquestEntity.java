package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.luojiuoscar.isaac_disaster.entity.familiar.Locust;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** White locust: an individual deals 2D; group size is a responsibility of its spawn source. */
public final class LocustOfConquestEntity extends BlueFlyEntity implements Locust {
    public LocustOfConquestEntity(EntityType<? extends LocustOfConquestEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getBodyColor() {
        return 0xF2F2EA;
    }
}
