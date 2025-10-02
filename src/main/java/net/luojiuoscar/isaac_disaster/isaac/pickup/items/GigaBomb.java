package net.luojiuoscar.isaac_disaster.isaac.pickup.items;

import net.luojiuoscar.isaac_disaster.isaac.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.Objects;

public class GigaBomb implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.GIGA_BOMB.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        // 只在服务端执行实体操作
        if (player.level().isClientSide()) {
            return;
        }

        IsaacBomb.throwBomb(player, 120, 16, 2);

        // cd
        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 10);

        // 物品-1
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }
    }

    @Override
    public void onUseSound(Player player) {
        SoundEvent sound = SoundEvents.TNT_PRIMED;
        player.playSound(sound, 1.0F, 1.0F);
    }
}
