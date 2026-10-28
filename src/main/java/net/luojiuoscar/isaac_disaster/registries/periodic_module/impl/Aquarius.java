package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class Aquarius extends PeriodicModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.AQUARIUS)
    ));
    // Compare actual server positions: a remote player's horizontal velocity can remain zero.
    private final Map<LivingEntity, Vec3> lastPositions = new WeakHashMap<>();

    public Aquarius() { super(TRIGGER); }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, PeriodicModuleQueue queue) { return 2; }

    @Override
    public void fire(ExecutableEffectContext context) {
        var entity = context.getEntity();
        if (!(entity instanceof ServerPlayer) || !entity.isAlive()) {
            lastPositions.remove(entity);
            return;
        }
        Vec3 current = entity.position();
        Vec3 previous = lastPositions.put(entity, current);
        if (previous == null || previous.subtract(current).horizontalDistanceSqr() <= 1.0E-8D) return;
        super.fire(context);
    }

    @Override
    public void handleRemove(LivingEntity entity) { lastPositions.remove(entity); }
}
