package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class TearBulletShootEvent extends IsaacAttackEvent {
    private final TearBullet bullet;

    public TearBulletShootEvent(IBulletObject source, Entity indirectSource, int attackType, TriggerModuleQueue triggerModuleQueue,
                                TearBullet bullet) {
        super(source, indirectSource, attackType, triggerModuleQueue);
        this.bullet = bullet;
    }


    public TearBullet getBullet() {
        return bullet;
    }
}