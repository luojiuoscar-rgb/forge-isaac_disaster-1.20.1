package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.id.PickupId;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class GrabBag implements IPickup {
    @Override
    public int getItemId() {
        return PickupId.GRAB_BAG.getId();
    }

    @Override
    public void onUseEffect(Player player, ItemStack stack, InteractionHand hand) {
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.GRAB_BAG);

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
    }

    @Override
    public void onUseSound(Player player) {}
}
