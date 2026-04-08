package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

public class StackFragileHeart implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        ((IStackableEffect) ModEffects.FRAGILE_HEART.get()).stack(player, 1);
        return true;
    }
}
