package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DarkPrincesCrown.BULLET_SPEED;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DarkPrincesCrown.RANGE;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DarkPrincesCrown.FIRE_RATE;

public final class DarkPrincesCrown extends PeriodicModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.DARK_PRINCES_CROWN)));

    public DarkPrincesCrown() { super(TRIGGER); }

    @Override
    public int getTickInterval(LivingEntity entity, int amplifier, PeriodicModuleQueue queue) { return 1; }

    @Override
    public void handleRemove(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        StatManager.removeModifier(player, player.getAttribute(StatManager.FIRE_RATE.getAttribute()),
                FIRE_RATE);
        StatManager.removeModifier(player, player.getAttribute(StatManager.RANGE.getAttribute()),
                RANGE);
        StatManager.removeModifier(player, player.getAttribute(StatManager.BULLET_SPEED.getAttribute()),
                BULLET_SPEED);
    }
}
