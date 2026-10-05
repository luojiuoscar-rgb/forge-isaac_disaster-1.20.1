package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;

public class FiringModifierShotDelay implements IExecutableEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.GET_SHOT_DELAY;
    }

    @Override
    public void apply(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        if (!(context.get(ContextKeys.EVENT) instanceof GetShotDelayEvent event)) return;

        player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(playerIsaacItems -> {
            double delay = computeShotDelay(
                    event.getOriginalDelay(),
                    playerIsaacItems.getItemCountFromAll(ModPassiveItems.POLYPHEMUS.getId()) > 0,
                    playerIsaacItems.getItemCountFromAll(ModPassiveItems.THE_INNER_EYE.getId()) > 0,
                    playerIsaacItems.getItemCountFromAll(ModPassiveItems.MUTANT_SPIDER.getId()) > 0,
                    playerIsaacItems.getItemCountFromAll(ModPassiveItems.PERFECT_VISION.getId()) > 0,
                    playerIsaacItems.getItemCountFromAll(ModPassiveItems.IPECAC.getId()) > 0,
                    playerIsaacItems.getItemCountFromAll(ModPassiveItems.HAEMOLACRIA.getId()) > 0);

            event.setDelay(delay);
        });
    }

    static double computeShotDelay(double originalDelay, boolean polyphemus, boolean innerEye,
                                           boolean mutantSpider, boolean perfectVision, boolean ipecac,
                                           boolean haemolacria) {
        double delay = originalDelay;

        if (polyphemus || innerEye || mutantSpider) {
            delay = originalDelay * 2;
        }

        if (ipecac) {
            delay = originalDelay * 3;
        }

        if (perfectVision && delay > originalDelay) {
            delay = originalDelay;
        }

        if (haemolacria) {
            delay *= 2;
        }

        return delay;
    }
}
