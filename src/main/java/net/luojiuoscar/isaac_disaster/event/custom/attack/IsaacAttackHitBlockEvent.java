package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

@Cancelable
public class IsaacAttackHitBlockEvent extends IsaacAttackEvent {
    private final BlockHitResult hit;

    public IsaacAttackHitBlockEvent(IBulletObject bulletObject, Entity source, ResourceLocation attackType,
                                    List<SimpleTrigger> triggers,
                                    BlockHitResult hit) {
        super(bulletObject, source, attackType, triggers);
        this.hit = hit;
    }

    public BlockHitResult getHitResult() { return hit; }
}