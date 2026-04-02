package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

@Cancelable
public class IsaacAttackEvent extends Event {
    private final IBulletObject bulletObject;
    private final Entity source;
    private final ResourceLocation attackType;
    private final List<SimpleTrigger> triggers;

    public IsaacAttackEvent(IBulletObject bulletObject, Entity source, ResourceLocation attackType,
                            List<SimpleTrigger> triggers) {
        this.bulletObject = bulletObject;
        this.source = source;
        this.attackType = attackType;
        this.triggers = triggers;
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

    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }
}
