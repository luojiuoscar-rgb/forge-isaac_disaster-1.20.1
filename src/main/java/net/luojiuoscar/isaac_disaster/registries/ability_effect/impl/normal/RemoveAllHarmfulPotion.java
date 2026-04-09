package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class RemoveAllHarmfulPotion implements IAbilityEffect {

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();

        List<MobEffectInstance> toRemove = new ArrayList<>();
        // 获取所有需要被移除的效果
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL
                    && !effect.getEffect().getCurativeItems().isEmpty()) {
                toRemove.add(effect);
            }
        }

        // 移除效果
        for (MobEffectInstance effect : toRemove) {
            entity.removeEffect(effect.getEffect());
        }

        return true;
    }
}
