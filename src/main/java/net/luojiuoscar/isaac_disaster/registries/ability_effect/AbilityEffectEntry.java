package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class AbilityEffectEntry implements IExecutableEffect {
    private final RegistryObject<IExecutableEffect> effect;
    private final Consumer<AbilityEffectContext> config;

    public AbilityEffectEntry(RegistryObject<IExecutableEffect> effect,
                              Consumer<AbilityEffectContext> config) {
        this.effect = effect;
        this.config = config;
    }

    @Override
    public void apply(AbilityEffectContext context) {
        config.accept(context);   // 写入参数后调用
        effect.get().apply(context);
    }
}
