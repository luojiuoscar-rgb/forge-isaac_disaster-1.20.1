package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import java.util.List;

public class CompositeExecutableEffect implements IExecutableEffect {

    private final List<IExecutableEffect> effects;

    public CompositeExecutableEffect(List<IExecutableEffect> effects) {
        this.effects = effects;
    }

    @Override
    public void apply(ExecutableEffectContext context) {
        for (IExecutableEffect effect : effects) {
            effect.apply(context);
        }
    }
}