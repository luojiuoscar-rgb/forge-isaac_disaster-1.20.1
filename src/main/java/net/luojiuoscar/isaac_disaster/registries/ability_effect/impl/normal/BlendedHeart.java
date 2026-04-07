package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.minecraft.world.entity.player.Player;

public class BlendedHeart implements IAbilityEffect {

    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return true;

        double health = player.getMaxHealth() - player.getHealth();
        if ( health >= StatManager.MAX_HEALTH.getBonus() * 0.5){
            ModAbilityEffects.HEAL.get().apply(context);
        } else if (health > 0) {
            ModAbilityEffects.HALF_RED_HEART.get().apply(context);
            ModAbilityEffects.HALF_SOUL_HEART.get().apply(context);
        }else {
            ModAbilityEffects.ABSORPTION.get().apply(context);
        }

        return true;
    }
}
