package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class AddCoolDownToItem implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.getEntity() instanceof Player player){
            InteractionHand hand = context.get(ContextKeys.HAND);
            if (hand == null) return false;

            var nums = context.get(ContextKeys.DOUBLE);
            int ticks = 5;
            if (nums != null && !nums.isEmpty()){
                ticks = nums.get(0).intValue();
            }else{
                return false;
            }

            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), ticks);
        }

        return true;
    }
}
