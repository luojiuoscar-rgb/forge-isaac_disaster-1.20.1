package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BloodRights implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        float amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).floatValue();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        float damage = (float) StatManager.MAX_HEALTH.getBonus() * amplifier;
        // 自伤，排除创造模式和旁观模式玩家
        if (!(entity instanceof Player player && (player.isCreative() || player.isSpectator()))){
            entity.hurt(PlayerHelper.getSelfDamageSource(entity), damage);
        }

        // 对周围的生物造成伤害
        var entities = LevelHelper.selectBySphere(entity.level(), pos, StatManager.getNearbyRange());

        DamageSource damageSource = entity instanceof ServerPlayer player
                ? player.damageSources().playerAttack(player)
                : entity.damageSources().mobAttack(entity);

        for (LivingEntity e : entities){
            if (e.equals(entity)) continue; // 跳过自己

            e.hurt(damageSource, damage * 4);
        }
        return true;
    }
}
