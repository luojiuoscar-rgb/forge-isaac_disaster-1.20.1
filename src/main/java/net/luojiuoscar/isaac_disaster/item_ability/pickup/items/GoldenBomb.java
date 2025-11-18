package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class GoldenBomb implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.GOLDEN_BOMB.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        if (player.level().isClientSide()) return;

        // throw bomb
        if(PlayerHelper.hasItem(ItemId.MR_MEGA.getId(), (ServerPlayer) player)){
            EntityHelper.throwBomb(player, 80, 7, 1.4f);
        }else{
            EntityHelper.throwBomb(player, 80, 4);
        };

        // cd
        if(PlayerHelper.hasItem(ItemId.FAST_BOMB.getId(), (ServerPlayer) player)){
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 20);
        }else {
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 100);
        }
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playSound(sound, 1.0F, 1.0F);
    }

    @Override
    public void shrinkAfterUse(ItemStack stack){
    }
}
