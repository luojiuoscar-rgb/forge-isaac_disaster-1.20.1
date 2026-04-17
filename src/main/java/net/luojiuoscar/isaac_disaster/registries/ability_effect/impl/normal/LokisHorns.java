package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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

                    List<AttackContext> attackContexts = new ArrayList<>();

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

                    // 三个方向：左、右、后
                    float[] offsets = new float[] {
                            -90f,  // 左
                            90f,   // 右
                            180f   // 后
                    };

                    for (float offset : offsets) {
                        AttackContext c = baseCtx.copy();
                        c.setYRotOffset(offset);
                        attackContexts.add(c);
                    }

                    // 触发事件错误时：直接发射；触发事件正确时，如果是直接发射则加入发射序列，否则阻止
                    if (!(context.get(ContextKeys.EVENT) instanceof GetAttackContextEvent event)){
                        attack.performAttack(attackContexts);
                    }else if (event.isDirectlyShotByPlayer()) {
                        event.getContexts().addAll(attackContexts);
                    }
                    // do nothing
                }
        );
    }}