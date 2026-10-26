package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeAim;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackFiredEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class BookPlusMomsKnife implements IAbilityEffect {
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
        var reference = event.getFirstFiredContext();
        if (reference.getOwner() != player) return false;
        if (player.getRandom().nextDouble() >= 0.25) return true;

        Vec3 origin = PlayerMomsKnifeMotion.scatterOrigin(player);
        ExecutableEffectContext shot = new ExecutableEffectContext(player);
        shot.set(ContextKeys.ATTACK_CONTEXT, reference.toBuilder().position(origin)
                .mainAxis(PlayerMomsKnifeAim.point(player).subtract(origin)).range(24).build());
        return ModExecutableEffects.MOMS_KNIFE_SHOT.get() instanceof IAbilityEffect effect && effect.applyEffect(shot);
    }
}
