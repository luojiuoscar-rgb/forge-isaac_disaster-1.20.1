package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.ModAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class LokisHorns implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        shoot(player, context);
        return true;
    }

    private void shoot(Player player, ExecutableEffectContext context){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    AttackType attack = playerAbility.getCachedAttackType();

                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();
                    Vec3 eyePos = player.getEyePosition().add(0, player.getBbHeight() * -0.15, 0);

                    AttackContext baseCtx = new AttackContext(
                            player,
                            player,
                            colorRl,
                            new CompositeTrigger(),
                            trajectories,
                            eyePos,
                            player.getXRot(),
                            player.getYRot()
                    );

                    AttackContext reversedReference = baseCtx.copy();
                    Vec3 reversedDirection = Vec3.directionFromRotation(
                            baseCtx.getXRot(), baseCtx.getYRot()).scale(-1.0);
                    reversedReference.setDirection(reversedDirection);

                    List<AttackContext> extraContexts = ModAttackPattern.SEMICIRCLE.get().generate(
                            new AttackPatternContext(reversedReference, 3));

                    Object event = context.get(ContextKeys.EVENT);
                    if (event instanceof AttackPlanEvent planEvent) {
                        if (planEvent.getOrigin() != AttackOrigin.PLAYER_PRIMARY) return;
                        planEvent.appendExtraContexts(extraContexts);
                    } else {
                        AttackExecutor.perform(AttackRequest.withContexts(
                                player, attack, AttackOrigin.ABILITY_EXTRA,
                                AttackPipelineMode.BULLET_ONLY, extraContexts, false));
                   }
                }
        );
    }

}
