package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTriggerView;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public abstract class TriggerModule {
    private final CompositeTrigger trigger;

    public TriggerModule(CompositeTrigger trigger){
        this.trigger = trigger == null ? new CompositeTrigger() : trigger.copy();
    }

    public CompositeTriggerView getTriggers(ExecutableEffectContext context) {
        return trigger.getView();
    }

    public final void fire(ExecutableEffectContext context, TriggerType type) {
        getTriggers(context).fire(context, type);
    }

    /** 每一次获取模块的时候都会触发 */
    public void onAdded(LivingEntity entity){}

    /** 每一次失去该模块的时候都会触发 */
    public void onRemove(LivingEntity entity){}

    protected static double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }
}
