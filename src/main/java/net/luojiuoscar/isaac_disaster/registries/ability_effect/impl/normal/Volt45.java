package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class Volt45 implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var amounts = context.get(ContextKeys.DOUBLE);
        if (amounts == null || amounts.isEmpty()) return false;
        double amount = amounts.get(0);
        int rate = GameplayConfig.ACTIVE_ITEM_DURABILITY_RESTORE_RATE.get() / 2;
        if (context.get(ContextKeys.EVENT) instanceof LivingAttackEvent event
                && event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            rate *= 4;
        }
        PlayerHelper.chargeFirst(player, Math.max((int) amount * rate, 1));
        return true;
    }
}
