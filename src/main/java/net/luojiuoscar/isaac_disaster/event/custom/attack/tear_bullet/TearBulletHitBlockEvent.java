package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Set;

@Cancelable
public class TearBulletHitBlockEvent extends IsaacAttackHitBlockEvent {
    private final TearBullet bullet;

    public TearBulletHitBlockEvent(Object directSource, Entity indirectSource, int attackType, Set<Integer> hitEffectIds, BlockHitResult hit,
                                   TearBullet bullet) {
        super(directSource, indirectSource, attackType, hitEffectIds, hit);
        this.bullet = bullet;
    }

    public TearBullet getBullet() {
        return bullet;
    }
}