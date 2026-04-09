package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Predicate;

/**
 * 记录type->effect
 * 一个triggerModule可以持有多个simpleTrigger
 */
public class SimpleTrigger {
    public final TriggerType type;
    public final RegistryObject<IExecutableEffect> effect;
    private final Predicate<ExecutableEffectContext> condition;

    public SimpleTrigger(TriggerType type,
                         RegistryObject<IExecutableEffect> effect,
                         Predicate<ExecutableEffectContext> condition) {
        this.type = type;
        this.effect = effect;
        this.condition = condition != null ? condition : ctx -> true;
    }

    public SimpleTrigger(TriggerType type, RegistryObject<IExecutableEffect> effect){
        this(type, effect, null);
    }

    private boolean shouldFire(ExecutableEffectContext context) {
        return condition.test(context);
    }

    public void fire(ExecutableEffectContext context){
        if (shouldFire(context)) {
            effect.get().apply(context);
        }
    }

    public TriggerType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "SimpleTrigger: " + type + " -> " + effect.get();
    }
}
