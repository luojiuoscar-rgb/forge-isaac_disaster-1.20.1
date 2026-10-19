package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.world.phys.Vec3;

public final class SinusInfectionDamage implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() { return ModTriggerTypes.BULLET_STICK_TICK; }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var bullet = context.get(ContextKeys.BULLET);
        var stick = context.get(ContextKeys.BULLET_STICK);
        if (bullet == null || stick == null || stick.bullet() != bullet) return false;
        var target = stick.target();
        if (target == null || !target.isAlive() || target.isRemoved()
                || target.level() != bullet.getBulletLevel()) return false;
        if (stick.elapsedTicks() <= 0 || stick.elapsedTicks() % 20 != 0) return true;
        target.hurt(ModDamageSources.tear(target.level(), bullet.getOwner(), Vec3.ZERO), bullet.getDamage());
        return true;
    }
}
