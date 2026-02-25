package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.List;
import java.util.Set;

public class TheVirus implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.HIT_ENTITY
        );
    }
    @Override
    public void onHitEntity(LivingAttackEvent event, int stacks, TriggerModuleQueue queue) {
        // 只用player attack类型的伤害会触发（近战攻击）
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)) return;

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity victim = event.getEntity();

        applyRandomEffect(attacker, victim);
    }

    private void applyRandomEffect(LivingEntity attacker, LivingEntity target) {
        RandomSource random = target.getRandom();

        List<MobEffect> harmfulEffects = BuiltInRegistries.MOB_EFFECT.stream()
                .filter(effect -> effect.getCategory() == MobEffectCategory.HARMFUL)
                .toList();

        if (harmfulEffects.isEmpty()) {
            return;
        }

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
