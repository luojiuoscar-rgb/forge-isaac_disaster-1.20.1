package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class GoldenHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.GOLDEN_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        int amplifier = player.getRandom().nextInt(5, 9);
        EntityHelper.addAmplifier(player, ModEffects.GILDING.get(), amplifier);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.GOLDEN_HEART.get());
    }
}
