package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class EternalHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.ETERNAL_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        EntityHelper.applyOrStackEffect(player, ModEffects.ETERNAL_HEART.get(), 3600, 0, false, true);

    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.ETERNAL_HEART.get());
    }
}
