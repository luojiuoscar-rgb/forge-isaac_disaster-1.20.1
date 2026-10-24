package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeAim;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class MomsKnifeBarrage implements IAbilityEffect {
    public static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "moms_knife_barrage");

    public static int countForCharge(double chargeFraction) {
        double charge = Double.isFinite(chargeFraction) ? Mth.clamp(chargeFraction, 0, 1) : 0;
        return 2 + (int) Math.floor(8 * charge);
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
            Vec3 origin = PlayerMomsKnifeMotion.scatterOrigin(PlayerMomsKnifeMotion.barragePosition(player),
                    player.getYRot(), player.getXRot(), player.getRandom().nextDouble() * 2 - 1,
                    player.getRandom().nextDouble() * 2 - 1);
            PlayerMomsKnifeEntity knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), level);
            if (knife.initializeOneWay(player, origin, PlayerMomsKnifeAim.point(player), 24, speed, snapshot)) {
                level.addFreshEntity(knife);
            }
        });
        return true;
    }
}
