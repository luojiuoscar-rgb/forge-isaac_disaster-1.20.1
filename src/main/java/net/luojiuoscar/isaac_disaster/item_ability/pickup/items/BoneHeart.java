package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class BoneHeart implements IPickup {

    @Override
    public int getItemId() {
        return PickupId.BONE_HEART.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        // 增加一层脆弱的心
        EntityHelper.addAmplifier(player, ModEffects.FRAGILE_HEART.get());
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = ModSounds.BONE_HEART.get();
        player.level().playSound(null, player.blockPosition(),
                sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
