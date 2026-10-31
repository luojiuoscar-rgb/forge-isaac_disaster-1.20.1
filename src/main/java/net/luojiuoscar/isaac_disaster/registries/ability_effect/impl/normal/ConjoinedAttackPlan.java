package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ConjoinedAttackPlan implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ATTACK_PLAN;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event)) return false;
        if (event.getOwner() != player) return false;
        if (event.getOrigin() != AttackOrigin.PLAYER_PRIMARY || event.getBaseContexts().isEmpty()) return true;

        AttackContext reference = event.getAttackType().createAttackContext(player, player);
        if (reference == null) return false;
        Vec3 aim = reference.getMainAxis();
        Vec3 side = GeometryHelper.lateralAxis(aim);
        event.appendExtraContexts(List.of(
                reference.toBuilder().mainAxis(aim.add(side).normalize()).build(),
                reference.toBuilder().mainAxis(aim.subtract(side).normalize()).build()
        ));
        return true;
    }
}
