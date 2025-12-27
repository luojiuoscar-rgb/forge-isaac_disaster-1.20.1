package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Set;

public class CursedEye implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.ON_HURT_NEGATIVE);
    }

    @Override
    public void onHurtNegative(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player)) return;

        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    if (playerAbility.getCachedAttackType() != ModAttackType.CURSED_EYE.get()
                            || playerAbility.getChargeAmount() == 0) return;

                    playerAbility.setChargeAmount(0);
                    EntityHelper.teleportToRandomLocation(player, StatManager.getNearbyRange());
                }
        );
    }
}
