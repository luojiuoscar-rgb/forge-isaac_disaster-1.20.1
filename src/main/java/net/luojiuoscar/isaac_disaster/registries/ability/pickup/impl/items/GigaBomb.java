package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class GigaBomb extends PickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (player.level().isClientSide()) return;

        EntityHelper.throwGigaBomb(player, 120);

        // cd
        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 50);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

