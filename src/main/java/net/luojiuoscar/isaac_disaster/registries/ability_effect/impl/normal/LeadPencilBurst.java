package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.MonstrosLungAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.LeadPencil;
import net.minecraft.server.level.ServerPlayer;

public final class LeadPencilBurst implements IAbilityEffect {
    private static final int BURST_COUNT = 12;
    private static final MonstrosLungAttackPattern PATTERN = new MonstrosLungAttackPattern();

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        AttackType bullet = ModAttackTypes.BULLET.get();
        AttackContext reference = bullet.createAttackContext(player, player);
        if (reference == null) return false;
        LeadPencil.applyBloodColor(reference);
        reference.addTrajectoryModule(ModTrajectoryModules.GRAVITY.getId(), 1);
        var projectiles = PATTERN.generate(new AttackPatternContext(reference, BURST_COUNT));
        return AttackExecutor.perform(AttackRequest.withContexts(player, bullet, AttackOrigin.ABILITY_EXTRA,
                AttackPipelineMode.PREPARE_AND_EXECUTE, projectiles, false)
                .withSourceId(ModExecutableEffects.LEAD_PENCIL_BURST.getId()));
    }
}
