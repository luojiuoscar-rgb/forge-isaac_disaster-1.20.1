package net.luojiuoscar.isaac_disaster.effect;

import net.minecraft.world.entity.LivingEntity;

public interface IStackableEffect {
    void stack(LivingEntity entity, int count);
}
