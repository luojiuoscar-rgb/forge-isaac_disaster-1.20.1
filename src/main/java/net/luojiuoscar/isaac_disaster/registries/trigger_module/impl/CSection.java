package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class CSection implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.GET_ATTACK_CONTEXT,
                TriggerCategory.BEFORE_PERFORM_ATTACK
        );
    }

    @Override
    public void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {
        for (AttackContext context : event.getContexts()){
            context.addTriggerModule(ModTriggerModule.C_SECTION.getId(), 1);
        }
    }

    @Override
    public void beforePerformAttack(BeforePerformAttackEvent event, int stacks, TriggerModuleQueue queue) {
        if (event.getAttackType() == ModAttackType.BRIMSTONE.get()){
            AttackType attack = ModAttackType.C_SECTION.get();
            LivingEntity entity = event.getEntity();
            if (!(entity instanceof ServerPlayer player)) return;

            ScheduledFuncHelper.schedule("CSectionAttack", 3, 4, false, () -> {
                        attack.shoot(attack.getOneAttackContext(player, player));
                        attack.makeSound(player);
                    });
        }
    }
}
