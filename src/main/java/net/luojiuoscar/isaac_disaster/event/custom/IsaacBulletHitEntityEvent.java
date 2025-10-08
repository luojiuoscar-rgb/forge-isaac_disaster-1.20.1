package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.IsaacBullet;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacBulletHitEntityEvent extends IsaacBulletEvent {
    private final EntityHitResult hit;
    public IsaacBulletHitEntityEvent(IsaacBullet b, EntityHitResult hit) { super(b); this.hit = hit; }
    public EntityHitResult getHit() { return hit; }
}