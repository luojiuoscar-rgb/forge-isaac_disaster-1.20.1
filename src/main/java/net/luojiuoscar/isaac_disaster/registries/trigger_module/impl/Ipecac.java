package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.List;
import java.util.Set;

public class Ipecac implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.ON_SHOOT,
                TriggerCategory.HIT_ENTITY,
                TriggerCategory.BULLET_HIT_ENTITY_BEFORE,
                TriggerCategory.BULLET_HIT_BLOCK
        );
    }

    @Override
    public void onShoot(PlayerPerformAttackEvent event, int stacks, TriggerModuleQueue queue) {
        event.getContext().colorRl = ModBulletColors.IPECAC.getId();
        event.getContext().addTriggerModule(ModTriggerModule.IPECAC.getId(), 1);
    }

    @Override
    public void onHitEntity(LivingAttackEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity victim = event.getEntity();

        AttributeInstance instance = attacker.getAttribute(Attributes.ATTACK_DAMAGE);
        float damage = instance == null ? 0.0f : (float) instance.getValue();

        applyEffect(attacker, victim.position(), damage);
    }

    private void applyEffect(LivingEntity attacker, Vec3 pos, float damage){
        damage = (float) computeDamageWithCompensation(damage);

        float power = powerFromDamage(damage);

        LevelHelper.explodeCustom(attacker, pos, power, damage, false);

        List<LivingEntity> livingEntities = LevelHelper.selectBySquare(attacker.level(), pos.x, pos.y, pos.z,
                power + 2);

        for (LivingEntity entity : livingEntities){

            if(EntityHelper.isFriendly(entity, attacker)) continue;
            MobEffectInstance poisonEffect = new MobEffectInstance(
                    ModEffects.POISON.get(),
                    (int) Math.min(320, damage * 10),
                    (int) Math.max(0, power - 3),
                    false,
                    true,
                    true
            );

            entity.addEffect(poisonEffect, attacker);
        }
    }

    private static int powerFromDamage(float damage) {
        if (damage <= 10) return 2;
        if (damage <= 25) return 3;
        if (damage <= 50) return 4;
        if (damage <= 100) return 5;
        return 6;
    }

    private static double computeDamageWithCompensation(double damage) {
        double BASE = 4.0;   // damage=0 → 4
        double MAX = 23.0;   // 最终趋近值
        double k = 0.26;     // 衰减速度

        double finalDamage = MAX - (MAX - BASE) * Math.exp(-k * damage);
        double compensation = finalDamage - damage;

        if (compensation < 0.1) compensation = 0;

        return damage + compensation;
    }


    @Override
    public void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getHit().getEntity() instanceof LivingEntity victim)) return;

        applyEffect(victim, victim.position(), event.getBulletObject().getDamage());
    }

    @Override
    public void onBulletHitBlock(IsaacAttackHitBlockEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getSource() instanceof LivingEntity living)) return;
        applyEffect(living, event.getHitResult().getLocation(), event.getBulletObject().getDamage());
    }

}
