package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.FrozenEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class UranusFreezeOnKill implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var source = context.getEntity();
        var bullet = context.get(ContextKeys.BULLET);
        if (source == null || !(source.level() instanceof ServerLevel)
                || !(context.get(ContextKeys.EVENT) instanceof LivingDeathEvent event) || event.isCanceled()
                || bullet == null || ModDamageSources.projectile(event.getSource()) != bullet
                || bullet.getOwner() != source || event.getEntity().level() != source.level()) return false;
        if (!(event.getEntity() instanceof Mob mob) || !EntityVisualState.isEligible(mob)
                || mob.hasEffect(ModEffects.FROZEN.get())) return true;
        mob.addEffect(new MobEffectInstance(ModEffects.FROZEN.get(), -1, 0), source);
        if (mob.hasEffect(ModEffects.FROZEN.get())) {
            FrozenEffect.markIceStatue(mob);
            mob.setHealth(1);
            event.setCanceled(true);
        }
        return true;
    }
}
