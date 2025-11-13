package net.luojiuoscar.isaac_disaster.manager.attack.types;

import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public interface IAttackType {
    int getId();

    int getPriority();

    void performAttack(LivingEntity livingEntity, int ColorId, Set<Integer> hitEffects);
}
