package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.BatteryAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class SmallBattery extends BatteryAbility {
    @Override
    public void chargeItem(ServerPlayer player, ItemStack stack, ItemStack target, InteractionHand hand) {

        PlayerHelper.chargeItem(target, 4 * ActiveItem.DAMAGE_PER_CHARGE_RATE,
                PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), player));

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BATTERY_SMALL.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

