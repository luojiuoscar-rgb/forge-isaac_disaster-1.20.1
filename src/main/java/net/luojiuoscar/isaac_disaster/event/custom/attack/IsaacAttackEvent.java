package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

import java.util.Set;

public class IsaacAttackEvent extends Event {
    private final Object directSource;
    private final Entity indirectSource;
    private final int attackType;
    private final Set<Integer> hitEffects;

    public IsaacAttackEvent(Object directSource, Entity indirectSource, int attackType, Set<Integer> hitEffectIds) {
        this.directSource = directSource;
        this.indirectSource = indirectSource;
        this.attackType = attackType;
        this.hitEffects = hitEffectIds;
    }

    public int getAttackType() {
        return attackType;
    }

    public Object getDirectSource() {
        return directSource;
    }

    public Entity getIndirectSource() {
        return indirectSource;
    }

    public Set<Integer> getHitEffects() {
        return hitEffects;
    }
}
