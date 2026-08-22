package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LaserPlusFetus implements IAbilityEffect {
    static AttackPipelineMode secondaryLaserPipelineMode() {
        return AttackPipelineMode.RAW;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.BULLET) instanceof FetusBullet bullet)) return true;
        if (!(bullet.getOwner() instanceof Player player)) return true;

        int interval = 4; // fixed interval

        if (bullet.tickCount % interval != 0) return true;

        AttackExecutor.perform(AttackRequest.withContexts(
                player, ModAttackType.LASER.get(), AttackOrigin.BULLET_SECONDARY,
                secondaryLaserPipelineMode(), List.of(
                new AttackContext(
                        player,
                        bullet,
                        bullet.getColorId(),
                        bullet.getTriggers(),
                        bullet.getTrajectories(),
                        bullet.getPosition(),
                        bullet.getXRot(),
                        bullet.getYRot()
                )
        ), false));

        return true;
    }
}
