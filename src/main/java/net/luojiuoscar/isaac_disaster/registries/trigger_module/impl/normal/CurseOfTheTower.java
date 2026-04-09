package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CurseOfTheTower extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.CURSE_OF_THE_TOWER,
                    context -> {
                        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
                        return !PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player);
                    })
    ));

    public CurseOfTheTower() {
        super(TRIGGER);
    }
}
