package net.luojiuoscar.isaac_disaster.registries.ability.pickup.impl.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.BatteryAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;


public class MegaBattery extends BatteryAbility {
    @Override
    public void chargeItem(ServerPlayer player, ItemStack stack, ItemStack target, InteractionHand hand) {
        PlayerHelper.chargeItem(target, 24 * ActiveItem.DAMAGE_PER_CHARGE_RATE, true);

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BATTERY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

