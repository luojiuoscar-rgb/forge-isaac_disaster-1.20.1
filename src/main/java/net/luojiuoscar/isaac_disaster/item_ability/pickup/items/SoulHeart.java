package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class SoulHeart implements IPickup {

    @Override
    public int getItemId() {
        return PickupId.SOUL_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        StatManager.gainAbsorption(player, 1);
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = ModSounds.SOUL_HEART.get();
        player.level().playSound(null, player.blockPosition(),
                sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
