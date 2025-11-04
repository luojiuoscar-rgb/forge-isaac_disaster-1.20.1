package net.luojiuoscar.isaac_disaster.item_ability.pickup;

import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PickupOnUseS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface IBattery extends IPickup{
    default void onUse(Player player, ItemStack stack, InteractionHand hand){
        if (player.level().isClientSide) return;

        ItemStack held = ItemStack.EMPTY;
        if (hand == InteractionHand.MAIN_HAND){
            held = player.getItemInHand(InteractionHand.OFF_HAND);
        }else if (hand == InteractionHand.OFF_HAND){
            held = player.getItemInHand(InteractionHand.MAIN_HAND);
        }

        if (held.isEmpty()){
            List<ItemStack> inv = new ArrayList<>();
            inv.addAll(player.getInventory().items);
            inv.addAll(player.getInventory().armor);
            inv.addAll(player.getInventory().offhand);

            for (ItemStack s : inv){
                if (s.getItem() instanceof ActiveItem item){
                    held = s;
                    break;
                }
            }
        }

        if (held.isEmpty()) return;

        onUseEffect(player, stack, held, hand);

        ModMessages.sentToPlayer(new PickupOnUseS2CPacket(getItemId()), (ServerPlayer) player);

        if (!player.isCreative()){
            shrinkAfterUse(stack);
        }
    }

    @Override
    default void onUseEffect(Player player, ItemStack stack, InteractionHand hand){}

    void onUseEffect(Player player, ItemStack stack, ItemStack target ,InteractionHand hand);

}
