package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class LaserPlusMomsKnife implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.BULLET) instanceof PlayerMomsKnifeEntity knife)
                || knife.getMode() != PlayerMomsKnifeEntity.Mode.RETURNING
                || !knife.isInFlight() || knife.discardsOnReturn()) return true;
        if (!(knife.getOwner() instanceof ServerPlayer player) || context.getEntity() != player
                || !player.isAlive() || player.isRemoved() || player.level() != knife.level()) return false;
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.2;
        if (!Double.isFinite(damage) || damage <= 0) return true;
        Vec3 start = PlayerMomsKnifeMotion.center(player, 1);
        Vec3 end = knife.position();
        if (start.distanceToSqr(end) < 1e-12) return true;
        AttackContext laser = AttackContext.builder(player, knife).attackType(ModAttackTypes.LASER.get())
                .position(start).direction(end.subtract(start).normalize()).range(start.distanceTo(end)).damage(damage).build();
        AttackContext source = knife.getAttackContext();
        if (source == null) return false;
        laser.setBulletScale(source.getBulletScale(), true);
        laser.freeze();
        return ((LaserAttack) ModAttackTypes.LASER.get()).shootBetween(laser);
    }
}
