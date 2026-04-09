package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class AbilityEffectEntry implements IExecutableEffect {
    private final RegistryObject<IExecutableEffect> effect;
    private final Consumer<ExecutableEffectContext> config;

    public AbilityEffectEntry(RegistryObject<IExecutableEffect> effect,
                              Consumer<ExecutableEffectContext> config) {
        this.effect = effect;
        this.config = config;
    }

    @Override
    public void apply(ExecutableEffectContext context) {
        config.accept(context);   // 写入参数后调用
        effect.get().apply(context);
    }
}
