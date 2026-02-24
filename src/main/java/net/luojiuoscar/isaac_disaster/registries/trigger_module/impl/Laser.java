package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletTickEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Set;

public class Laser implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.GET_ATTACK_CONTEXT,
                TriggerCategory.BULLET_HIT_ENTITY_BEFORE,
                TriggerCategory.BULLET_TICK
        );
    }

    @Override
    public void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {
        for (AttackContext context : event.getContexts()){
            context.addTriggerModule(ModTriggerModule.LASER.getId(), stacks);
        }
    }

    @Override
    public void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event, int stacks, TriggerModuleQueue queue) {
        if (event.getAttackType().equals(ModAttackType.BRIMSTONE.getId())){
            event.setDamage(event.getDamage() * 1.5);
        }
    }

    @Override
    public void onBulletTick(BulletTickEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getBullet() instanceof FetusBullet bullet)) return;
        if (!(bullet.getOwner() instanceof Player player)) return;

        int interval = (int) PlayerHelper.getShotDelay(player) + 2;

        if (bullet.tickCount % interval != 0) return;

        ModAttackType.LASER.get().performAttack(List.of(
                new AttackContext(
                        player,
                        bullet,
                        bullet.getColorId(),
                        bullet.getTriggerModules(),
                        bullet.getTrajectories(),
                        bullet.getPosition(),
                        bullet.getXRot(),
                        bullet.getYRot()
                )
        ));
    }

    @Override
    public double getPriority(){
        return TriggerModulePriority.LOWEST.priority;
    }
}
