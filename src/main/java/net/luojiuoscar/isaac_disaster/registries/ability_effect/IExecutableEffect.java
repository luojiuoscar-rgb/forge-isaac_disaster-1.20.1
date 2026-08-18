package net.luojiuoscar.isaac_disaster.registries.ability_effect;

public interface IExecutableEffect {
    IExecutableEffect EMPTY = context -> {};

    void apply(ExecutableEffectContext context);
}
