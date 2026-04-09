package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

public class TwoOfDiamonds implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        int value = PlayerHelper.countMoney(player);
        if (value == 0){
            PlayerHelper.giveMoney(player, 2);
        }else{
            PlayerHelper.giveMoney(player, value);
        }
        return true;
    }
}
