package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class HalfSoulHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.HALF_SOUL_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        StatManager.gainAbsorption(player, 0.5f);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.SOUL_HEART.get());
    }
}
