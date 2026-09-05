package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LaserPlusFetus implements IAbilityEffect {
    static AttackPipelineMode secondaryLaserPipelineMode() {
        return AttackPipelineMode.EXECUTE_ONLY;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        IBulletObject bullet = context.get(ContextKeys.BULLET);
        if (bullet == null || bullet.getSourceType() != BulletSourceType.FETUS_BULLET) return true;
        if (!(bullet.getOwner() instanceof Player player)) return true;

        int interval = 4; // fixed interval
        if (!shouldFireThisTick(bullet, interval)) return true;
        Vec3 axis = bullet.getVelocity().lengthSqr() > 1.0E-8D ? bullet.getVelocity() : player.getLookAngle();

        AttackExecutor.perform(AttackRequest.withContexts(
                player, ModAttackTypes.LASER.get(), AttackOrigin.BULLET_SECONDARY,
                secondaryLaserPipelineMode(), List.of(
                AttackContext.builder(player, player)
                        .color(bullet.getColorId()).trigger(bullet.getTriggers())
                        .position(bullet.getPosition()).mainAxis(axis)
                        .range(ModAttackTypes.LASER.get().getRange(player))
                        .speed(ModAttackTypes.LASER.get().getBulletSpeed(player)).build()
        ), false));

        return true;
    }

    /** Keeps the legacy entity cadence while deriving lightweight-bullet cadence from its stored age. */
    private static boolean shouldFireThisTick(IBulletObject bullet, int interval) {
        // BulletTickEvent is emitted before BulletState advances, unlike Entity.tickCount which has
        // already been incremented by Entity#tick when the legacy event is posted.
        if (bullet instanceof BulletState state) return (state.age() + 1) % interval == 0;
        return false;
    }
}
