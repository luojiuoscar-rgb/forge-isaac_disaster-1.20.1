package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.item.ModTrinkets;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.TrinketId;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class PlayerPermanentModule extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.NECRONMICON_SHIELD_ACTIVE,
                    context -> context.getEntity().hasEffect(ModEffects.NECRONMICON_SHIELD.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.GILDING_ACTIVE,
                    context -> context.getEntity().hasEffect(ModEffects.GILDING.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.ADULT_SET,
                    context -> context.getEntity() instanceof ServerPlayer player
                            && PlayerHelper.hasSet(ModSetAbilities.ADULT.getId(), player)),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.FRAGILE_HEART_ACTIVE,
                    context -> context.getEntity().hasEffect(ModEffects.FRAGILE_HEART.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.CURSE_OF_THE_MAZE,
                    context -> context.getEntity().hasEffect(ModEffects.CURSE_OF_THE_MAZE.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.DROP_PERFECTION,
                              context -> context.getEntity() instanceof ServerPlayer player
                                      && PlayerHelper.hasTrinket(ModTrinkets.PERFECTION.getId(), player))
            ));

    public PlayerPermanentModule() {
        super(TRIGGER);
    }
}
