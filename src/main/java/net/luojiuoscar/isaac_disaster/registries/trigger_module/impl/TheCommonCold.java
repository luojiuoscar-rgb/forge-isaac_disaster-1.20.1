package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.Set;

public class TheCommonCold implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.ON_SHOOT,
                TriggerCategory.HIT_ENTITY,
                TriggerCategory.BULLET_HIT_ENTITY_BEFORE
        );
    }

    @Override
    public void onShoot(PlayerPerformAttackEvent event, int stacks, TriggerModuleQueue queue) {
        Player player = event.getPlayer();

        if (player.getRandom().nextDouble() < getTriggerChance(player)) {
            event.getContext().colorRl = ModBulletColor.POISON.getId();
            event.getContext().addTriggerModule(ModTriggerModule.THE_COMMON_COLD.getId(), 1);
        }
    }

    private double getTriggerChance(LivingEntity entity){
        return 1 / Math.max(1, 4 - (getLuck(entity) / 4));
    }

    @Override
    public void onHitEntity(LivingAttackEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity victim = event.getEntity();

        if (attacker.getRandom().nextDouble() < getTriggerChance(attacker)){
            applyEffect(attacker, victim);
        }
    }

    private void applyEffect(LivingEntity attacker, LivingEntity target){
        MobEffectInstance poisonEffect = new MobEffectInstance(
                ModEffects.POISON.get(),
                70,
                0,
                false,
                true,
                true
        );

        target.addEffect(poisonEffect, attacker);
    }

    @Override
    public void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getHit().getEntity() instanceof LivingEntity victim)) return;
        if (!(event.getSource() instanceof LivingEntity attacker)) return;

        applyEffect(attacker, victim);
    }
}
