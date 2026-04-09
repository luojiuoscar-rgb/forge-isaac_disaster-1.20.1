package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class Justice implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER ,1.).intValue();
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_COINS, amplifier);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_HEARTS, amplifier);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_BOMBS, amplifier);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_KEYS, amplifier);
        return false;
    }
}
