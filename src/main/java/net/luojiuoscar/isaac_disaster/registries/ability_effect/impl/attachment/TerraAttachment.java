package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import java.util.List;
import java.util.HashSet;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;

public final class TerraAttachment extends AttackPreparationEffect {
    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.BREAK_BLOCK_AND_DROP)
    );

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSimpleTriggers(bullet_triggers);
        var visuals = new HashSet<>(attackContext.getVisualIds());
        visuals.add(ModBulletVisuals.TERRA_ROCK.getId());
        attackContext.setVisualIds(visuals);
    }
}
