package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacAttackAfterHitEvent extends IsaacAttackEvent {
    private final EntityHitResult hit;
    private double damage;
    private final double targetHealth;

    public IsaacAttackAfterHitEvent(IBulletObject bulletObject, Entity source, ResourceLocation attackTypeId,
                                    CompositeTrigger triggers,
                                    EntityHitResult hit, double damage, double targetHealth) {
        super(bulletObject, source, attackTypeId, triggers);
        this.hit = hit;
        this.damage = damage;
        this.targetHealth = targetHealth;
    }

    public EntityHitResult getHitResult() { return hit; }

    public double getDamage() { return damage; }
    public double getTargetHealth() { return targetHealth; }

    public void setDamage(double damage) { this.damage = damage; }
}
