package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.util.Set;

public class CursedPenny implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.PICKUP_ITEM);
    }

    @Override
    public void onPickupItem(EntityItemPickupEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Item item = event.getItem().getItem().getItem();

        Item tier1Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_1_ID.get());
        Item tier2Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_2_ID.get());
        Item tier3Coin = PlayerHelper.getItemFromConfig(Config.COIN_TIER_3_ID.get());

        if (item.equals(tier1Coin) || item.equals(tier2Coin) || item.equals(tier3Coin)){
            if (event.getEntity().getRandom().nextDouble() < 0.1){

                EntityHelper.teleportToRandomLocation(player, 8);

                player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

    }
}
