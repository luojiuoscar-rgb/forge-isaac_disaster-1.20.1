package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.server.level.ServerPlayer;

public final class MomsKnifeShot implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isRemoved()) return false;
        AttackContext attack = context.get(ContextKeys.ATTACK_CONTEXT);
        if (attack == null || attack.getOwner() != player) return false;
        PlayerMomsKnifeEntity knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), player.level());
        return knife.initializeOneWay(player, attack.getPos(), attack.getPos().add(attack.getMainAxis()),
                attack.getBulletRange(), attack.getBulletSpeed(), attack) && player.serverLevel().addFreshEntity(knife);
    }
}
