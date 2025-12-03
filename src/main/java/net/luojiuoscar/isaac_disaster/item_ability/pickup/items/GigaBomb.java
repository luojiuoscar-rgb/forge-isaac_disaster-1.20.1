package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GigaBomb implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.GIGA_BOMB.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        if (player.level().isClientSide()) return;

        EntityHelper.throwGigaBomb(player, 120);

        // cd
        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 50);
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

