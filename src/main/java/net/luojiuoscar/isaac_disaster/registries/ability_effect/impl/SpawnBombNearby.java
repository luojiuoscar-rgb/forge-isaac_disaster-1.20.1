package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class SpawnBombNearby implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        var nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (nums.size() < 2){
            nums = List.of(0.1, 1.);
        }
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 6);
        amplifier = Math.min(amplifier, 12);

        PlayerHelper.spawnRandomBombsNearby(player,
                StatManager.getNearbyRange() * nums.get(0) * amplifier,
                nums.get(1).intValue() * amplifier);

    }
}