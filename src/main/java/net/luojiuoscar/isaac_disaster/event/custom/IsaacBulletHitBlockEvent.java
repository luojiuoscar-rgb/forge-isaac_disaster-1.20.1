package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.IsaacBullet;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacBulletHitBlockEvent extends IsaacBulletEvent {
    private final BlockHitResult hit;
    public IsaacBulletHitBlockEvent(IsaacBullet b, BlockHitResult hit) { super(b); this.hit = hit; }
    public BlockHitResult getHit() { return hit; }
}