package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class BoneHeart implements IFoodPickup {

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
        player.playSound(ModSounds.BONE_HEART.get());
    }
}
