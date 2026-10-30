package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup.Pose;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.event.custom.attack.CollectHeldKnivesEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class LokisHornsHeldKnives implements IAbilityEffect {
    private static final List<Vec3> DIRECTIONS = List.of(new Vec3(-1, 0, 0), new Vec3(1, 0, 0), new Vec3(0, 0, -1));

    @Override
    public TriggerType getRequiredTriggerType() { return ModTriggerTypes.COLLECT_HELD_KNIVES; }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof CollectHeldKnivesEvent event)
                || context.getEntity() != event.getPlayer()) return false;
        double radius = Math.max(0.6, event.getPlayer().getBbWidth());
        event.addEntries(ModTriggerModules.LOKIS_HORNS.getId(), DIRECTIONS.stream()
                .map(direction -> new Pose(direction.subtract(PlayerMomsKnifeMotion.FORWARD).scale(radius), direction))
                .toList());
        return true;
    }
}
