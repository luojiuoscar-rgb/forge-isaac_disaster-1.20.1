package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IBattery;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class Battery implements IBattery {
    @Override
    public int getItemId() {
        return PickupId.BATTERY.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, ItemStack target, InteractionHand hand) {
        if (player.level().isClientSide()) return;

        PlayerHelper.chargeItem(target, 12 * ActiveItem.DAMAGE_PER_CHARGE,
                PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), (ServerPlayer) player));

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.BATTERY.get(), 1.0F, 1.0F);
    }
}
