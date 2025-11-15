package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Set;

@Cancelable
public class IsaacAttackHitBlockEvent extends IsaacAttackEvent {
    private final BlockHitResult hit;

    public IsaacAttackHitBlockEvent(Object directSource, Entity indirectSource, int attackType, Set<Integer> hitEffectIds,
                                    BlockHitResult hit) {
        super(directSource, indirectSource, attackType, hitEffectIds);
        this.hit = hit;
    }

    public BlockHitResult getHitResult() { return hit; }
}