package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class TearBulletHitBlockEvent extends IsaacAttackHitBlockEvent {
    private final TearBullet bullet;

    public TearBulletHitBlockEvent(IBulletObject directSource, Entity indirectSource, int attackType, TriggerModuleQueue triggerModuleQueue, BlockHitResult hit,
                                   TearBullet bullet) {
        super(directSource, indirectSource, attackType, triggerModuleQueue, hit);
        this.bullet = bullet;
    }

    public TearBullet getBullet() {
        return bullet;
    }
}