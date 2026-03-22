package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import java.util.List;

public class CompositeAbilityEffect implements IAbilityEffect {

    private final List<IAbilityEffect> effects;

    public CompositeAbilityEffect(List<IAbilityEffect> effects) {
        this.effects = effects;
    }

    @Override
    public void apply(AbilityEffectContext context) {
        for (IAbilityEffect effect : effects) {
            effect.apply(context);
        }
    }
}