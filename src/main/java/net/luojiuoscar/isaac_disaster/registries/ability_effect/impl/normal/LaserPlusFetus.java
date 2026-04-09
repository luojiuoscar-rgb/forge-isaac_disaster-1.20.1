package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class LaserPlusFetus implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.BULLET) instanceof FetusBullet bullet)) return true;
        if (!(bullet.getOwner() instanceof Player player)) return true;

        int interval = (int) PlayerHelper.getShotDelay(player) + 2;

        if (bullet.tickCount % interval != 0) return true;

        ModAttackType.LASER.get().performAttack(List.of(
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
        ));

        return true;
    }
}
