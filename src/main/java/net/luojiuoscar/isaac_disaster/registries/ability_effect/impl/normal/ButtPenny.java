package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class ButtPenny implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        Item item = context.get(ContextKeys.ITEM);
        if (item == null) return true;

        Item tier1Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_1_ID.get());
        Item tier2Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_2_ID.get());
        Item tier3Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_3_ID.get());

        if (item.equals(tier1Coin) || item.equals(tier2Coin) || item.equals(tier3Coin)){
            if (player.getRandom().nextDouble() < 0.2){

                // 使用放屁效果
                ModAbilityEffects.FART.get().apply(context);
            }
        }

        return true;
    }
}
