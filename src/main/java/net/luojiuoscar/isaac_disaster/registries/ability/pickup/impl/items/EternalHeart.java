package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.FoodPickupAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class EternalHeart extends FoodPickupAbility {

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        EntityHelper.applyOrStackEffect(player, ModEffects.ETERNAL_HEART.get(), 3600, 0, false, true);

    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.ETERNAL_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}

