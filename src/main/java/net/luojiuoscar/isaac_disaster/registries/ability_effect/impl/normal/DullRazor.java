package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class DullRazor implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        // 避免循环触发
        if (context.get(ContextKeys.EVENT) instanceof LivingHurtEvent) return false;

        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1);
        amplifier = Math.max(amplifier, 2);

        for (int i = 0; i < amplifier; i++){
            LivingHurtEvent event = new LivingHurtEvent(player, player.damageSources().genericKill(), 0);
            MinecraftForge.EVENT_BUS.post(event);
        }
        return true;
    }
}