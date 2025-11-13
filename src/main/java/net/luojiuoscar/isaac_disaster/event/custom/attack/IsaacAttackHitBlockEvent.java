package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Set;

@Cancelable
public class IsaacAttackHitBlockEvent extends IsaacAttackEvent {
    private final BlockHitResult hit;

    public IsaacAttackHitBlockEvent(LivingEntity source, int attackType, Set<Integer> hitEffectIds,
                                    BlockHitResult hit) {
        super(source, attackType, hitEffectIds);
        this.hit = hit;
    }

    public BlockHitResult getHitResult() { return hit; }
}