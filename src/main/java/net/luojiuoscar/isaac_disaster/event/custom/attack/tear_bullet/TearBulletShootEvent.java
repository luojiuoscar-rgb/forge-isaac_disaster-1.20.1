package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Set;

@Cancelable
public class TearBulletShootEvent extends IsaacAttackEvent {
    private final TearBullet bullet;

    public TearBulletShootEvent(LivingEntity entity, int attackType, Set<Integer> hitEffectIds,
                                TearBullet bullet) {
        super(entity, attackType, hitEffectIds);
        this.bullet = bullet;
    }


    public TearBullet getBullet() {
        return bullet;
    }
}