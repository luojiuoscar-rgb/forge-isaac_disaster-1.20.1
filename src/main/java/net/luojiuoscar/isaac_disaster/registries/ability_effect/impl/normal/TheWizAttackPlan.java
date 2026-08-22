package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class TheWizAttackPlan implements IExecutableEffect {
    @Override
    public void apply(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event)) return;

        MobEffectInstance effect = player.getEffect(ModEffects.THE_WIZ.get());
        if (effect == null) return;

        if (effect.getAmplifier() > 0) {
            replaceWithDualSpread(event, player);
        } else {
            rotateBaseContexts(event, player);
        }
    }

    private static void replaceWithDualSpread(AttackPlanEvent event, ServerPlayer player) {
        int bulletCount = (event.getBaseContexts().size() + 1) / 2;
        AttackType attackType = event.getAttackType();
        List<AttackContext> contexts = new ArrayList<>();

        for (AttackContext attackContext : attackType.getAttackContexts(player, bulletCount)) {
            attackContext.setYRot(attackContext.getYRot() - 45);
            contexts.add(attackContext);
        }
        for (AttackContext attackContext : attackType.getAttackContexts(player, bulletCount)) {
            attackContext.setYRot(attackContext.getYRot() + 45);
            contexts.add(attackContext);
        }

        event.replaceBaseContexts(contexts);
    }

    private static void rotateBaseContexts(AttackPlanEvent event, ServerPlayer player) {
        float rotation = player.getRandom().nextDouble() < 0.5 ? -45.0f : 45.0f;
        for (AttackContext attackContext : event.getBaseContexts()) {
            attackContext.setYRot(attackContext.getYRot() + rotation);
        }
    }
}
