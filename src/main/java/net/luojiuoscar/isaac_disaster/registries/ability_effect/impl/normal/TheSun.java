package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TheSun implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, player.position());

        ServerLevel level = (ServerLevel) player.level();
        level.setDayTime(6000); // 正午
        player.setHealth(player.getMaxHealth()); // 满血
        if (player.hasEffect(MobEffects.BLINDNESS)) player.removeEffect(MobEffects.BLINDNESS);
        if (player.hasEffect(MobEffects.DARKNESS)) player.removeEffect(MobEffects.DARKNESS);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, pos, StatManager.getNearbyRange());

        for (LivingEntity entity : entities){
            // 友好、有火抗生物不造成伤害
            if (EntityHelper.isFriendly(entity, player) || entity.fireImmune()) continue;
            // 生成火焰、造成火伤
            EntityHelper.setFireAtEntity(entity);
            entity.hurt(player.damageSources().inFire(),
                    (float) StatManager.DAMAGE.getBonus() * 64f * amplifier);

            damageParticle(entity);
        }
        return true;
    }

    private void damageParticle(LivingEntity entity){
        if (entity.level() instanceof ServerLevel serverLevel) {
            ParticleOptions particleType = ParticleTypes.FLAME;

            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2.0;
            double z = entity.getZ();

            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(
                        particleType,
                        x, y, z,
                        1,
                        0, 0.5, 0,
                        0.1
                );
            }
        }
    }
}
