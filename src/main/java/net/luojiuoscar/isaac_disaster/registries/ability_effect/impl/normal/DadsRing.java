package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.luojiuoscar.isaac_disaster.networking.EntityVisualStateSync;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class DadsRing implements IAbilityEffect {
    public static final double RADIUS = 2.0;
    public static final double INNER_RADIUS = 1.0;

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        var center = player.position();
        var level = player.serverLevel();
        if (EntityVisualState.mutate(player, state -> state.addVisualLayer(ModVisualLayers.DADS_RING.getId()))) {
            EntityVisualStateSync.syncToTracking(player);
            EntityVisualStateSync.syncToPlayer(player, player);
        }
        for (var target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS))) {
            if (!target.isAlive() || target.isInvulnerable() || EntityHelper.isFriendly(player, target)) continue;
            var offset = target.position().subtract(center);
            double horizontalDistanceSqr = offset.horizontalDistanceSqr();
            if (Math.abs(offset.y) > RADIUS
                    || horizontalDistanceSqr <= INNER_RADIUS * INNER_RADIUS
                    || horizontalDistanceSqr > RADIUS * RADIUS) continue;
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
        return true;
    }
}
