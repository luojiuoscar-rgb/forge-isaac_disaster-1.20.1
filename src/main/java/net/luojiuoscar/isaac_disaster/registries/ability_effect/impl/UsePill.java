package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.minecraft.server.level.ServerPlayer;

public class UsePill implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (!(context.get(ContextKeys.ITEM) instanceof Pill pill)) return;

        if (context.getEntity() instanceof ServerPlayer player){

            IPillEffect effect = PillEffectManager.getInstance().getEffectFromPill(pill.getPillId()).get();

            boolean isHorse = context.getOrDefault(ContextKeys.AMPLIFIER, 1) > 1 ||
                    pill.isHorsePill();

            effect.redirectAndUse(player, isHorse);
            effect.redirectAndMakeSound(player, isHorse);
        }
    }
}