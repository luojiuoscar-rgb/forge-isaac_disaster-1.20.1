package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;


/**
 * Spawn the specified item through loot generation using LOOT_COUNT.
 */
public class GiveItemViaLoot implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        Item item = context.get(ContextKeys.ITEM);
        if (item == null) return false;

        int lootCount = Math.max(0, context.getOrDefault(ContextKeys.LOOT_COUNT, 1));
        LootHelper.spawnItemViaLoot(entity, pos, item, lootCount);
        return true;
    }
}
