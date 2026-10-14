package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;

public final class KeeperCoinDrop implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var position = context.get(ContextKeys.TARGET_POSITION);
        if (position == null) return false;
        var coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_1_ID.get());
        if (coin == null) return false;
        LootHelper.spawnItemViaLoot(context.getEntity(), position, coin, 1);
        return true;
    }
}
