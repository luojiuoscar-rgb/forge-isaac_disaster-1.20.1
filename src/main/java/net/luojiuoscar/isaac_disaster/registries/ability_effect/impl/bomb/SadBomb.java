package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.bomb;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SadBomb extends BombRelated {
    @Override
    protected boolean customEffect(ExecutableEffectContext context, ServerPlayer player,
                                   Level level, Vec3 pos, IsaacBomb bomb) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    AttackType attack = playerAbility.getAttackSelection().baseAttack();

                    ResourceLocation colorRl = playerAbility.getBestBulletColor();

                    int bulletCount = getBulletCount(bomb.getPower());

                    AttackContext ctx = AttackContext.builder(player, bomb)
                            .color(colorRl).trigger(new CompositeTrigger())
                            .position(pos).mainAxis(GeometryHelper.mainAxisFromRotation(bomb.getXRot(), bomb.getYRot()))
                            .range(attack.getRange(player)).speed(attack.getBulletSpeed(player)).build();

                    List<AttackContext> contexts = ModAttackPatterns.RING.get().generate(
                            new AttackPatternContext(ctx, bulletCount));
                    AttackExecutor.perform(AttackRequest.withContexts(
                            player, attack, AttackOrigin.ABILITY_EXTRA,
                            AttackPipelineMode.PREPARE_AND_EXECUTE, contexts, true));
                });
        return true;
    }

    private int getBulletCount(int power) {
        if (power == BombData.MEGA.power()) {
            return 13;
        } else if (power == BombData.NORMAL.power()) {
            return 8;
        } else {
            return 0;
        }
    }
}
