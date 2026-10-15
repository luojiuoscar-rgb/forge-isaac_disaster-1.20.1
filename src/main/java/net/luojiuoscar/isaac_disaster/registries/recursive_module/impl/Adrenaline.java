package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Adrenaline.DAMAGE;

public final class Adrenaline extends RecursiveModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.ADRENALINE)));

    public Adrenaline() { super(TRIGGER); }

    @Override
    public int getTickInterval(LivingEntity entity, int amplifier, RecursiveModuleQueue queue) { return 1; }

    @Override
    public void handleRemove(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            StatManager.removeModifier(player, player.getAttribute(StatManager.DAMAGE.getAttribute()),
                    DAMAGE);
        }
    }
}
