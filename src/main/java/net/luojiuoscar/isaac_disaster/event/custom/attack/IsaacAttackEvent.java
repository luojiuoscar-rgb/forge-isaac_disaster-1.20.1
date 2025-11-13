package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

public class IsaacAttackEvent extends Event {
    private final Entity directSource;
    private final int attackType;
    private final Set<Integer> hitEffects;

    public IsaacAttackEvent(Entity entity, int attackType, Set<Integer> hitEffectIds) {
        this.directSource = entity;
        this.attackType = attackType;
        this.hitEffects = hitEffectIds;
    }

    public int getAttackType() {
        return attackType;
    }

    public Entity getDirectSource() {
        return directSource;
    }

    public Set<Integer> getHitEffects() {
        return hitEffects;
    }
}
