package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PlayerPermanentModule implements ITriggerModule {
    private static final CompositeTrigger TRIGGERS = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.NECRONMICON_SHIELD_ACTIVE,
                    context -> context.getEntity().hasEffect(ModEffects.NECRONMICON_SHIELD.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.GILDING_ACTIVE,
                    context -> context.getEntity().hasEffect(ModEffects.GILDING.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.ADULT_SET,
                    context -> context.getEntity() instanceof ServerPlayer player
                            && PlayerHelper.hasSet(ModSetAbility.ADULT.getId(), player)),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.FRAGILE_HEART_ACTIVE,
                    context -> context.getEntity().hasEffect(ModEffects.FRAGILE_HEART.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.CURSE_OF_THE_MAZE,
                    context -> context.getEntity().hasEffect(ModEffects.CURSE_OF_THE_MAZE.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.DROP_PERFECTION,
                              context -> context.getEntity() instanceof ServerPlayer player
                                      && PlayerHelper.hasTrinket(TrinketId.PERFECTION.getId(), player))
            ));

    @Override
    public CompositeTrigger getTriggers() {
        return TRIGGERS;
    }
}
