package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.BlueSpiderEntity;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import java.util.List;

public final class Infestation2 extends TriggerModule {
    private static final ResourceLocation INFESTATION_2 = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "infestation_2");
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.KILL_ENTITY, ModExecutableEffects.SPAWN_BLUE_SPIDER, context ->
                    context.get(ContextKeys.EVENT) instanceof LivingDeathEvent event && !event.isCanceled()
                            && event.getSource().getEntity() == context.getEntity()
                            && !(event.getSource().getDirectEntity() instanceof BlueSpiderEntity)
                            && event.getEntity().level() == context.getEntity().level(), INFESTATION_2)));

    public Infestation2() { super(TRIGGER); }

}
