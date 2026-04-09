package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;

public class CursedPenny implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        Item item = context.getOrDefault(ContextKeys.ITEM, null);
        if (item == null) return true;

        Item tier1Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_1_ID.get());
        Item tier2Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_2_ID.get());
        Item tier3Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_3_ID.get());

        if (item.equals(tier1Coin) || item.equals(tier2Coin) || item.equals(tier3Coin)){
            if (player.getRandom().nextDouble() < 0.1){

                EntityHelper.teleportToRandomLocation(player, 8);

                player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        return true;
    }
}
