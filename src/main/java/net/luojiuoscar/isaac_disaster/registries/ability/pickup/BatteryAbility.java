package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class BatteryAbility extends PickupAbility {
    @Override
    public void onUse(ServerPlayer player, ItemStack stack, InteractionHand hand){

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
                if (s.getItem() instanceof ActiveItem){
                    held = s;
                    break;
                }
            }
        }

        if (held.isEmpty()) return;

        chargeItem(player, stack, held, hand);

        if (!player.isCreative()){
            shrinkAfterUse(stack);
        }
    }

    @Override
    public void onUseEffect(ServerPlayer player, ItemStack stack, InteractionHand hand){}

    public abstract void chargeItem(ServerPlayer player, ItemStack stack, ItemStack target , InteractionHand hand);


}
