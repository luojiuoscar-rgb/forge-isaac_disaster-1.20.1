package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Set;

public class Cartridge implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.ON_HURT);
    }

    @Override
    public void onHurt(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {
        LivingEntity entity = event.getEntity();
        if (entity.getRandom().nextDouble() < stacks / Math.max(stacks, 20 - getLuck(entity) * 0.5)){
            entity.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), 120));
        }
    }
}
