package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.BombRelated;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SadBomb extends BombRelated {
    @Override
    protected boolean customEffect(ExecutableEffectContext context, ServerPlayer player,
                                   Level level, Vec3 pos, IsaacBomb bomb) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    AttackType attack = playerAbility.getCachedAttackType();

                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();

                    int bulletCount = getBulletCount(bomb.getPower());

                    float angleInterval = (float) 360 / bulletCount;
                    float curAngle = -angleInterval * (bulletCount - 1) / 2.0f;

                    List<AttackContext> contexts = new ArrayList<>();
                    AttackContext ctx = new AttackContext(
                            player,
                            bomb,
                            colorRl,
                            new CompositeTrigger(),
                            trajectories,
                            pos,
                            bomb.getXRot(),
                            bomb.getYRot()
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

                    attack.performAttack(contexts);
                    attack.makeSound(player);
                });
        return true;
    }

    private int getBulletCount(int power){
        if (power == BombData.MEGA.power()){
            return 13;
        }else if (power == BombData.NORMAL.power()){
            return 8;
        }else {
            return 0;
        }
    }
}
