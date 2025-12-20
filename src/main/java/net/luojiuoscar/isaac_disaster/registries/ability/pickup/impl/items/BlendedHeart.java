package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.ModPickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class BlendedHeart extends PickupAbility {

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        double health = player.getMaxHealth() - player.getHealth();
        if ( health >= StatManager.MAX_HEALTH.getBonus() * 0.5){
            ModPickupAbility.RED_HEART.get().onUseEffect(player, stack, hand);
        } else if (health > 0) {
            ModPickupAbility.HALF_RED_HEART.get().onUseEffect(player, stack, hand);
            ModPickupAbility.HALF_SOUL_HEART.get().onUseEffect(player, stack, hand);
        }else {
            ModPickupAbility.SOUL_HEART.get().onUseEffect(player, stack, hand);
        }
    }

    @Override
    public void makeSound(ServerPlayer player) {
        double health = player.getMaxHealth() - player.getHealth();
        if (health > 0) {
            player.playNotifySound(ModSounds.RED_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }else {
            player.playNotifySound(ModSounds.SOUL_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}

