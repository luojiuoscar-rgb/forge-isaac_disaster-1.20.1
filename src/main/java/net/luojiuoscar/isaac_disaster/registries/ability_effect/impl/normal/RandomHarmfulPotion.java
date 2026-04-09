package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.ArrayList;
import java.util.List;

public class RandomHarmfulPotion implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingAttackEvent event)) return false;

        List<Entity> target = context.getOrDefault(ContextKeys.SECONDARY_ENTITIES, new ArrayList<>());
        if (target.isEmpty()) return false;

        // 攻击类型不正确只是不触发，并不是错误情况
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !event.getSource().is(DamageTypes.MOB_ATTACK)) return true;

        LivingEntity attacker = context.getEntity();
        for (Entity e : target){
            if (e instanceof LivingEntity e1){
                applyRandomEffect(attacker, e1);
            }
        }
        return true;
    }

    private void applyRandomEffect(LivingEntity attacker, LivingEntity target) {
        RandomSource random = target.getRandom();

        List<MobEffect> harmfulEffects = BuiltInRegistries.MOB_EFFECT.stream()
                .filter(effect -> effect.getCategory() == MobEffectCategory.HARMFUL)
                .toList();

        if (harmfulEffects.isEmpty()) return;

        // 随机选择一个有害效果
        MobEffect randomEffect = harmfulEffects.get(
                random.nextInt(harmfulEffects.size())
        );

        MobEffectInstance instance = new MobEffectInstance(
                randomEffect,
                target.getRandom().nextInt(350, 601),
                target.getRandom().nextInt(0,2),
                false,
                true,
                true
        );

        target.addEffect(instance, attacker);
    }
}
