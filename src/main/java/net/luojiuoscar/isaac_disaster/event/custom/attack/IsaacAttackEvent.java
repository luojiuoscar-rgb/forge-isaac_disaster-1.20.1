package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class IsaacAttackEvent extends Event {
    private final IBulletObject bulletObject;
    private final Entity source;
    private final ResourceLocation attackType;
    private final CompositeTrigger trigger;

    public IsaacAttackEvent(IBulletObject bulletObject, Entity source, ResourceLocation attackType,
                            CompositeTrigger trigger) {
        this.bulletObject = bulletObject;
        this.source = source;
        this.attackType = attackType;
        this.trigger = trigger;
    }

    public ResourceLocation getAttackType() {
        return attackType;
    }

    public IBulletObject getBulletObject() {
        return bulletObject;
    }

    public Entity getSource() {
        return source;
    }

    public CompositeTrigger getTrigger() {
        return trigger;
    }
}
