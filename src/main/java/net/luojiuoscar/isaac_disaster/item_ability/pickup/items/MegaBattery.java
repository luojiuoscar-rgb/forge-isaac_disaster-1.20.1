package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IBattery;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class MegaBattery implements IBattery {
    @Override
    public int getItemId() {
        return PickupId.MEGA_BATTERY.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, ItemStack target, InteractionHand hand) {
        if (player.level().isClientSide()) return;

        PlayerHelper.chargeItem(target, 24 * ActiveItem.DAMAGE_PER_CHARGE_RATE, true);

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(ModSounds.BATTERY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

