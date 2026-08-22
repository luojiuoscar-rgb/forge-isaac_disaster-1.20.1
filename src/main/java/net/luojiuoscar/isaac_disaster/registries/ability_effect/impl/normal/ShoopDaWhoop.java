package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;

public class ShoopDaWhoop implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        Vec3 position = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        double damage = StatManager.DAMAGE.getBonus() * 2;
        var inst = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (inst != null){
            damage = inst.getValue();
        }

        AttackContext ctx = new AttackContext(
                entity,
                entity,
                ModBulletColor.SHOOP_DA_WHOOP.getId(),
                new CompositeTrigger(),
                new HashMap<>(),
                position,
                entity.getXRot(),
                entity.getYRot(),
                damage * 2 * amplifier
        );

        AttackExecutor.perform(AttackRequest.withContexts(
                entity, ModAttackType.SHOOP_DA_WHOOP.get(), AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.BULLET_ONLY, List.of(ctx), true));
        return true;
    }
}
