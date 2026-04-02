package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import java.util.List;

public class SpawnBombNearby implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (nums.size() < 2){
            nums = List.of(0.5, 6.);
        }

        double range = Mth.clamp(nums.get(0), 0.1, 2);
        int count = Mth.clamp(nums.get(1).intValue(), 1, 6);

        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 6);
        amplifier = Math.min(amplifier, 2);

        PlayerHelper.spawnRandomBombsNearby(player,
                StatManager.getNearbyRange() * range * amplifier,
                count * amplifier);

        return true;
    }
}