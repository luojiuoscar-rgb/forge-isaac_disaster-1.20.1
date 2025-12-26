package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BeforePerformAttackEvent extends Event {
    private final LivingEntity entity;
    private final AttackType attackType;

    public BeforePerformAttackEvent(LivingEntity entity, AttackType attackType){
        this.entity = entity;
        this.attackType = attackType;
    }

    public AttackType getAttackType() {
        return attackType;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
