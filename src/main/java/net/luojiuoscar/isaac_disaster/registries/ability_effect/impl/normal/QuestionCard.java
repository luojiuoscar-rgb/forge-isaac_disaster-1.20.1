package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class QuestionCard implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        InteractionHand hand = context.getOrDefault(ContextKeys.HAND, InteractionHand.MAIN_HAND);

        ItemStack target = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (hand == InteractionHand.MAIN_HAND){
            target = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        if (!(target.getItem() instanceof ActiveItem activeItem)) return true;
        ActiveAbility activeAbility = (ActiveAbility) activeItem.getAbility();

        // 调用对应效果
        activeAbility.onUse(player, hand);
        return true;
    }
}
