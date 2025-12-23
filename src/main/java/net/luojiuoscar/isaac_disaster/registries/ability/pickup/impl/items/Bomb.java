package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class Bomb extends PickupAbility {
    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (player.level().isClientSide()) return;

        // throw bomb
        if(PlayerHelper.hasItem(ItemId.MR_MEGA.getId(), player)){
            EntityHelper.throwBomb(player, 80, 7, 1.4f);
        }else {
            EntityHelper.throwBomb(player, 80, 4);
        }

        // cd
        if(PlayerHelper.hasItem(ItemId.FAST_BOMB.getId(), player)){
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
        }else {
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 10);
        }
    }

    @Override
    public void makeSound(ServerPlayer player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

