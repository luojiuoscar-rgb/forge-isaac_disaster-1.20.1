package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ChargeItemViaBattery implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var amounts = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (amounts.isEmpty()) return false;
        int chargeAmount = amounts.get(0).intValue();
        var flags = context.getOrDefault(ContextKeys.BOOLEAN, List.of());
        boolean canOverCharge = (!flags.isEmpty() && flags.get(0))
                || PlayerHelper.hasItem(ModPassiveItems.THE_BATTERY.getId(), player);
        PlayerHelper.chargeFirst(player, chargeAmount * ActiveItem.DAMAGE_PER_CHARGE_RATE, canOverCharge);
        return true;
    }
}
