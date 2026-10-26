package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.ViewPlaneRandomAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public final class MonstrosLungPlusMomsKnife implements IAbilityEffect {
    private static final ViewPlaneRandomAttackPattern PATTERN = new ViewPlaneRandomAttackPattern();

    static int countForCharge(double chargeFraction) {
        double charge = Double.isFinite(chargeFraction) ? Mth.clamp(chargeFraction, 0, 1) : 0;
        return Math.min(4, 1 + (int) Math.floor(4 * charge));
    }

    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ATTACK_FIRED;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackFiredEvent event) || event.getOwner() != player) return false;
        if (event.getOrigin() != AttackOrigin.PLAYER_PRIMARY
                || !ModAttackTypes.MOMS_KNIFE.getId().equals(event.getAttackType().getId())) return true;
        var firstFired = event.getFirstFiredContext();
        if (firstFired.getOwner() != player) return false;
        if (!(ModExecutableEffects.MOMS_KNIFE_SHOT.get() instanceof IAbilityEffect effect)) return false;

        var reference = firstFired.toBuilder().mainAxis(player.getLookAngle()).range(24).build();
        int count = countForCharge(event.getChargeFraction());
        boolean allSucceeded = true;
        for (var attack : PATTERN.generate(new AttackPatternContext(reference, count))) {
            attack.setPos(PlayerMomsKnifeMotion.scatterOrigin(player));
            ExecutableEffectContext shot = new ExecutableEffectContext(player);
            shot.set(ContextKeys.ATTACK_CONTEXT, attack);
            if (!effect.applyEffect(shot)) allSucceeded = false;
        }
        return allSucceeded;
    }
}
