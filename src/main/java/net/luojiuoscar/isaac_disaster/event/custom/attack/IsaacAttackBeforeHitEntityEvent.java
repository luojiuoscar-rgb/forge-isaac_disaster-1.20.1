package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.Set;

@Cancelable
public class IsaacAttackBeforeHitEntityEvent extends IsaacAttackEvent {
    private final EntityHitResult hit;
    private double damage;

    public IsaacAttackBeforeHitEntityEvent(LivingEntity source, int attackType, Set<Integer> hitEffectIds,
                                           EntityHitResult hit, float damage) {
        super(source, attackType, hitEffectIds);
        this.hit = hit;
        this.damage = damage;
    }

    public EntityHitResult getHit() { return hit; }

    public double getDamage() { return damage; }

    public void setDamage(double damage) { this.damage = damage; }
}
