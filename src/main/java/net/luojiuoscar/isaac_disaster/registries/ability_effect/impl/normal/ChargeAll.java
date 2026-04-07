package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

public class ChargeAll implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (context.getEntity() instanceof ServerPlayer player){
            PlayerHelper.chargeAll(player, null);
            return true;
        }
        return false;
    }
}
