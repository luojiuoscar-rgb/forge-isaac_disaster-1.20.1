package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class BlendedHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.BLENDED_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        double health = player.getMaxHealth() - player.getHealth();
        if ( health >= StatManager.getHealthBonus() * 0.5){
            PickupManager.getInstance().getItemFromId(PickupId.RED_HEART.getId()).onUseEffect(player, stack, hand);

        } else if (health > 0) {
            PickupManager.getInstance().getItemFromId(PickupId.HALF_SOUL_HEART.getId()).onUseEffect(player, stack, hand);
            PickupManager.getInstance().getItemFromId(PickupId.HALF_RED_HEART.getId()).onUseEffect(player, stack, hand);

        }else {
            PickupManager.getInstance().getItemFromId(PickupId.SOUL_HEART.getId()).onUseEffect(player, stack, hand);
        }
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = ModSounds.SOUL_HEART.get();
        player.level().playSound(null, player.blockPosition(),
                sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
