package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.ModAttackPatterns;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public class TammysHead implements IAbilityEffect {
    private final static ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "tammys_head");

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(), SCHEDULE_TYPE,
                5,5, context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue(), true,
                () -> shoot(player));
        return true;
    }

    private void shoot(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    AttackType attack = playerAbility.getCachedAttackType();

                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();
                    Vec3 eyePos = player.getEyePosition().add(0, player.getBbHeight() * -0.15, 0);

                    int bulletCount = 12;
                    AttackContext ctx = AttackContext.builder(player, player)
                            .color(colorRl).trigger(new CompositeTrigger()).trajectories(trajectories)
                            .position(eyePos).mainAxis(GeometryHelper.mainAxisFromRotation(player.getXRot(), player.getYRot()))
                            .range(attack.getRange(player)).speed(attack.getBulletSpeed(player)).build();

                    List<AttackContext> contexts = ModAttackPatterns.RING.get().generate(
                            new AttackPatternContext(ctx, bulletCount));

                    AttackExecutor.perform(AttackRequest.withContexts(
                            player, attack, AttackOrigin.ABILITY_EXTRA,
                            AttackPipelineMode.PREPARE_AND_EXECUTE, contexts, true));
                }
        );
    }
}
