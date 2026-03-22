package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class ApplyEffectToNearby implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        IAbilityEffect effect = context.get(ContextKeys.ABILITY_EFFECT);
        if (effect == null) return;

        LivingEntity entity = context.getEntity();
        boolean ignoreFriendly = context.getOrDefault(ContextKeys.BOOLEAN, List.of(true)).get(0);

        List<LivingEntity> entities = LevelHelper.selectBySphere(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(), StatManager.getNearbyRange());

        for (LivingEntity e : entities){
            if (ignoreFriendly && EntityHelper.isFriendly(entity, e)) continue;

            effect.apply(context.copy(e)); // 施加效果到对应实体上
        }
    }
}