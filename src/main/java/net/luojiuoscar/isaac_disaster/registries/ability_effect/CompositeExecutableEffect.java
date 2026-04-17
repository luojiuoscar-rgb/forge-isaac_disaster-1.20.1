package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import java.util.ArrayList;
import java.util.List;

public class CompositeExecutableEffect implements IExecutableEffect {
    public static final CompositeExecutableEffect EMPTY = new CompositeExecutableEffect();

    private final List<IExecutableEffect> effects;

    public CompositeExecutableEffect(){
        this.effects = new ArrayList<>();
    }

    public CompositeExecutableEffect(List<IExecutableEffect> effects) {
        this.effects = new ArrayList<>(effects);
    }

    @Override
    public void apply(ExecutableEffectContext context) {
        for (IExecutableEffect effect : effects) {
            effect.apply(context);
        }
    }

    public void add(IExecutableEffect effect){
        this.effects.add(effect);
    }

    public void remove(IExecutableEffect effect){
        this.effects.remove(effect);
    }

    public void clear(){
        this.effects.clear();
    }

    public boolean contains(IExecutableEffect effect){
        return this.effects.contains(effect);
    }
}