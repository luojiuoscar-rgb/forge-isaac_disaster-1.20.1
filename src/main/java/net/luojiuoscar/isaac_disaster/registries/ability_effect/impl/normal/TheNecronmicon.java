package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TheNecronmicon implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        damageNearby(pos, context.getEntity(), StatManager.DAMAGE.getBonus() * 20 *
                context.getOrDefault(ContextKeys.AMPLIFIER, 1.));

        // sound
        entity.level().playSound(null, pos.x, pos.y, pos.z,
                ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }

    private void damageNearby(Vec3 pos, LivingEntity entity, double amount){
        Level level = entity.level();

        if (level.isClientSide) return;

        // 设定范围半径
        double radius = StatManager.getNearbyRange();

        List<LivingEntity> entities = LevelHelper.selectBySphere(
                level, pos.x, pos.y, pos.z, radius);

        // 遍历并造成伤害
        for (LivingEntity target : entities) {

            // 排除队友
            if (EntityHelper.isFriendly(target, entity)) continue;

            DamageSource source = entity instanceof Player ?
                    level.damageSources().playerAttack((Player) entity) :
                    level.damageSources().mobAttack(entity);

            // 造成伤害
            if (target.getMobType() == MobType.UNDEAD){
                target.hurt(source, (float) amount * 2);
            }else {
                target.hurt(source, (float) amount);
            }

            // 粒子
            damageParticle(target);
        }
    }

    private void damageParticle(LivingEntity entity){
        if (entity.level() instanceof ServerLevel serverLevel) {
            ParticleOptions particleType = ParticleTypes.LARGE_SMOKE; // 黑色烟雾粒子

            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2.0;
            double z = entity.getZ();

            // 向上喷射的黑烟
            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(
                        particleType,
                        x, y, z,
                        1,      // 一次一个粒子
                        0, 0.5, 0,// 扩散
                        0.1
                );
            }
        }
    }

}