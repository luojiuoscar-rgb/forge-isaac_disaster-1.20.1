package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.minecraft.server.level.ServerPlayer;

public class ShootLaser implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.getEntity() instanceof ServerPlayer player){
            AttackType attack = ModAttackType.TECHNOLOGY2.get();
            AttackExecutor.perform(AttackRequest.withContexts(
                    player, attack, AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.BULLET_ONLY, attack.getAttackContexts(player, 1), false));
            return true;
        }
        return false;
    }
}
