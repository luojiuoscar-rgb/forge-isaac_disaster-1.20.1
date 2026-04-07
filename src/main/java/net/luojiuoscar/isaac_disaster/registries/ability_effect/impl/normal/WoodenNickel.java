package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

public class WoodenNickel implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();

        for (int i = 0; i < context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue(); i++){
            if (Math.random() < 0.6 && !entity.level().isClientSide){
                LootHelper.spawnLootAtPos(
                        entity,
                        entity.blockPosition().getCenter(),
                        LootTableManager.RANDOM_COINS);
            }
        }
        return true;
    }
}