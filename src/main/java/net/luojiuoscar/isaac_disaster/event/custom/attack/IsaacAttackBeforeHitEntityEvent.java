package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacAttackBeforeHitEntityEvent extends IsaacAttackEvent {
    private final EntityHitResult hit;
    private double damage;

    public IsaacAttackBeforeHitEntityEvent(IBulletObject bulletObject, Entity source, ResourceLocation attackType,
                                           CompositeTrigger triggers,
                                           EntityHitResult hit, float damage) {
        super(bulletObject, source, attackType, triggers);
        this.hit = hit;
        this.damage = damage;
    }

    public EntityHitResult getHit() { return hit; }

    public double getDamage() { return damage; }

    public void setDamage(double damage) { this.damage = damage; }
}
