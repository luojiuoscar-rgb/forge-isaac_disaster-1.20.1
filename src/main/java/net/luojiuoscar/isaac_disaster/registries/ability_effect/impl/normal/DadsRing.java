package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public final class DadsRing implements IAbilityEffect {
    public static final double RADIUS = 2.0;
    public static final double INNER_RADIUS = 1.0;

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var center = player.position();
        var level = player.serverLevel();
        for (var target : LevelHelper.selectBySphere(level, center, RADIUS)) {
            if (!target.isAlive() || target.isInvulnerable() || EntityHelper.isFriendly(player, target)) continue;
            if (player.distanceToSqr(target) <= INNER_RADIUS * INNER_RADIUS) continue;
            var targetContext = context.copy(target);
            targetContext.set(ContextKeys.AMPLIFIER, 1.0);
            if (!EntityVisualState.isEligible(target)) {
                if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) continue;
                targetContext.set(ContextKeys.POTIONS, List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, 0, 0, true)));
            } else {
                targetContext.set(ContextKeys.POTIONS, List.of(new PotionProfile(ModEffects.PETRIFIED.get(), 8, 0, 0, 0, true)));
            }
            ModExecutableEffects.POTIONS.get().apply(targetContext);
        }
        for (int index = 0; index < 8; index++) {
            double angle = index * Math.PI / 4;
            level.sendParticles(ParticleTypes.END_ROD, center.x + RADIUS * Math.cos(angle), center.y + 0.7 * player.getScale(),
                    center.z + RADIUS * Math.sin(angle), 1, 0, 0, 0, 0);
        }
        return true;
    }
}
