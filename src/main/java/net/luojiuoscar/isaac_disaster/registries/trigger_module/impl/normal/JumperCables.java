package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.JumperCablesKillCounter;

import java.util.List;


public final class JumperCables extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.KILL_ENTITY, ModExecutableEffects.JUMPER_CABLES_KILL_COUNTER)));

    public JumperCables() { super(TRIGGER); }

    @Override
    public void onRemove(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        boolean stillOwned = entity.getCapability(EffectModulesProvider.EFFECT_MODULES)
                .map(modules -> modules.getTriggerModules().contains(ModTriggerModules.JUMPER_CABLES.getId())).orElse(false);
        if (!stillOwned) JumperCablesKillCounter.clearProgress(player);
    }
}
