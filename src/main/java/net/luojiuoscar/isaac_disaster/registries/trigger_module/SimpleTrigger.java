package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Predicate;

/**
 * 记录type->effect
 * 一个triggerModule可以持有多个simpleTrigger
 */
public class SimpleTrigger {
    public final TriggerType type;
    public final RegistryObject<IAbilityEffect> effect;
    private final Predicate<AbilityEffectContext> condition;

    public SimpleTrigger(TriggerType type,
                         RegistryObject<IAbilityEffect> effect,
                         Predicate<AbilityEffectContext> condition) {
        this.type = type;
        this.effect = effect;
        this.condition = condition != null ? condition : ctx -> true;
    }

    public SimpleTrigger(TriggerType type, RegistryObject<IAbilityEffect> effect){
        this(type, effect, null);
    }

    private boolean shouldFire(AbilityEffectContext context) {
        return condition.test(context);
    }

    public void fire(AbilityEffectContext context){
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
