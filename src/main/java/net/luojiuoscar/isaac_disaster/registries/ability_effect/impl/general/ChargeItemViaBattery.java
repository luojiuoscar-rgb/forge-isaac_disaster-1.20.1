package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChargeItemViaBattery implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        InteractionHand hand = context.get(ContextKeys.HAND);
        if (hand == null) return false;
        var nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (nums.isEmpty()) return false;
        int chargeAmount = nums.get(0).intValue();

        var bools = context.getOrDefault(ContextKeys.BOOLEAN, List.of());
        boolean canOverCharge = false;
        if (!bools.isEmpty()){
            canOverCharge = bools.get(0);
        }

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

        if (held.isEmpty()) return false;

        PlayerHelper.chargeItem(held, chargeAmount * ActiveItem.DAMAGE_PER_CHARGE_RATE,
                canOverCharge || PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), player));

        return true;
    }
}
