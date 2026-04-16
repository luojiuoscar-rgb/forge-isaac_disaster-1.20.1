package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class Box implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_HEARTS);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_COINS);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_BOMBS);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_KEYS);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_CARDS);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_PILLS);
        LootHelper.spawnLootAtPos(entity, pos, LootTableManager.RANDOM_TRINKETS);

        return true;
    }
}
