package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Lemon implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (nums.size() < 3) {
            nums = List.of(0.4, 100., 2.5);
        }

        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        float range = Mth.clamp(nums.get(0).floatValue(), 0.1f, 2f);
        int duration = Mth.clamp(nums.get(1).intValue(), 20, 1200);
        float damage = nums.get(2).floatValue() < 0 ? 0 : nums.get(2).floatValue();
        return GroundEffectRuntime.INSTANCE.spawnOnGround(ModGroundEffects.LEMON.get(), entity, pos,
                (float) StatManager.getNearbyRange() * range, duration,
                (float) StatManager.DAMAGE.getBonus() * damage, 10) != null;
    }
}
