package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public abstract class TarotAbility extends PickupAbility{
    @Override
    public void onUse(ServerPlayer player, ItemStack stack, InteractionHand hand){

        if (PlayerHelper.hasItem(ItemId.TAROT_CLOTH.getId(), player)){
            onUseEffectS(player, stack, hand);
        }else{
            onUseEffect(player, stack, hand);
        }

        if (!player.isCreative() && stack != null){
            shrinkAfterUse(stack);
        }
    }

    /**
     * Stronger use effect, synergy for tarot cloth
     */
    public abstract void onUseEffectS(ServerPlayer player, ItemStack stack, InteractionHand hand);
}
