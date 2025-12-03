package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.effect.custom.FragileHeartEffect;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IFoodPickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PickupId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class BoneHeart implements IFoodPickup {

    @Override
    public int getItemId() {
        return PickupId.BONE_HEART.getId();
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        // 增加一层脆弱的心
        FragileHeartEffect.stack(player, 1);
    }

    @Override
    public void onUseSound(Player player) {
        player.playNotifySound(ModSounds.BONE_HEART.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
