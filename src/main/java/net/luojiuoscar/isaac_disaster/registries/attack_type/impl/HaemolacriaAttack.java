package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.resources.ResourceLocation;

/** Ordinary tear firing with a distinct priority for Haemolacria compatibility. */
public final class HaemolacriaAttack extends BulletAttack {
    public HaemolacriaAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.HAEMOLACRIA.getId();
    }

    @Override
    public ResourceLocation getRootId() {
        return ModAttackTypes.BULLET.getId();
    }
}
