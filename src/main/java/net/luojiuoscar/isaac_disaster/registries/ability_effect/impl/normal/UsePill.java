package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;

import java.util.List;

public class UsePill implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.ITEM) instanceof Pill pill)) return false;

        PillEffect effect = (PillEffect) PillEffectManager.getInstance().getEffectFromPill(pill.getPillId()).get();

        boolean isHorse = context.getOrDefault(ContextKeys.AMPLIFIER, 1.) > 1 ||
                pill.isHorsePill();
        context.set(ContextKeys.BOOLEAN, List.of(isHorse));

        effect.apply(context);
        return true;
    }
}