package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeAim;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class MomsKnifeBarrage implements IAbilityEffect {
    public static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "moms_knife_barrage");

    public static int countForWaitTicks(long ticks) {
        return (int) Math.min(Integer.MAX_VALUE, 2 + Math.max(0, ticks) / 2);
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        Double amplifier = context.get(ContextKeys.AMPLIFIER);
        if (amplifier == null) return false;
        int count = Double.isFinite(amplifier) ? Math.max(0, (int) Math.floor(amplifier)) : 0;
        if (count == 0) return true;
        AttackContext attack = context.get(ContextKeys.ATTACK_CONTEXT);
        if (attack == null) return false;
        double speed = attack.getBulletSpeed();
        if (!Double.isFinite(speed) || speed <= 0) return false;
        if (!MomsKnifeAttack.isSelectedAndReady(player)) return false;
        ServerLevel level = player.serverLevel();
        AttackContext snapshot = attack.copyConfiguration();
        snapshot.freeze();
        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(), SCHEDULE_TYPE, 1, 0, count, false, () -> {
            if (player.level() != level || !MomsKnifeAttack.isSelectedAndReady(player)) {
                ScheduledFuncHelper.clearByType(SCHEDULE_TYPE, player.getUUID());
                return;
            }
            Vec3 origin = PlayerMomsKnifeMotion.scatterOrigin(player);
            ExecutableEffectContext shot = new ExecutableEffectContext(player);
            shot.set(ContextKeys.ATTACK_CONTEXT, snapshot.toBuilder().position(origin)
                    .mainAxis(PlayerMomsKnifeAim.point(player).subtract(origin)).build());
            ModExecutableEffects.MOMS_KNIFE_SHOT.get().apply(shot);
        });
        return true;
    }
}
