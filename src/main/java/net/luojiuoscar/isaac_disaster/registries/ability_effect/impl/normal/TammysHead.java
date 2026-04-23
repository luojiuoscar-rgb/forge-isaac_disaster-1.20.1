package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
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

                    int bulletCount = 13;

                    float angleInterval = 30;
                    float curAngle = -angleInterval * (bulletCount - 1) / 2.0f;

                    List<AttackContext> contexts = new ArrayList<>();
                    AttackContext ctx = new AttackContext(
                            player,
                            player,
                            colorRl,
                            new CompositeTrigger(),
                            trajectories,
                            eyePos,
                            player.getXRot(),
                            player.getYRot()
                    );

                    for (int i = 0; i < bulletCount; i++) {
                        AttackContext c = ctx.copy();
                        c.setYRotOffset(curAngle);
                        contexts.add(c);

                        curAngle += angleInterval;
                    }

                    // 给bullet附加CompositeTrigger
                    GetAttackContextEvent event =
                            new GetAttackContextEvent(player, contexts, attack, false);
                    MinecraftForge.EVENT_BUS.post(event);
                    contexts = event.getContexts();

                    IsaacDisaster.LOGGER.info("List of bullets: "+contexts);

                    attack.performAttack(contexts);
                    attack.makeSound(player);
                }
        );
    }
}