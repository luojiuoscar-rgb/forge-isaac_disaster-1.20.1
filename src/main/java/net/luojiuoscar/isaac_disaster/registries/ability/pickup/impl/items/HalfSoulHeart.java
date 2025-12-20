package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.FoodPickupAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class HalfSoulHeart extends FoodPickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        StatManager.gainAbsorption(player, 0.5f);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.SOUL_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

