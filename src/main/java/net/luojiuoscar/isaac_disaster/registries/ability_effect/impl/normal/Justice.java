package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class Justice implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        int lootCount = Math.max(0, context.getOrDefault(ContextKeys.LOOT_COUNT, 1));
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        LootHelper.spawnLootAtPos(entity, pos, ModLootTables.RANDOM_COINS, lootCount);
        LootHelper.spawnLootAtPos(entity, pos, ModLootTables.RANDOM_HEARTS, lootCount);
        LootHelper.spawnLootAtPos(entity, pos, ModLootTables.RANDOM_BOMBS, lootCount);
        LootHelper.spawnLootAtPos(entity, pos, ModLootTables.RANDOM_KEYS, lootCount);
        return false;
    }
}
