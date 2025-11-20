package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Set;

public class SwallowedPenny implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.ON_HURT);
    }

    @Override
    public void onHurt(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {
        LivingEntity entity = event.getEntity();
        if (entity.getRandom().nextDouble() < Math.min(0.7, 0.35 * stacks)){
            LootHelper.spawnLootAtPos(entity, entity.blockPosition().getCenter(), LootTableManager.RANDOM_COINS);
        }
    }
}
