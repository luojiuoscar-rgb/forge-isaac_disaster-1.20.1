package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.world.entity.player.Player;

public class BlendedHeart implements IAbilityEffect {

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return true;

        double health = player.getMaxHealth() - player.getHealth();
        if ( health >= StatManager.MAX_HEALTH.getBonus() * 0.5){
            ModExecutableEffects.HEAL.get().apply(context);
        } else if (health > 0) {
            ModExecutableEffects.HALF_RED_HEART.get().apply(context);
            ModExecutableEffects.HALF_SOUL_HEART.get().apply(context);
        }else {
            ModExecutableEffects.ABSORPTION.get().apply(context);
        }

        return true;
    }
}
