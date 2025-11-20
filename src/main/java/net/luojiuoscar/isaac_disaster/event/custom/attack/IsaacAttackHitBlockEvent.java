package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.manager.attack.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacAttackHitBlockEvent extends IsaacAttackEvent {
    private final BlockHitResult hit;

    public IsaacAttackHitBlockEvent(IBulletObject bulletObject, Entity source, int attackType, TriggerModuleQueue triggerModules,
                                    BlockHitResult hit) {
        super(bulletObject, source, attackType, triggerModules);
        this.hit = hit;
    }

    public BlockHitResult getHitResult() { return hit; }
}