package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.luojiuoscar.isaac_disaster.networking.EntityVisualStateSync;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;

import java.util.List;

public final class DadsRing extends RecursiveModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.DADS_RING)
    ));

    public DadsRing() { super(TRIGGER); }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) { return 4; }

    @Override
    public void handleRemove(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (EntityVisualState.mutate(player, state -> state.removeVisualLayer(ModVisualLayers.DADS_RING.getId()))) {
            EntityVisualStateSync.syncToTracking(player);
            EntityVisualStateSync.syncToPlayer(player, player);
        }
    }
}
