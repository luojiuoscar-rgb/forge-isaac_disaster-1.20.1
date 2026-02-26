package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.util.List;
import java.util.Set;

public class ButtPenny implements ITriggerModule {
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
            if (event.getEntity().getRandom().nextDouble() < 0.2){

                // 施加中毒
                List<LivingEntity> entities = LevelHelper.selectBySphere(player.level(),
                        player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange());

                for (LivingEntity entity : entities){
                    if (EntityHelper.isFriendly(entity, player)) continue;

                    entity.addEffect(new MobEffectInstance(
                            MobEffects.POISON,
                            120,
                            0
                    ));
                }

                player.playNotifySound(ModSounds.FART_NORMAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

    }
}
