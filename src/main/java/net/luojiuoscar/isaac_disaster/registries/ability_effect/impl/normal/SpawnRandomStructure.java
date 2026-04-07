package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class SpawnRandomStructure implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) return true;
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();

        for (int i = 0; i < amplifier; i++){
            LevelHelper.spawnRandomStructure(level, BlockPos.containing(pos));
        }
        return true;
    }
}
