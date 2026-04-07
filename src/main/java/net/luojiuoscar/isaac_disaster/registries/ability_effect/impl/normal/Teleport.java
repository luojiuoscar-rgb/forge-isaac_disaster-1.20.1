package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class Teleport implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        List<Double> radius = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        radius = new ArrayList<>(radius);
        if (radius.isEmpty()) radius = List.of(64.);
        LivingEntity entity = context.getEntity();

        EntityHelper.teleportToRandomLocation(entity, radius.get(0));

        // 在传送后获取位置并且播放声音
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f ,1.0f);
        return true;
    }
}