package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public class MidasTouch implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;

        DamageSource damageSource = event.getSource();
        Entity sourceEntity = damageSource.getEntity();
        Entity directEntity = damageSource.getDirectEntity();
        if (!isMeleeSource(sourceEntity, directEntity)) return true;

        List<LivingEntity> targets = context.get(ContextKeys.SECONDARY_LIVING_ENTITIES);
        if (targets == null) return false;

        context.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(ModEffects.GOLDEN.get(),
                        60,
                        0,
                        0,
                        0,
                        false)
        ));
        context.set(ContextKeys.EXECUTABLE_EFFECT, ModExecutableEffects.POTIONS.get());
        ModExecutableEffects.APPLY_EFFECT_TO_SECONDARY_LIVING_ENTITY.get().apply(context);
        return true;
    }

    private static boolean isMeleeSource(Entity sourceEntity, Entity directEntity) {
        return sourceEntity instanceof LivingEntity && sourceEntity == directEntity;
    }
}
