package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;

public class LootMoney implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (context.getEntity() instanceof Player player){
            int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1);
            LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_COINS, amplifier);
            return true;
        }
        return false;
    }
}
