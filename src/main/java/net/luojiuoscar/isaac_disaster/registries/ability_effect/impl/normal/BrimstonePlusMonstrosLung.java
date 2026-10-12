package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class BrimstonePlusMonstrosLung implements IExecutableEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ATTACK_PLAN;
    }

    @Override
    public void apply(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event)) return;
        if (event.getOrigin() != AttackOrigin.PLAYER_PRIMARY
                || !ModAttackTypes.BRIMSTONE.getId().equals(event.getAttackType().getId())) return;

        AttackContext reference = event.getAttackType().createAttackContext(player, player);
        if (reference == null) return;
        Vec3 axis = reference.getMainAxis();
        Vec3 lateral = GeometryHelper.lateralAxis(axis);
        int count = 3 + player.getRandom().nextInt(3);
        List<AttackContext> extraContexts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
            Vec3 direction = axis.scale(Math.cos(angle)).add(lateral.scale(Math.sin(angle)));
            // Copy a fresh, unprepared context; each beam rolls its own effects later.
            extraContexts.add(reference.toBuilder().mainAxis(direction).useFixedLaunchTransform().build());
        }
        event.appendExtraContexts(extraContexts);
    }
}
