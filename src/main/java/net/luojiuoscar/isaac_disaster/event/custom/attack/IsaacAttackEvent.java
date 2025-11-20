package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.manager.attack.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class IsaacAttackEvent extends Event {
    private final IBulletObject bulletObject;
    private final Entity source;
    private final int attackType;
    private final TriggerModuleQueue triggerModuleQueue;

    public IsaacAttackEvent(IBulletObject bulletObject, Entity source, int attackType, TriggerModuleQueue triggerModuleQueue) {
        this.bulletObject = bulletObject;
        this.source = source;
        this.attackType = attackType;
        this.triggerModuleQueue = triggerModuleQueue;
    }

    public int getAttackType() {
        return attackType;
    }

    public IBulletObject getBulletObject() {
        return bulletObject;
    }

    public Entity getSource() {
        return source;
    }

    public TriggerModuleQueue getHitEffects() {
        return triggerModuleQueue;
    }
}
