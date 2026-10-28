package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

public class SwallowedPenny implements IAbilityEffect {
    public static double getChance(boolean enchanted) {
        return enchanted ? 0.7 : 0.35;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        double chance = PlayerHelper.getValueFromTrinket(getChance(false), getChance(true),
                ModTrinkets.SWALLOWED_PENNY.getId(), player);
        if (player.getRandom().nextDouble() < chance){
            LootHelper.spawnLootAtPos(player, context.getOrDefault(ContextKeys.TARGET_POSITION, player.position()),
                    ModLootTables.RANDOM_COINS, Math.max(0, context.getOrDefault(ContextKeys.COUNT, 1)));
        }

        return true;
    }
}
