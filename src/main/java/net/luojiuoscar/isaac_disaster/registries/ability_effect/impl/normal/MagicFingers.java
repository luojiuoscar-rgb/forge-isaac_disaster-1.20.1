package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class MagicFingers implements IAbilityEffect {
    public static double damage(double playerDamage) {
        return 1.5 * playerDamage;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!PlayerHelper.takeMoney(player, GameplayConfig.COIN_TIER_1_VALUE.get())) return false;
        // Ipecac's damage is projectile-local; the player attribute excludes its large shot bonus.
        float damage = (float) damage(player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        for (var target : LevelHelper.selectBySphere(player.level(), player.position(), StatManager.getNearbyRange())) {
            if (EntityHelper.isFriendly(player, target)) continue;
            target.hurt(player.damageSources().playerAttack(player), damage);
        }
        return true;
    }
}
