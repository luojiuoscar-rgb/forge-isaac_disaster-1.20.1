package net.luojiuoscar.isaac_disaster.item_ability.pickup.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.manager.LootTableNameManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.minecraft.resources.ResourceLocation;
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
        EntityHelper.spawnLootAtPos(player, player.position(), ResourceLocation.fromNamespaceAndPath(
                IsaacDisaster.MOD_ID, LootTableNameManager.GRAB_BAG));

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 5);
    }

    @Override
    public void onUseSound(Player player) {}
}
